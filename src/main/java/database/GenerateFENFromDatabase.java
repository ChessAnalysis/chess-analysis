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


public class GenerateFENFromDatabase {
	
	private Map<String, Integer> fenMap;
	private Connection connexion;
	private static int count = 0;

	public GenerateFENFromDatabase(ConfigSQL config, int i, Map<String, Integer> fenMap) throws IOException, InterruptedException, ClassNotFoundException, SQLException, AmbiguousChessMoveException, IllegalMoveException {
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(false);
		this.fenMap = fenMap;
		init(i);
	}

	public void init(int i) throws IOException, InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {

		
		
		ChessBoard board;
		SAN san = new SAN();
		FEN fen = new FEN();
		Move move = null;
		
		PreparedStatement insertFEN = connexion.prepareStatement("INSERT INTO FEN (id, fen) VALUES (?, ?)");
		PreparedStatement insertMove = connexion.prepareStatement("INSERT INTO Move (idGame, halfMove, move, idFEN) VALUES (?, ?, ?, ?)");
		
		int currentID = 0;
		
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
			if((count%100)==0) {
				System.out.println(count + "...");
			}
			if((count%10000)==0) {
				System.out.println("insertions des parties parsées...");
				insertFEN.executeBatch();
				insertMove.executeBatch();
			}
			board = new ChessBoard();
			
			halfMove = 0;
			moves = rs.getString(2);

			stoken = new StringTokenizer(moves);
			while (stoken.hasMoreTokens() && stoken.countTokens() != 1) {
				insertMove.setInt(1, rs.getInt(1));
				token = stoken.nextToken();
				if(!token.contains(".")) {
					move = (ChessMove) san.stringToMove(board, token);
					board.playMove(move);
					insertMove.setInt(2, halfMove++);
					insertMove.setString(3, token);
					currentFEN = fen.boardToString(board);
					if(fenMap.containsKey(currentFEN)) {
						insertMove.setInt(4, (int)fenMap.get(currentFEN));
					} else {
						fenMap.put(currentFEN, ++currentID);
						insertFEN.setInt(1, currentID);
						insertFEN.setString(2, currentFEN);
						insertFEN.addBatch();
						insertMove.setInt(4, currentID);
					}
					
					
					insertMove.addBatch();
				}
			}
		}
		
		System.out.println("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
		startTimeParsed = System.nanoTime();
		insertFEN.executeBatch();
		insertFEN.close();
		insertMove.executeBatch();
		insertMove.close();
		connexion.commit();
		connexion.close();
		
		System.out.println("Inserted in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
	}

}