package tennis_final;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;

public class FileManager {
	String pathname;
    File saveFile;
    ScoreBoard board;
    LocalDate d = LocalDate.now();
	
    // 생성자
    FileManager(ScoreBoard scoreBoard) {
    	//현재 자바 프로그램이 실행되고 있는 디렉토리 경로(working directory)
		String parent = System.getProperty("user.dir"); 
		this.pathname = scoreBoard.team1.getName() + " vs " +  scoreBoard.team2.getName() + " (" + d + ").txt";
		this.saveFile = new File(parent, this.pathname);
		this.board = scoreBoard;
    }
    
    // 경기 정보 기록
    public void writeMatchInfo() {
    	writeToFile(String.format("%s 경기 (%s vs %s) %s %s \n ( %s포인트 %s , %s)", d, board.team1.getName(), board.team2.getName()
    			, board.isMale? "남자" : "여자", board.isDoubles?"복식":"단식", board.tieBreakPoints == 0 ? "" : String.valueOf(board.tieBreakPoints)
    			, board.tieBreakMode==0 ? "타이브레이크 미적용": (board.tieBreakMode==1 ? "타이브레이크": "마지막 세트 타이브레이크"), board.noAd ? "": "노 애드(no-Ad)경기"));
    }
    // 게임 기록 저장
    public void writeGameHistroy() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(String.format("[%d Set] |Set Score| %d : %d  |Game Score| %d : %d\n", board.totalSets, board.team1.getSetsWon(), board.team2.getSetsWon()
    			,board.team1.getGamesWon(), board.team2.getGamesWon()));
    	
    	appendScoreRecord(sb, board.team1.getName(), 0, board.tieBreak);
    	appendScoreRecord(sb, board.team2.getName(), 1, board.tieBreak);
    	
    	sb.append("\n\n");
        writeToFile(sb.toString());
    }
    
    // 최종 결과 저장
    public void writeFinalResult() {
    	String finalResult = String.format("최종 세트 스코어 \n %s\t\t\t[%d] \n %s\t\t\t[%d]\n", board.team1.getName(),
                board.team1.getSetsWon(), board.team2.getName(), board.team2.getSetsWon());
        writeToFile(finalResult);
    }
    
    // 점수 기록 메소드
    private void appendScoreRecord(StringBuilder sb, String teamName, int teamIndex, boolean tieBreak) {
		sb.append(String.format("%-12s", teamName));
		for (int i = 0; i < board.gameHistoryList.get(teamIndex).size(); i++) {
	        int score = board.gameHistoryList.get(teamIndex).get(i);
	        sb.append(String.format("%3s\t", String.valueOf(board.convertPoints(score))));
	        
	        //	        if (!tieBreak) {
//	            sb.append(String.format("%3s\t", String.valueOf(board.convertPoints(score))));
//	        } else {
//	            sb.append(String.format("%3d\t", score));
//	        }
	    }
	    sb.append("\n");
	}
    
    // 파일 작성 메소드
	private void writeToFile(String content) {
    	try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile, true));){
			bw.append(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
