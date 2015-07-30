package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import jline.internal.Log;
import config.ConfigSQL;

/**
 */
public class InsertECOToDatabase {

	private Connection connexion;
	private int row = 0;
	private static PreparedStatement insertOpening = null;

	/**
	 * Constructor for InsertECOToDatabase.
	 * @param connexion ConfigSQL
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public InsertECOToDatabase(ConfigSQL connexion) throws ClassNotFoundException, SQLException, IOException {
		Class.forName(connexion.getDriver());
		this.connexion = DriverManager.getConnection(connexion.getUrl() + connexion.getDb() + "?user=" + connexion.getUser() + "&password=" + connexion.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(false);
		init();
	}

	/**
	 * Method init.
	 * @throws SQLException
	 * @throws IOException
	 */
	public void init() throws SQLException, IOException {

		insertOpening = connexion.prepareStatement("INSERT INTO Opening (eco, opening, variation, moves, nbMoves) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

		row++;
		insertOpening.setString(1, "Unknown");
		insertOpening.setString(2, "Unknown");
		insertOpening.setString(3, "Unknown");
		insertOpening.setString(4, "");
		insertOpening.setInt(5, 0);
		insertOpening.addBatch();
		
		BufferedReader br = new BufferedReader(new FileReader("lib/pgn-extract/eco.pgn"));

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();

			String[] openings = everything.split("\\*");

			for(String opening : openings) {
				BufferedReader br2 = new BufferedReader(new StringReader(opening));
				StringBuilder movesSAN = new StringBuilder();
				String openingEco = "";
				String openingName = "";
				String openingVariation = "";

				try {
					String line2;

					while ((line2 = br2.readLine()) != null) {
						//System.out.println(line2);
						line2 = line2.trim();

						if (line2.startsWith("[")) {
							String tagName = line2.substring(1, line2.indexOf(" "));
							String tagValue = line2.substring(line2.indexOf("\"") + 1,
									line2.lastIndexOf("\""));
							if(tagValue.length()>0) {
								switch (tagName) {
								case "ECO": openingEco = tagValue; break;
								case "Opening": openingName = tagValue; break;
								case "Variation": openingVariation = tagValue; break;
								}
							}
						} else {
							if (!line2.isEmpty()) {
								movesSAN.append(line2 + " ");
							}
						}
					}

				} finally {
					br2.close();
				}
				row++;
				insertOpening.setString(1, openingEco);
				insertOpening.setString(2, openingName);
				insertOpening.setString(3, openingVariation);
				insertOpening.setString(4, movesSAN.toString());
				insertOpening.setInt(5, wordcount(movesSAN.toString().replaceAll("\\d+\\.", "")));
				insertOpening.addBatch();
			}

		} finally {
			br.close();
		}
		
		insertOpening.executeBatch();
		connexion.commit();
		
		Log.info(row + " openings insérés dans la base de données.");
	}

	/**
	 * Method wordcount.
	 * @param s String
	 * @return int
	 */
	static int wordcount(String s)
	{
		int c = 0;
		char ch[]= new char[s.length()];      //in string especially we have to mention the () after length
		for(int i=0;i<s.length();i++)
		{
			ch[i]= s.charAt(i);
			if( ((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' ')) || ((ch[0]!=' ')&&(i==0)) )
				c++;
		}
		return c;
	}

}
