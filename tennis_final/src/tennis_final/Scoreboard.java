package tennis_final;

import java.io.Serializable;
import java.util.ArrayList;

class ScoreBoard implements Serializable {  // 게임 데이터를 저장하고 다시 불러올 때 직렬화를 지원하기 위함.
	Team team1 ;           // private 사용하여 내부 데이터 직접 접근 막음.   player2;
	Team team2;
	private String team1Name, team2Name;
	int totalSets;            // 경기의 총 세트 수 저장                   

	boolean tieBreak;         // 현재 타이브레이크 진행 중 여부
	int tieBreakMode;         // 0=미적용, 1=전체 세트, 2=마지막 세트만
	int tieBreakPoints;       // 사용자 입력: 7 or 10 등
	boolean isDuece;

	boolean isDoubles;        // 단/복식      
	boolean isMale;           // 성별        
	String scoreStatus;       // 전광판 점수 형식 
	boolean noAd; 			  // No-Ad 규칙 사용 여부
	
	FileManager fileManager;
	
	// 게임 기록 저장
	ArrayList<ArrayList<Integer>> gameHistoryList = new ArrayList<>();
	

	ScoreBoard(Team t1, Team t2,
			int sets,
			boolean isDoubles,
			boolean isMale,
			int tieBreakMode,
			int tieBreakPoints,
			boolean noAd) {
		this.team1 = t1;
		this.team2 = t2;
		this.team1Name = team1.getName();
		this.team2Name = team2.getName();
		this.totalSets = sets;

		this.tieBreak = false;                // 기본값 false
		this.tieBreakMode = tieBreakMode;                 
		this.tieBreakPoints = tieBreakPoints;  
		this.isDoubles = isDoubles;
		this.isMale = isMale;
		this.noAd = noAd; 						// No-Ad 적용 여부 저장
		this.scoreStatus = "0 - 0";            // 기본값 0-0
		
		this.isDuece = false;
		
		gameHistoryList.add(new ArrayList<>()); // team1
		gameHistoryList.add(new ArrayList<>()); // team2
		this.fileManager = new FileManager(this);
		fileManager.writeMatchInfo();
		
	}

	// 점수변환 메서드 
	String convertPoints(int points) { // Private : 클래스 내부에서만 호출되어야 하는 보조 메서드. 외부에서 호출할 필요 없음. 
		
		switch (points) {
		case 0: return "0";
		case 1: return "15";
		case 2: return "30";
		case 3: return "40";
		default: return "승리";
		}
	}
	
	String convertPoints(Team scorer, Team opponent, boolean isDeuce, boolean isTiebreak) {
		int scorerPoint = scorer.getPoints();
		int opponentPoint = opponent.getPoints();
		
	    if (isTiebreak) {
	        return String.valueOf(scorerPoint);
	    }

	    if (isDeuce) {
	        int diff = scorerPoint - opponentPoint;
	        
	        if (scorerPoint >= 4 || opponentPoint >= 4) {
	            if (diff == 0) {
	                return "40";
	            } else if (diff == 1) {
	                return "AD";
	            } else if (diff >= 2) {
	            	isDuece = false;
	                return "승리";
	            } else {
	                return ""; // 상대가 AD인 경우
	            }
	        } else {
	            return "40"; // 둘 다 3점 이하이면 그냥 40으로 표시
	        }
	    }

	    // 일반 게임 상황
	    switch (scorerPoint) {
	        case 0: return "0";
	        case 1: return "15";
	        case 2: return "30";
	        case 3: return "40";
	        default: return "승리";
	    }
	}

	// 타이브레이크 전용 스코어 출력 메서드
	private void displayTieBreakScore() { // Private : 클래스 내부에서만 호출되어야 하는 보조 메서드. 외부에서 호출할 필요 없음.
		System.out.println("----------------------------------");
		System.out.println("Tie-break in progress!");
		System.out.println(team1.getName() + " - Sets: " + team1.getSetsWon()
		+ " | Games: " + team1.getGamesWon()
		+ " | Tie-break points: " + team1.getPoints());
		System.out.println(team2.getName() + " - Sets: " + team2.getSetsWon()
		+ " | Games: " + team2.getGamesWon()
		+ " | Tie-break points: " + team2.getPoints());
		System.out.println("----------------------------------");
	}

