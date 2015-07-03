import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.chess.AmbiguousChessMoveException;

import java.io.IOException;
import java.sql.SQLException;

import stockfish.StockfishAnalyze;

public class MainIgrida {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException, AmbiguousChessMoveException, IllegalMoveException {
		if(args.length > 0) {
			new StockfishAnalyze(Integer.valueOf(args[0]));
		}
	}
}