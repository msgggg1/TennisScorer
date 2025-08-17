package tennis_final;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

//타이브레이크 선택(x,전체 세트, 마지막 세트만), 타이브레이크 점수 선택 추가, no-Ad 규칙 추가
public class PlayTennis {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		ScoreBoard scoreboard = null;
			// 새로운 게임 설정
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
			
			boolean isDoubles = gameMode.equalsIgnoreCase("복식");
			
			Team team1;
			Team team2;
			if (!isDoubles) {
				System.out.print("첫번째 플레이어 이름: ");
				String player1Name = scanner.nextLine();
				Player p1 = new Player(player1Name);
				
				System.out.print("두번째 플레이어 이름: ");
				String player2Name = scanner.nextLine();
				Player p2 = new Player(player2Name);
				
				ArrayList<Player> t1 = new ArrayList<Player>();
				t1.add(p1);
				team1 = new Team(t1);
				
				ArrayList<Player> t2 = new ArrayList<Player>();
				t2.add(p2);
				team2 = new Team(t2);
				
			} else {
				System.out.print("팀1 -첫번째 플레이어 이름: ");
				String player1Name = scanner.nextLine();
				Player p1 = new Player(player1Name);
				
				System.out.print("팀1 -두번째 플레이어 이름: ");
				String player2Name = scanner.nextLine();
				Player p2 = new Player(player2Name);
				
				System.out.print("팀2 -첫번째 플레이어 이름: ");
				String player3Name = scanner.nextLine();
				Player p3 = new Player(player3Name);
				
				System.out.print("팀2 -두번째 플레이어 이름: ");
				String player4Name = scanner.nextLine();
				Player p4 = new Player(player4Name);
				
				ArrayList<Player> t1 = new ArrayList<Player>();
				t1.add(p1);
				t1.add(p2);
				team1 = new Team(t1);
				ArrayList<Player> t2 = new ArrayList<Player>();
				t2.add(p3);
				t2.add(p4);
				team2 = new Team(t2);
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
			int tieBreakPoints = 0; // 기본값
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
			
			// ★ No-Ad 규칙 선택 추가
			boolean noAd = false;
			while (true) {
			    System.out.print("노애드(No-Ad) 규칙을 적용하시겠습니까? (네/아니오): ");
			    String noAdOption = scanner.nextLine().toLowerCase();
			    if (noAdOption.equals("네")) {
			        noAd = true;
			        break;
			    } else if (noAdOption.equals("아니오")) {
			        noAd = false;
			        break;
			    }
			    System.out.println("잘못된 입력!!! '네' or '아니오'.");
			}

			boolean isMale = gender.equalsIgnoreCase("남자");

			scoreboard = new ScoreBoard(team1, team2, totalSets,
					isDoubles, isMale,
					tieBreakMode, tieBreakPoints, noAd);
		

		while (true) {
			System.out.println("\n선택옵션: [1] Press \"Enter\" = 경기진행 [2] Press '3' = 나가기");
			System.out.print("선택옵션 : ");
			String option = scanner.nextLine();

			switch (option) {
			case "1":
			case "": // enter
				int winner = new Random().nextInt(2) + 1;
				scoreboard.pointScored(winner);
				break;
			case "3":
				System.out.println("나가기!");
				return;
			default:
				System.out.println("올바른값이 아닙니다!");
			}
		}
	} // main
} // playTennis

