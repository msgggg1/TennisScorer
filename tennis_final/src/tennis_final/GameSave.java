package tennis_final;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


class GameSave {
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
