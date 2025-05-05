package days29;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Scanner;

//타이브레이크 선택(x,전체 세트, 마지막 세트만), 타이브레이크 점수 선택 추가
// Serializable : 객체를 바이트 스트림으로 변환할 수 있도록 하는 마커 인터페이스(marker interface)
class Player implements Serializable {//게임 데이터를 저장하고 다시 불러올 때 직렬화를 지원하기 위함.
	//ScoreBoard 객체를 파일로 저장하려면 내부에 있는 Player 객체도 직렬화 가능해야 하므로, 
	//Player도 Serializable 구현
	private String name;
	private int points;
	private int gamesWon;
	private int setsWon;	//private -> 객체 지향, 외부 접근 막음

	public Player(String name) {
		this.name = name;
		this.points = 0;
		this.gamesWon = 0;
		this.setsWon = 0;
	}

	public String getName() { return name; }
	public int getPoints() { return points; }
	public int getGamesWon() { return gamesWon; }
	public int getSetsWon() { return setsWon; }

	public void addPoint() { points++; }                    // 득점                                   
	public void winGame() {  gamesWon++; points = 0;}       // 게임승리 시 , 게임점수++, 포인트 0점 초기화          
	public void winSet() { setsWon++; gamesWon = 0; }       // 세트승리 시 , 세트점수++, 게임 스코어 0점 초기화       
	public void resetPoints() {points = 0;}                 // 승리하지 못하고 점수 초기화 필요 시                 
	public void resetGames() { this.gamesWon = 0; }         // 세트 종료 시 양쪽 플레이어를 모두 0으로 만들기 위해 사용    
}

class ScoreBoard implements Serializable {  // 게임 데이터를 저장하고 다시 불러올 때 직렬화를 지원하기 위함.
	private Player player1;           // private 사용하여 내부 데이터 직접 접근 막음.   
	private Player player2;                                              
	private int totalSets;            // 경기의 총 세트 수 저장                   

	private boolean tieBreak;         // 현재 타이브레이크 진행 중 여부
	private int tieBreakMode;         // 0=미적용, 1=전체 세트, 2=마지막 세트만
	private int tieBreakPoints;       // 사용자 입력: 7 or 10 등

	private boolean isDoubles;        // 단/복식      
	private boolean isMale;           // 성별        
	private String scoreStatus;       // 전광판 점수 형식 

	public ScoreBoard(Player p1, Player p2,
			int sets,
			boolean isDoubles,
			boolean isMale,
			int tieBreakMode,
			int tieBreakPoints) {
		this.player1 = p1;
		this.player2 = p2;
		this.totalSets = sets;

		this.tieBreak = false;                // 기본값 false
		this.tieBreakMode = tieBreakMode;                 
		this.tieBreakPoints = tieBreakPoints;  
		this.isDoubles = isDoubles;
		this.isMale = isMale;
		this.scoreStatus = "0 - 0";            // 기본값 0-0
	}

