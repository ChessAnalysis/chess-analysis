package database;

import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.Move;
import ictk.boardgame.chess.AmbiguousChessMoveException;
import ictk.boardgame.chess.ChessBoard;
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


/**
 */
public class GenerateFENFromDatabase {
	
	private Connection connexion;
	private static int count = 0;

	/**
	 * Constructor for GenerateFENFromDatabase.
	 * @param config ConfigSQL
	 * @param i int
	 * @param fenMap Map<String,Integer>
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws AmbiguousChessMoveException
	 * @throws IllegalMoveException
	 */
	public GenerateFENFromDatabase(ConfigSQL config, int i) throws IOException, InterruptedException, ClassNotFoundException, SQLException, AmbiguousChessMoveException, IllegalMoveException {
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(true);
		Statement stmt = connexion.createStatement();
		stmt.execute("ALTER TABLE Move ENABLE KEYS");
		stmt.execute("SET GLOBAL FOREIGN_KEY_CHECKS=1");
		stmt.close();
		//initB();
	}

	/**
	 * Method init.
	 * @param i int
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws AmbiguousChessMoveException
	 * @throws IllegalMoveException
	 */
	public void init(int i) throws IOException, InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {

		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;
		
		PreparedStatement insertMove = connexion.prepareStatement("INSERT INTO Move (idGame, halfMove, move, idFEN) VALUES (?, ?, ?, ?)");
		
		Statement st = connexion.createStatement();
		
		StringTokenizer stoken = null;
		
		long startTimeParsed = System.nanoTime();
		
		int halfMove = 0;
		String moves;
		String token;
		String currentFEN;
		
		ResultSet rs = st.executeQuery("select id, movesSAN from Game LIMIT " + 50000 + " OFFSET " + (i*50000));
		while (rs.next()) {
			count++;
			if((count%10000)==0) {
				System.out.println(count + "...");
				insertMove.executeBatch();
			}
			board = new ChessBoard();
			
			halfMove = 0;
			moves = rs.getString(2);

			stoken = new StringTokenizer(moves);
			while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
				
				token = stoken.nextToken();
				if(!token.contains(".")) {
					move = san.stringToMove(board, token);
					board.playMove(move);
					currentFEN = fen.boardToString(board);
					
					insertMove.setInt(1, rs.getInt(1));
					insertMove.setInt(2, halfMove++);
					insertMove.setString(3, token);
					insertMove.setString(4, currentFEN);
					
					insertMove.addBatch();
				}
			}
		}
		
		System.out.println("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
		startTimeParsed = System.nanoTime();
		insertMove.executeBatch();
		insertMove.close();
		connexion.close();
		
		System.out.println("Inserted in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
	}
	
	public void initB() throws IOException, InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {

		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;
		
		PreparedStatement insertFEN = connexion.prepareStatement("INSERT IGNORE INTO FEN (id) VALUES (?)");
		
		PreparedStatement insertMove = connexion.prepareStatement("INSERT INTO Move (idGame, halfMove, move, idFEN) VALUES (?, ?, ?, ?)");
		
		Statement st = connexion.createStatement();
		
		StringTokenizer stoken = null;
		
		long startTimeParsed = System.nanoTime();
		
		int halfMove = 0;
		String moves;
		String token;
		String currentFEN;
		
		ResultSet rs = st.executeQuery("select id, movesSAN from Game");
		while (rs.next()) {
			count++;
			if((count%10000)==0) {
				System.out.println(count + "...");
				insertMove.executeBatch();
			}
			board = new ChessBoard();
			
			halfMove = 0;
			moves = rs.getString(2);

			stoken = new StringTokenizer(moves);
			while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
				
				token = stoken.nextToken();
				if(!token.contains(".")) {
					move = san.stringToMove(board, token);
					board.playMove(move);
					currentFEN = fen.boardToString(board);
					insertFEN.setString(1, currentFEN);
					insertFEN.addBatch();
					insertFEN.executeBatch();
					
					insertMove.setInt(1, rs.getInt(1));
					insertMove.setInt(2, halfMove++);
					insertMove.setString(3, token);
					insertMove.setString(4, currentFEN);
					
					insertMove.addBatch();
				}
			}
		}
		
		System.out.println("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
		startTimeParsed = System.nanoTime();
		insertMove.executeBatch();
		insertMove.close();
		connexion.close();
		
		System.out.println("Inserted in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
	}

}