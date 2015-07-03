package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jline.internal.Log;
import config.ConfigSQL;


public class InsertPGNToDatabase {

	private static final String PATH = "";
	private static SimpleDateFormat formatterDay = new SimpleDateFormat("yyyy.MM.dd");
	private static SimpleDateFormat formatterMonth = new SimpleDateFormat("yyyy.MM");
	private static SimpleDateFormat formatterYear = new SimpleDateFormat("yyyy");
	private static int count = 0;

	private String filename;

	private Connection connexion;
	private Statement st = null;
	private PreparedStatement selectEvent = null, insertEvent = null;
	private PreparedStatement selectPlayer = null, insertPlayer = null;
	private PreparedStatement insertGame = null;
	private ResultSet rs = null;

	static int idEvent = 3;
	static int idPlayer = 3;


	private static HashMap<String, Integer> players = new HashMap<String, Integer>();
	private static HashMap<String, Integer> events = new HashMap<String, Integer>();
	private static HashMap<String, Integer> openings = new HashMap<String, Integer>();

	public InsertPGNToDatabase(String filename, ConfigSQL connexion) throws ClassNotFoundException, SQLException {
		this.filename = filename;
		Class.forName(connexion.getDriver());
		this.connexion = DriverManager.getConnection(connexion.getUrl() + connexion.getDb() + "?user=" + connexion.getUser() + "&password=" + connexion.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(false);
		parse();
	}

	public void parse() {
		File file = new File(PATH + filename);
		if(file.exists()) {
			long startTimeParsed = System.nanoTime();
			try {
				init(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.info("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		} else {
			Log.warn("Le fichier n'existe pas.");
		}
	}

	public void init(BufferedReader pgn) throws IOException, NullPointerException, ClassNotFoundException, SQLException {
		List<String> pgnSources = splitPGN(pgn);

		if (pgnSources == null || pgnSources.size() == 0) {
			Log.warn("Fichier vide");
			return;
		}

		Iterator<String> i = pgnSources.iterator();

		st = connexion.createStatement();
		ResultSet rs = st.executeQuery("SELECT id, name FROM Player");
		while (rs.next()) {
			players.put(rs.getString("name"), rs.getInt("id"));
		}

		rs = st.executeQuery("SELECT id, name, city FROM Event");
		while (rs.next()) {
			events.put(rs.getString("name")+"/"+rs.getString("city"), rs.getInt("id"));
		}

		rs = st.executeQuery("SELECT id, eco, opening, variation FROM Opening");
		while(rs.next()) {
			openings.put(rs.getString("eco")+"/"+rs.getString("opening")+"/"+rs.getString("variation"), rs.getInt("id"));
		}

		selectEvent = connexion.prepareStatement("SELECT id FROM Event WHERE name = ? AND city = ? LIMIT 1");
		insertEvent = connexion.prepareStatement("INSERT INTO Event (name, city, id) VALUES (?,?,?)");

		selectPlayer = connexion.prepareStatement("SELECT id FROM Player WHERE name = ? LIMIT 1");
		insertPlayer = connexion.prepareStatement("INSERT INTO Player (name, id) VALUES (?,?)");

		insertGame = connexion.prepareStatement("INSERT INTO Game (eventId, whiteId, blackId, ecoId, whiteElo, blackElo, date, round, result, totalPlyCount, movesSAN, movesUCI) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		while (i.hasNext()) {
			try {
				parseGame(i.next(), players, events);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		insertEvent.executeBatch();
		Log.info("Events inserted !");
		insertPlayer.executeBatch();
		Log.info("Players inserted !");
		connexion.commit();
		insertGame.executeBatch();
		Log.info("Games inserted !");
		connexion.commit();

		Log.info(count + " inserted games");
	}

	private void parseGame(String pgn, HashMap<String, Integer> players, HashMap<String, Integer> events) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(pgn));
		String line;


		String eventName = "";
		String eventCity = "";

		String openingEco = "";
		String openingName = "";
		String openingVariation = "";

		int eventId = 1;
		int whiteId = 1;
		int blackId = 1;
		int ecoId = 1;
		int whiteElo = 0;
		int blackElo = 0;
		Date date = null;
		java.sql.Date dateSql = null;
		String round = "";
		int result = 9;
		int totalPlyCount = 0;
		StringBuilder movesSAN = new StringBuilder();
		StringBuilder movesUCI = new StringBuilder();

		try {
			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.startsWith("[")) {
					try {
						String tagName = line.substring(1, line.indexOf(" "));
						String tagValue = line.substring(line.indexOf("\"") + 1,
								line.lastIndexOf("\""));
						if(tagValue.length()>0) {
							switch (tagName) {
							case "Event":	eventName = tagValue; break;
							case "Site" :	eventCity = tagValue;  break;
							case "Date" :	date = getDate(tagValue); break;
							case "Round":	round = tagValue; break;
							case "White":	whiteId = getPlayer(tagValue, players); break;
							case "Black":  	blackId = getPlayer(tagValue, players); break;
							case "TotalPlyCount": try { totalPlyCount = Integer.parseInt(tagValue); break; } catch (Exception e) { totalPlyCount = -1; continue; }
							case "Result":
								switch (tagValue) {
								case "1-0": result = 0; break;
								case "0-1": result = 1; break;
								case "1/2-1/2": result = 2; break;
								case "*": result = 3; break;
								} break;
							case "WhiteElo": try { whiteElo = Integer.parseInt(tagValue); break; } catch (Exception e) { whiteElo = -1; continue; }
							case "BlackElo": try { blackElo = Integer.parseInt(tagValue); break; } catch (Exception e) { whiteElo = -1; continue; }
							case "ECO": openingEco = tagValue; break;
							case "Opening": openingName = tagValue; break;
							case "Variation": openingVariation = tagValue; break;
							}
						}
					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
						continue;
					}
				} else {
					if (!line.isEmpty()) {
						if(line.startsWith("-")) {
							movesSAN.append(line.substring(1) + " ");
						} else if (line.startsWith("+")) {
							movesUCI.append(line.substring(1) + " ");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			eventId = getEvent(eventName, eventCity, events);
			ecoId = getOpening(openingEco, openingName, openingVariation);
			dateSql = new java.sql.Date(date.getTime());
		} catch (Exception e) {
			dateSql = null;
		}

		if((++count %1000) == 0) {
			Log.info(count + " parsed games");
		}

		try {
			insertGame.setInt(1, eventId);
			insertGame.setInt(2, whiteId);
			insertGame.setInt(3, blackId);
			insertGame.setInt(4, ecoId);
			insertGame.setInt(5, whiteElo);
			insertGame.setInt(6, blackElo);
			insertGame.setDate(7, dateSql);
			insertGame.setString(8, round);
			insertGame.setInt(9, result);
			insertGame.setInt(10, totalPlyCount);
			insertGame.setString(11, movesSAN.toString());
			insertGame.setString(12, movesUCI.toString());
			insertGame.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private int getOpening(String openingEco, String openingName,
			String openingVariation) {
		if(openings.containsKey(openingEco+"/"+openingName+"/"+openingVariation))
			return openings.get(openingEco+"/"+openingName+"/"+openingVariation);
		return 1;
	}



	private Date getDate(String tagValue) throws ParseException {
		if(tagValue.contains("?")) {
			tagValue = tagValue.substring(0, 7);
			if(tagValue.contains("?")) {
				tagValue = tagValue.substring(0, 4);
				if(tagValue.contains("?")) {
					return null;
				} else {
					return formatterYear.parse(tagValue);
				}
			} else {
				return formatterMonth.parse(tagValue);
			}
		} else {
			try {
				return formatterDay.parse(tagValue);
			} catch (ParseException e) {
				return null;
			}
		}
	}



	private int getEvent(String eventName, String eventCity, HashMap<String, Integer> events) throws SQLException {

		if(events.containsKey(eventName+"/"+eventCity))
			return events.get(eventName+"/"+eventCity);
		else {
			insertEvent.setString(1, eventName);
			insertEvent.setString(2, eventCity);
			insertEvent.setInt(3, ++idEvent);
			insertEvent.addBatch();
			events.put(eventName+"/"+eventCity, idEvent);
			return idEvent;
		}
	}

	private int getPlayer(String name, HashMap<String, Integer> players) throws SQLException {
		if(players.containsKey(name)) {
			return players.get(name);
		}
		else {
			insertPlayer.setString(1, name);
			insertPlayer.setInt(2, ++idPlayer);
			insertPlayer.addBatch();
			players.put(name, idPlayer);
			return idPlayer;
		}
	}

	/**
	 * 
	 * @param pgn
	 * @return
	 * @throws IOException
	 */
	private static List<String> splitPGN(BufferedReader br) throws IOException {
		List<String> pgnGames = new LinkedList<String>();

		StringBuilder buffer = new StringBuilder();

		for (String line = null; (line = br.readLine()) != null;) {
			//br.readLine();
			//br.readLine();
			//br.readLine();

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (!line.isEmpty()) {
					buffer.append(line + "\r\n");

					if (line.startsWith("+")) {
						pgnGames.add(buffer.toString());
						buffer.delete(0, buffer.length());
					}
				}

			}
		}
		br.close();

		return pgnGames;
	}

}