	// 점수변환 메서드 
	private String convertPoints(int points) { // Private : 클래스 내부에서만 호출되어야 하는 보조 메서드. 외부에서 호출할 필요 없음. 
		switch (points) {
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
		System.out.println(player1.getName() + " - Sets: " + player1.getSetsWon()
		+ " | Games: " + player1.getGamesWon()
		+ " | Tie-break points: " + player1.getPoints());
		System.out.println(player2.getName() + " - Sets: " + player2.getSetsWon()
		+ " | Games: " + player2.getGamesWon()
		+ " | Tie-break points: " + player2.getPoints());
		System.out.println("----------------------------------");
	}

	// 득점 메서드 (타이브레이크 상황 구분)
	public void pointScored(int playerNumber) {
		Player scorer = (playerNumber == 1) ? player1 : player2;     // 득점자   
		Player opponent = (playerNumber == 1) ? player2 : player1;   // 실점자 결정

		if (tieBreak) {  // 타이브레이크 모드
			// 타이브레이크 중 득점
			scorer.addPoint();           // 득점자 득점          
			displayTieBreakScore();      // 타이브레이크 전용 전광판 출력
			checkTieBreakWin(scorer, opponent);   // 타이브레이크 종료여부 판단
		} else {
			// 일반 득점
			scorer.addPoint();       // 득점자 득점      
			updateScoreStatus();     // 전광판 점수 업데이트 
			checkGameWin(scorer, opponent);     // 게임 종료 여부 판단
		}
	}

	// 게임 종료 여부 판단 메서드
	private void checkGameWin(Player scorer, Player opponent) {
		// 40점(3) 이상 득점 & 2점 이상 차이 => 게임 획득
		if (scorer.getPoints() >= 3 && (scorer.getPoints() - opponent.getPoints()) >= 2) {  //2점이상 차이로 40점 득점
			scorer.winGame();              // gamesWon++;, points 
			opponent.resetPoints();        // 상대방의 점수 초기화         
			updateScoreStatus();           // 전광판 점수 실시간 업데이트     
			checkSetWin(scorer, opponent); // 세트 승리여부 판단   
		}
	}
	// 세트 종료 여부 판단 메서드 ( 타이브레이크 활성화 판단 d/t 세트 종료 여부 게임으로 판단함)
	private void checkSetWin(Player scorer, Player opponent) {
		// 현재까지 끝난 세트 수
		int setsPlayed = player1.getSetsWon() + player2.getSetsWon();
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
	private void checkTieBreakWin(Player scorer, Player opponent) {
		// ★ 사용자 입력 tieBreakPoints 사용 (기존 isDoubles ? 10 : 7 대체)
		int requiredPoints = tieBreakPoints; // 기본값 7포인트

		// requiredPoints 이상 & 2점 차이
		if (scorer.getPoints() >= requiredPoints 
				&& (scorer.getPoints() - opponent.getPoints()) >= 2) {

			// 타이브레이크가 끝나면 세트 종료
			scorer.winSet(); 		// etWon++, gameWon = 0;
			opponent.resetGames(); // 상대의 gameWon 초기화

			tieBreak = false;  		// 타이브레이크 종료	
			player1.resetPoints(); // 선수 두명 모두 포인트 0 초기화
			player2.resetPoints();
			updateScoreStatus();	// 전광판 업데이트        
			checkMatchWin();		// 매치 종료 여부 판단  
			displayScoreBoard();	// 세트/매치 상황도 최종 출력 
		}
	}

	// 매치 종료 여부 판단 메서드
	private void checkMatchWin() {
		// ex) 3세트 경기면 2세트 이상, 5세트 경기면 3세트 이상 이긴 쪽 승리
		if (player1.getSetsWon() >= (totalSets / 2) + 1) {
			System.out.println("\nMatch Over! Winner: " + player1.getName());
			System.exit(0); // 경기 종료
		} else if (player2.getSetsWon() >= (totalSets / 2) + 1) {
			System.out.println("\nMatch Over! Winner: " + player2.getName());
			System.exit(0);
		}
	}

	// 득점상황 표시 메서드 (듀스 / 어드밴티지 상태 판단)
	private void updateScoreStatus() {
		if (player1.getPoints() >= 3 && player2.getPoints() >= 3) { //40:40
			if (player1.getPoints() == player2.getPoints()) {
				scoreStatus = "Deuce";
			} else if (player1.getPoints() > player2.getPoints()) {
				scoreStatus = "Advantage " + player1.getName();
			} else {
				scoreStatus = "Advantage " + player2.getName();
			}
		} else {
			// 일반 득점 변환(0,15,30,40,A)  
			scoreStatus = convertPoints(player1.getPoints()) 
					+ " - " + convertPoints(player2.getPoints());
		}
		displayScoreBoard();
	}

	 // 전광판 형식 메서드 
	public void displayScoreBoard() {
		System.out.println("----------------------------------");
		System.out.println(player1.getName() + " - Sets: " + player1.getSetsWon()
		+ " | Games: " + player1.getGamesWon());
		System.out.println(player2.getName() + " - Sets: " + player2.getSetsWon()
		+ " | Games: " + player2.getGamesWon());
		System.out.println("----------------------------------");
		System.out.println("Score: " + scoreStatus);
		System.out.println("----------------------------------");
	}
}

class Save {
	// 게임 저장
	public static void saveGame(ScoreBoard scoreboard) throws IOException {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
			out.writeObject(scoreboard);
		}
	}
	// 게임 불러오기
	public static ScoreBoard loadGame() throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
			return (ScoreBoard) in.readObject();
		}
	}
}

public class tennis {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		ScoreBoard scoreboard = null;
		String loadOption;

		// 유효한 입력을 받을 때까지 반복
		while (true) {
			System.out.print("세이브된 게임을 불러오시겠습니까? (네/아니오): ");
			loadOption = scanner.nextLine().toLowerCase();
			
			if (loadOption.equals("네") || loadOption.equals("아니오")) break; // 올바른 입력이면 루프 종료
			System.out.println("잘못된 입력!!! '네' or '아니오'.");
		}

