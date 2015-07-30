package database;

import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.chess.AmbiguousChessMoveException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import jline.internal.Log;
import config.ConfigSQL;

public class UpdateFENFromFile {
	
	private Connection connexion;
	
	public UpdateFENFromFile(ConfigSQL config) throws IOException, InterruptedException, ClassNotFoundException, SQLException, AmbiguousChessMoveException, IllegalMoveException {
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(true);
		init();
	}
	
	public void init() throws IOException, InterruptedException, SQLException, AmbiguousChessMoveException, IllegalMoveException {
		
		PreparedStatement updateFEN = connexion.prepareStatement("UPDATE FEN set id = ?, log = ? WHERE id = ?");
		
		InputStream is = new FileInputStream(new File("resources/9999"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			String[] split = line.split("\t");
			String currentFEN = split[0];
			String log = split[1];
			updateFEN.setString(1, currentFEN);
			updateFEN.setString(2, log);
			updateFEN.setString(3, currentFEN);
			updateFEN.addBatch();
		}
		
		updateFEN.executeBatch();
		
	}

}
