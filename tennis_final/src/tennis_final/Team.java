package tennis_final;

import java.util.ArrayList;
						//Team class for managing team information
public class Team {
	//Team class for managing team information
	private String teamName;
	private int points;
	private int gamesWon;
	private int setsWon;	//private -> 객체 지향, 외부 접근 막음
	
	ArrayList<Player> players = new ArrayList<>();

	public Team(ArrayList<Player> players) {
		String name;
		
		if (players.size() == 1) {
			name = players.get(0).getPlayerName();
		} else {
			name = players.get(0).getPlayerName() + "," + players.get(1).getPlayerName();
		} //else
		
		this.teamName = name;
		this.points = 0;
		this.gamesWon = 0;
		this.setsWon = 0;
	}

	public String getName() { return teamName; }
	public int getPoints() { return points; }
	public int getGamesWon() { return gamesWon; }
	public int getSetsWon() { return setsWon; }

	public void addPoint() { points++; }                    // 득점                                   
	public void winGame() {  gamesWon++; points = 0;}       // 게임승리 시 , 게임점수++, 포인트 0점 초기화          
	public void winSet() { setsWon++; gamesWon = 0; }       // 세트승리 시 , 세트점수++, 게임 스코어 0점 초기화       
	public void resetPoints() {points = 0;}                 // 승리하지 못하고 점수 초기화 필요 시                 
	public void resetGames() { this.gamesWon = 0; }         // 세트 종료 시 양쪽 플레이어를 모두 0으로 만들기 위해 사용    
}