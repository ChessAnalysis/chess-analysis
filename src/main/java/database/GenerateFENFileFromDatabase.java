package database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.common.io.Files;

import config.ConfigSQL;

public class GenerateFENFileFromDatabase {
	
	private Connection connexion;
	private static int count = 0;

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		new GenerateFENFileFromDatabase(new ConfigSQL("diverse"));
	}
	
	public GenerateFENFileFromDatabase(ConfigSQL config) throws ClassNotFoundException, SQLException, IOException {
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(false);
		
		Statement st = connexion.createStatement();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 10 ; i++) {
			ResultSet rs = st.executeQuery("select id from FEN LIMIT " + 100 + " OFFSET " + (i*100));
			while (rs.next()) {
				sb.append(rs.getString(1) + "\n");
			}
			Files.append(sb, new File(System.getProperty("user.home") + "/fen/" + i), Charset.defaultCharset());
			sb.setLength(0);
		}
	}
}
