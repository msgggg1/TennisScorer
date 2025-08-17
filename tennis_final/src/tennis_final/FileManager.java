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
    
    // 전체 게임 히스토리 저장 (경기 종료 시)
    public void writeCompleteGameHistory() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("=== 전체 게임 히스토리 ===\n");
    	
    	// 각 세트별 게임 기록
    	for (int i = 0; i < board.gameHistoryList.get(0).size(); i++) {
    		sb.append(String.format("게임 %d: %s %d - %s %d\n", 
    			i + 1, 
    			board.team1.getName(), 
    			board.gameHistoryList.get(0).get(i),
    			board.team2.getName(), 
    			board.gameHistoryList.get(1).get(i)));
    	}
    	sb.append("\n");
        writeToFile(sb.toString());
    }
    
    // 전체 요약보고서 저장 (경기 종료 시)
    public void writeCompleteSummary() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("=== 경기 전체 요약보고서 ===\n");
    	sb.append(String.format("경기 날짜: %s\n", d));
    	sb.append(String.format("경기 유형: %s %s\n", board.isMale ? "남자" : "여자", board.isDoubles ? "복식" : "단식"));
    	sb.append(String.format("총 세트 수: %d\n", board.totalSets));
    	sb.append(String.format("타이브레이크: %s\n", 
    		board.tieBreakMode == 0 ? "미적용" : 
    		board.tieBreakMode == 1 ? "전체 세트" : "마지막 세트만"));
    	if (board.tieBreakMode != 0) {
    		sb.append(String.format("타이브레이크 포인트: %d\n", board.tieBreakPoints));
    	}
    	sb.append(String.format("노 애드 규칙: %s\n", board.noAd ? "적용" : "미적용"));
    	sb.append(String.format("최종 결과: %s %d세트 - %s %d세트\n", 
    		board.team1.getName(), board.team1.getSetsWon(),
    		board.team2.getName(), board.team2.getSetsWon()));
    	sb.append(String.format("승자: %s\n", 
    		board.team1.getSetsWon() > board.team2.getSetsWon() ? board.team1.getName() : board.team2.getName()));
    	sb.append("========================\n\n");
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