	// 득점 메서드 (타이브레이크 상황 구분)
	public void pointScored(int playerNumber) {
		Team scorer = (playerNumber == 1) ? team1 : team2;     // 득점자   
		Team opponent = (playerNumber == 1) ? team2 : team1;   // 실점자 결정

		if (tieBreak) {  // 타이브레이크 모드
			// 타이브레이크 중 득점
			scorer.addPoint();           // 득점자 득점          
			saveRecordToArrayList();
			convertPoints(scorer, opponent, isDoubles, tieBreak);
			displayTieBreakScore();      // 타이브레이크 전용 전광판 출력
			checkTieBreakWin(scorer, opponent);   // 타이브레이크 종료여부 판단
		} else {
			// 일반 득점
			scorer.addPoint();       // 득점자 득점      
			saveRecordToArrayList();
			convertPoints(scorer, opponent, isDoubles, tieBreak);
			updateScoreStatus();     // 전광판 점수 업데이트 
			checkGameWin(scorer, opponent);     // 게임 종료 여부 판단
		}
		
	}

	private void saveRecordToArrayList() {
		gameHistoryList.get(0).add(team1.getPoints());
		gameHistoryList.get(1).add(team2.getPoints());
	}

	// 게임 종료 여부 판단 메서드
	private void checkGameWin(Team scorer, Team opponent) {
		// 40점(3) 이상 득점 & 2점 이상 차이 => 게임 획득
		if (noAd&&scorer.getPoints() >= 3 && opponent.getPoints() >= 3) {  //2점이상 차이로 40점 득점
			scorer.winGame();              // gamesWon++;, points 
			opponent.resetPoints();        // 상대방의 점수 초기화       
			fileManager.writeGameHistroy();
			updateScoreStatus();           // 전광판 점수 실시간 업데이트     
			checkSetWin(scorer, opponent); // 세트 승리여부 판단   
			gameHistoryList.get(0).clear(); 
			gameHistoryList.get(1).clear(); 
		} else {
			 if (scorer.getPoints() >= 3 && (scorer.getPoints() - opponent.getPoints()) >= 2) {
	                scorer.winGame();
	                opponent.resetPoints();
	                fileManager.writeGameHistroy();
	                updateScoreStatus();
	                checkSetWin(scorer, opponent);
	                gameHistoryList.get(0).clear(); 
	    			gameHistoryList.get(1).clear(); 
	            }
		}
	}
	// 세트 종료 여부 판단 메서드 ( 타이브레이크 활성화 판단 d/t 세트 종료 여부 게임으로 판단함)
	private void checkSetWin(Team scorer, Team opponent) {
		// 현재까지 끝난 세트 수
		int setsPlayed = team1.getSetsWon() + team2.getSetsWon();
		// 지금 진행 중인 세트가 마지막 세트인지 여부
		boolean isFinalSet = (setsPlayed == totalSets - 1);

		// 두 선수 모두 6게임 이상 => 6:6 이상으로 진행
		if (scorer.getGamesWon() >= 6 && opponent.getGamesWon() >= 6 && !tieBreak) {
			switch (tieBreakMode) {
			case 1: // 전체 세트에 타이브레이크
				tieBreak = true;
				System.out.println("Tie-break activated!");
				displayScoreBoard();   // 6:6을 확인할 수 있도록 즉시 전광판 출력 
				break;
			case 2: // 마지막 세트만 타이브레이크
				if (isFinalSet) {
					tieBreak = true;
					System.out.println("Tie-break activated (final set)!");
					displayScoreBoard();
				} else {	// 마지막 세트가 아니라면 타이브레이크 발동 안 함
					if ((scorer.getGamesWon() - opponent.getGamesWon()) >= 2) {
						scorer.winSet();	//  setsWon++; gamesWon = 0;
						opponent.resetGames();  // 세트 종료 후 상대편 게임도 0으로 초기화
						updateScoreStatus(); // 전광판 업데이트
						checkMatchWin(); // 매치 종료 여부 판단
					}
				}
				break;
			case 0: default:
				// 타이브레이크 미적용 상태에서는 아무것도 안 하고
				// (계속 2게임 차이 날 때까지 게임 진행)
				if ((scorer.getGamesWon() - opponent.getGamesWon()) >= 2) {
					scorer.winSet();	//  setsWon++; gamesWon = 0;
					opponent.resetGames();  // 세트 종료 후 상대편 게임도 0으로 초기화
					updateScoreStatus(); // 전광판 업데이트
					checkMatchWin(); // 매치 종료 여부 판단
					break;
				}
			}
		}
		// 6게임 이상 & 2게임 차이 => 세트 획득
		else if (scorer.getGamesWon() >= 6 
				&& (scorer.getGamesWon() - opponent.getGamesWon()) >= 2) {
			scorer.winSet();
			opponent.resetGames();
			updateScoreStatus();
			checkMatchWin();
		}
	}
	// 타이브레이크 종료 여부 판단 메서드
	private void checkTieBreakWin(Team scorer, Team opponent) {
		// ★ 사용자 입력 tieBreakPoints 사용 (기존 isDoubles ? 10 : 7 대체)
		int requiredPoints = tieBreakPoints; // 기본값 7포인트

		// requiredPoints 이상 & 2점 차이
		if (scorer.getPoints() >= requiredPoints 
				&& (scorer.getPoints() - opponent.getPoints()) >= 2) {

			// 타이브레이크가 끝나면 세트 종료
			scorer.winSet(); 		// etWon++, gameWon = 0;
			opponent.resetGames(); // 상대의 gameWon 초기화

			tieBreak = false;  		// 타이브레이크 종료	
			team1.resetPoints(); // 선수 두명 모두 포인트 0 초기화
			team2.resetPoints();
			fileManager.writeGameHistroy();
			updateScoreStatus();	// 전광판 업데이트        
			checkMatchWin();		// 매치 종료 여부 판단  
			displayScoreBoard();	// 세트/매치 상황도 최종 출력 
			gameHistoryList.clear();
		}
		
	}

