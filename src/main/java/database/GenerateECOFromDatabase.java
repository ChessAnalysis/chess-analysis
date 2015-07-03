package database;

import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.Move;
import ictk.boardgame.chess.AmbiguousChessMoveException;
import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessMove;
import ictk.boardgame.chess.io.FEN;
import ictk.boardgame.chess.io.SAN;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringTokenizer;

import config.ConfigSQL;


public class GenerateECOFromDatabase {
	
	private Connection connexion;
	private int count = 0;

	public GenerateECOFromDatabase(ConfigSQL config) throws IOException, InterruptedException, ClassNotFoundException, SQLException, AmbiguousChessMoveException, IllegalMoveException {
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(false);
		init();
	}

	public void init() throws IOException, InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {

		
		
		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;
		
		PreparedStatement selectFEN = connexion.prepareStatement("SELECT id FROM FEN WHERE fen = ? LIMIT 1");
		PreparedStatement insertMove = connexion.prepareStatement("INSERT INTO MoveECO (idECO, halfMove, move, idFEN) VALUES (?, ?, ?, ?)");
		
		Statement st = connexion.createStatement();
		
		StringTokenizer stoken = null;
		
		long startTimeParsed = System.nanoTime();
		
		int halfMove = 0;
		String moves;
		String token;
		String currentFEN;
		
		ResultSet rs = st.executeQuery("select id, moves from Opening");
		while (rs.next()) {
			count++;
			System.out.println(count + "...");

			board = new ChessBoard();
			
			halfMove = 0;
			moves = rs.getString(2);

			stoken = new StringTokenizer(moves);
			while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
				
				token = stoken.nextToken();
				if(!token.contains(".")) {
					move = (ChessMove) san.stringToMove(board, token);
					board.playMove(move);
					insertMove.setInt(2, halfMove++);
					insertMove.setString(3, token);
					currentFEN = fen.boardToString(board);
					selectFEN.setString(1, currentFEN);
					ResultSet rs2 = selectFEN.executeQuery();
					while (rs2.next()) {
						insertMove.setInt(4, rs2.getInt(1));
					}
					insertMove.setInt(1, rs.getInt(1));
					insertMove.addBatch();
				}
			}
		}
		
		System.out.println("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
		startTimeParsed = System.nanoTime();
		selectFEN.executeBatch();
		selectFEN.close();
		insertMove.executeBatch();
		insertMove.close();
		connexion.commit();
		connexion.close();
		
		System.out.println("Inserted in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
	}

}