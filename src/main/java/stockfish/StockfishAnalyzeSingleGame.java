package stockfish;

import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.Move;
import ictk.boardgame.chess.AmbiguousChessMoveException;
import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessMove;
import ictk.boardgame.chess.io.FEN;
import ictk.boardgame.chess.io.SAN;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.google.common.io.Files;

import jline.internal.Log;
import engine.Engine;
import engine.EngineFactory;
import engine.EnginePreferences;

public class StockfishAnalyzeSingleGame {

	private static final String STOCKFISH_MAC = "lib/uci-engine/stockfish-6-mac/Mac/stockfish-6-64";
	private static EnginePreferences prefs = new EnginePreferences();
	private static int count = 0;
	private static Engine engine;

	public static void main(String[] args) throws AmbiguousChessMoveException, IllegalMoveException, ClassNotFoundException, InterruptedException, SQLException, IOException {
		new StockfishAnalyzeSingleGame();
	}

	public StockfishAnalyzeSingleGame() throws InterruptedException, ClassNotFoundException, SQLException, AmbiguousChessMoveException, IllegalMoveException, IOException {

		prefs.setOption("multipv", "3");
		prefs.setOption("Threads", "6");
		prefs.setDepth(18);

		engine = EngineFactory.getInstance().createEngine(STOCKFISH_MAC, prefs);

		Log.info("Début de l'analyse dynamique avec Stockfish");
		long startTimeParsed = System.nanoTime();
		initBase2();
		Log.info("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
	}

	public void initBase2() throws InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException, IOException {

		String moves = "1. Nf3 Nf6 2. c4 g6 3. Nc3 Bg7 4. d4 O-O 5. Bf4 d5 6. Qb3 dxc4 7. Qxc4 c6 8. e4 Nbd7 9. Rd1 Nb6 10. Qc5 Bg4 11. Bg5 Na4 12. Qa3 Nxc3 13. bxc3 Nxe4 14. Bxe7 Qb6 15. Bc4 Nxc3 16. Bc5 Rfe8+ 17. Kf1 Be6 18. Bxb6 Bxc4+ 19. Kg1 Ne2+ 20. Kf1 Nxd4+ 21. Kg1 Ne2+ 22. Kf1 Nc3+ 23. Kg1 axb6 24. Qb4 Ra4 25. Qxb6 Nxd1 26. h3 Rxa2 27. Kh2 Nxf2 28. Re1 Rxe1 29. Qd8+ Bf8 30. Nxe1 Bd5 31. Nf3 Ne4 32. Qb8 b5 33. h4 h5 34. Ne5 Kg7 35. Kg1 Bc5+ 36. Kf1 Ng3+ 37. Ke1 Bb4+ 38. Kd1 Bb3+ 39. Kc1 Ne2+ 40. Kb1 Nc3+ 41. Kc1 Rc2# 0-1";
		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;

		StringTokenizer stoken = null;
		String token;
		String currentFEN;

		board = new ChessBoard();

		stoken = new StringTokenizer(moves);
		
		StringBuilder sb = new StringBuilder();

		while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
			token = stoken.nextToken();
			if(!token.contains(".")) {
				move = (ChessMove) san.stringToMove(board, token);
				board.playMove(move);
				currentFEN = fen.boardToString(board);
				sb.append(currentFEN + "\n");
				sb.append(engine.computeScore(currentFEN));
				sb.append("---\n");
				count++;
			}
		}
		
		Files.append(sb, new File(System.getProperty("user.home") + "/log"), Charset.defaultCharset());
		

	}
	
	public void initBase() throws InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {

		String moves = "1. Nf3 Nf6 2. c4 g6 3. Nc3 Bg7 4. d4 O-O 5. Bf4 d5 6. Qb3 dxc4 7. Qxc4 c6 8. e4 Nbd7 9. Rd1 Nb6 10. Qc5 Bg4 11. Bg5 Na4 12. Qa3 Nxc3 13. bxc3 Nxe4 14. Bxe7 Qb6 15. Bc4 Nxc3 16. Bc5 Rfe8+ 17. Kf1 Be6 18. Bxb6 Bxc4+ 19. Kg1 Ne2+ 20. Kf1 Nxd4+ 21. Kg1 Ne2+ 22. Kf1 Nc3+ 23. Kg1 axb6 24. Qb4 Ra4 25. Qxb6 Nxd1 26. h3 Rxa2 27. Kh2 Nxf2 28. Re1 Rxe1 29. Qd8+ Bf8 30. Nxe1 Bd5 31. Nf3 Ne4 32. Qb8 b5 33. h4 h5 34. Ne5 Kg7 35. Kg1 Bc5+ 36. Kf1 Ng3+ 37. Ke1 Bb4+ 38. Kd1 Bb3+ 39. Kc1 Ne2+ 40. Kb1 Nc3+ 41. Kc1 Rc2# 0-1";
		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;

		StringTokenizer stoken = null;
		String token;
		String currentFEN;

		board = new ChessBoard();

		stoken = new StringTokenizer(moves);

		Log.debug("[cnt] \twhite\tblack\tscore\teval");
		Integer previous = 0;

		while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
			token = stoken.nextToken();
			if(!token.contains(".")) {
				move = (ChessMove) san.stringToMove(board, token);
				board.playMove(move);
				currentFEN = fen.boardToString(board);
				System.out.println(currentFEN);
				String result = engine.computeScoreFast(currentFEN);

				if(result == null) {
					break;
				}
				if (result.contains("#")) {
					if((count%2)==0)
						Log.debug("[" + ((int)(count/2)+1) + "] \t" + token + "\t\t" + result + "\t");
					else
						Log.debug("[" + ((int)(count/2)+1) + "] \t\t" + token + "\t" + result + "\t");
					previous = null;
				} else {
					Integer score = Integer.valueOf(result);
					if((count%2)==0)
						score = -score;

					int eval;
					if(previous == null)
						eval = 0;
					else if(score<previous)
						eval = score-previous;
					else
						eval = previous-score;
					previous = score;

					if((count%2)==0)
						Log.debug("[" + ((int)(count/2)+1) + "] \t" + token + "\t\t" + Double.valueOf(score)/100 + "\t" + Double.valueOf(eval)/100);
					else
						Log.debug("[" + ((int)(count/2)+1) + "] \t\t" + token + "\t" + Double.valueOf(score)/100 + "\t" + Double.valueOf(eval)/100);
				}
				count++;
				System.out.println("---");
			}

		}

	}

}