	// 매치 종료 여부 판단 메서드
	private void checkMatchWin() {
		// ex) 3세트 경기면 2세트 이상, 5세트 경기면 3세트 이상 이긴 쪽 승리
		if (team1.getSetsWon() >= (totalSets / 2) + 1) {
			System.out.println("\nMatch Over! Winner: " + team1.getName());
			System.exit(0); // 경기 종료
			fileManager.writeFinalResult();
		} else if (team2.getSetsWon() >= (totalSets / 2) + 1) {
			System.out.println("\nMatch Over! Winner: " + team2.getName());
			System.exit(0);
		}
	}

	// 득점상황 표시 메서드 (듀스 / 어드밴티지 상태 판단)
	private void updateScoreStatus() {
		if (team1.getPoints() >= 3 && team2.getPoints() >= 3) { //40:40
			if (noAd) {
				scoreStatus =  convertPoints(team1.getPoints()) 
						+ " - " + convertPoints(team2.getPoints());;
			} else {
			if (team1.getPoints() == team2.getPoints()) {
				scoreStatus = "Deuce";
				isDuece = true;
			} else if (team1.getPoints() > team2.getPoints()) {
				scoreStatus = "Advantage " + team1.getName();
			} else {
				scoreStatus = "Advantage " + team2.getName();
			}
			}
		} else {
			// 일반 득점 변환(0,15,30,40,A)  
			scoreStatus = convertPoints(team1.getPoints()) 
					+ " - " + convertPoints(team2.getPoints());
		}
		displayScoreBoard();
	}

	 // 전광판 형식 메서드 
	public void displayScoreBoard() {
		System.out.println("----------------------------------");
		System.out.println(team1.getName() + " - Sets: " + team1.getSetsWon()
		+ " | Games: " + team1.getGamesWon());
		System.out.println(team2.getName() + " - Sets: " + team2.getSetsWon()
		+ " | Games: " + team2.getGamesWon());
		System.out.println("----------------------------------");
		System.out.println("Score: " + scoreStatus);
		System.out.println("----------------------------------");
	}
}
