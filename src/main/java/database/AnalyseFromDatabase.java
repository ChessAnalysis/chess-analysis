package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import stockfish.RowLog;
import stockfish.Move;
import jline.internal.Log;
import config.ConfigSQL;

public class AnalyseFromDatabase {
	
	private Connection connexion;
	
	public AnalyseFromDatabase() throws SQLException, ClassNotFoundException {
		init();
	}
	
	private void init() throws ClassNotFoundException, SQLException {
		ConfigSQL config= new ConfigSQL("localhost");
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		Statement stmt = connexion.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT id FROM Game limit 10 offset 0;");
		while(rs.next()) {
			int idGame = rs.getInt(1);
			analyseGame(idGame);
		}
	}

	private void analyseGame(int idGame) throws SQLException {
		PreparedStatement selectMoves = connexion.prepareStatement("SELECT Move.id, FEN.id, FEN.log FROM FEN, Move WHERE Move.idGame = '" + idGame + "' AND Move.idFEN = FEN.id ORDER BY Move.halfMove ASC");
		PreparedStatement updateFEN = connexion.prepareStatement("UPDATE FEN SET score = ? WHERE id = ?");
		PreparedStatement updateMove = connexion.prepareStatement("UPDATE Move SET evaluation = ? WHERE id = ?");
		
		int previous = 0;
		int count = 0;
		
		ResultSet rs = selectMoves.executeQuery();
		while(rs.next()) {
			int idMove = rs.getInt(1);
			String idFEN = rs.getString(2);
			String logFEN = rs.getString(3).split("\\.")[19];
			String[] splittedFEN = logFEN.split(" ");
			if(splittedFEN[9].equals("mate")) {
				
			}else{
				Integer eval = getEval(count, Integer.valueOf(splittedFEN[10]));
				Integer gain = eval - previous;
				previous = eval;
				
				updateFEN.setInt(1, eval);
				updateFEN.setString(2, idFEN);
				updateFEN.addBatch();
				
				updateMove.setInt(1, gain);
				updateMove.setInt(2, idMove);
				updateMove.addBatch();
			}
			count++;
		}
		updateMove.executeBatch();
		updateFEN.executeBatch();
	}
	
	private Integer getEval(int count, int scoreResult) {
		if((count%2)==0) {
			return -Integer.valueOf(scoreResult);
		} else {
			return Integer.valueOf(scoreResult);
		}
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		new AnalyseFromDatabase();
	}
	


}