		if (loadOption.equals("네")) {
			try {
				scoreboard = Save.loadGame();
				System.out.println("성공적으로 불러왔습니다!");
				scoreboard.displayScoreBoard();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("로딩 에러: " + e.getMessage());
				return;
			}
		} else { // "no"인 경우
			// 새로운 게임 설정
			System.out.print("첫번째 플레이어 이름: ");
			String player1Name = scanner.nextLine();

			System.out.print("두번째 플레이어 이름: ");
			String player2Name = scanner.nextLine();

			String gender;
			while (true) {
				System.out.print("성별을 고르세요 (남자/여자): ");
				gender = scanner.nextLine().toLowerCase();
				if (gender.equals("남자") || gender.equals("여자")) {
					break;
				}
				System.out.println("잘못된 입력!!! '남자' or '여자'");
			}

			String gameMode;
			while (true) {
				System.out.print("단식/복식을 고르세요 (단식/복식): ");   
				gameMode = scanner.nextLine().toLowerCase();
				if (gameMode.equals("단식") || gameMode.equals("복식")) break;
				System.out.println("잘못된 입력!!! '단식' or '복식'");
			}

			int totalSets;
			while (true) {
				System.out.print("세트 선택 (3 or 5): ");
				if (scanner.hasNextInt()) {
					totalSets = scanner.nextInt();
					scanner.nextLine(); 
					// 남자 단식만 5세트 가능, 나머지는 3세트만
					if (totalSets == 3 || 
							(totalSets == 5 && gender.equalsIgnoreCase("남자") && gameMode.equals("단식"))) {
						break;
					}
				} else {
					scanner.nextLine();
				}
				System.out.println("잘못된 입력!!! 남자 단식만 5세트 가능, 나머지는 3세트만 선택 가능합니다.");
			}

			// ★ 타이브레이크 모드 선택
			int tieBreakMode;
			while (true) {
				System.out.print("타이브레이크 방식을 선택해주세요 (0=미적용, 1=전체 세트, 2=마지막 세트만): ");
				if (scanner.hasNextInt()) {
					tieBreakMode = scanner.nextInt();
					scanner.nextLine();
					if (tieBreakMode == 0 || tieBreakMode == 1 || tieBreakMode == 2) {
						break;
					}
				} else {
					scanner.nextLine();
				}
				System.out.println("잘못된 입력!!! 0, 1, 2 중 하나를 입력하세요.");
			}

			// ★ tieBreakMode != 0 인 경우 타이브레이크 포인트(7 또는 10 등) 입력받기
			int tieBreakPoints = 7; // 기본값
			if (tieBreakMode != 0) {
				while (true) {
					System.out.print("타이브레이크에 사용할 포인트를 입력하세요 (7 or 10): ");
					if (scanner.hasNextInt()) {
						tieBreakPoints = scanner.nextInt();
						scanner.nextLine();
						if (tieBreakPoints == 7 || tieBreakPoints == 10) {
							break;
						}
					} else {
						scanner.nextLine();
					}
					System.out.println("잘못된 입력!!! 7 또는 10을 입력하세요.");
				}
			} else {
				// 미적용이면 tieBreakPoints는 의미 없으니 그대로 둠(기본 7 등)
			}

			boolean isDoubles = gameMode.equalsIgnoreCase("복식");
			boolean isMale = gender.equalsIgnoreCase("남자");

			Player player1 = new Player(player1Name);
			Player player2 = new Player(player2Name);

			scoreboard = new ScoreBoard(player1, player2, totalSets,
					isDoubles, isMale,
					tieBreakMode, tieBreakPoints);
		}

		while (true) {
			System.out.println("\n선택옵션: [1] Press \"Enter\" = 경기진행 [2] Press '2' = 게임저장 [3] Press '3' = 나가기");
			System.out.print("선택옵션 : ");
			String option = scanner.nextLine();

			switch (option) {
			case "1":
			case "": // enter
				int winner = new Random().nextInt(2) + 1;
				scoreboard.pointScored(winner);
				break;
			case "2":
				try {
					Save.saveGame(scoreboard);
					System.out.println("저장완료!");
				} catch (IOException e) {
					System.out.println("저장실패: " + e.getMessage());
				}
				break;
			case "3":
				System.out.println("나가기!");
				return;
			default:
				System.out.println("올바른값이 아닙니다!");
			}
		}
	}
}
