import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import jline.console.ConsoleReader;
import jline.internal.Log;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import config.ConfigSQL;


public class MainR {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		new MainR();
	}
	
	ConsoleReader reader = new ConsoleReader();
	PrintWriter out = new PrintWriter(reader.getOutput());

	public MainR() throws ClassNotFoundException, SQLException, IOException {
		
		out.print("Quel traitement ?\n[1] Generate CSV\n[2] Generate graphs\n");
		
		String line;
		
		while ((line = reader.readLine("> ")) != null) {
			switch(line) {
			case "1" : generateCSV(); out.print("Quel traitement ?\n[1] Generate CSV\n[2] Generate graphs\n"); break;
			case "2" : generateGraphs(); out.print("Quel traitement ?\n[1] Generate CSV\n[2] Generate graphs\n"); break;
			}
		}

	}
	
	Rengine rengine;

	private void generateGraphs() {
		Log.info("Connexion with R engine... RUNNING");
		Log.info(System.getProperty("java.library.path"));
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);

		if (!re.waitForR()) {
			Log.error("Cannot load R");
			return;
		}
		Log.info("Connexion with R engine... OK\n");
		
		re.eval("library(ggplot2)");
		re.eval("library(plyr)");

		re.eval("games = read.csv(\"/Users/fesnault/git/chess-analysis/resources/csv/database.csv\", head=TRUE, sep=\",\")");
		re.eval("clearGames = subset(games, whiteElo>1600 & blackElo>1600, result != \"-1\") ");
		re.eval("nbData = length(games[,1])");
		re.eval("nbClearData = length(clearGames[,1])");

		REXP nbData = re.eval("nbData");
		REXP nbClearData = re.eval("nbClearData");
		REXP moyPlyPerGame = re.eval("mean(games[,3])");
		REXP sumPlyGames = re.eval("sum(games[,3])");

		Log.info(nbData.asInt() + " parties dans le dataset");
		Log.info(moyPlyPerGame.asDouble() + " coups moyen joué par partie");
		Log.info(sumPlyGames.asInt() + " coups joués au total");
		
		REXP nbUniqueEloRating = new REXP();

		if(nbClearData.asInt() > 0) {
			re.eval("whiteElo = clearGames[,1]");
			re.eval("blackElo = clearGames[,2]");
			re.eval("moves = clearGames[,3]");
			re.eval("result = clearGames[,4]");
			re.eval("stringResult = revalue(factor(clearGames[,4]), c(\"0\"=\"White\", \"1\"=\"Black\", \"2\"=\"Draw\"))");

			re.eval("whiteWin = as.numeric(result == 0)");
			re.eval("winnerElo = c(1:nbClearData)");
			re.eval("higherEloWin = as.numeric((whiteElo > blackElo & result == 0) | (whiteElo < blackElo & result == 1))");
			re.eval("eloRating = c(clearGames[,1], clearGames[,2])");
			re.eval("nbUniqueEloRating = length(unique(eloRating))");

			nbUniqueEloRating = re.eval("nbUniqueEloRating");

			re.eval("for(i in 1:nbClearData) { if(result[i] == 0) { winnerElo[i] = whiteElo[i] } else if(result[i] == 1) { winnerElo[i] = blackElo[i] } else { winnerElo[i] = max(whiteElo[i],blackElo[i]) }}");

			if(nbUniqueEloRating.asInt() < 100) {
				Log.info("Les parties ne contiennent pas d'information sur le classement Elo des joueurs");
			} else {
				Log.info("Analyse des " + nbUniqueEloRating.asInt() + " elo ratings...");

				re.eval("differenceElo = abs(whiteElo-blackElo)");
				re.eval("df1 <- data.frame(differenceElo, moves)");
				re.eval("df2 <- subset(data.frame(differenceElo, higherEloWin, result), result != 2)");
				re.eval("df3 <- subset(data.frame(whiteElo, whiteWin, result), result != 2)");
				re.eval("df4 <- subset(data.frame(winnerElo, stringResult), stringResult != \"3\")");


				re.eval("p1 = qplot(eloRating, geom=\"histogram\", binwidth = 25) 			+ xlab(\"Elo Rating\") 				+ ylab(\"Count\") 							+ ggtitle(\"1. Distribution of Elo ratings\")			+ xlim(1600, 3000)");
				re.eval("p2 = qplot(differenceElo, geom=\"histogram\", binwidth=25) 		+ xlab(\"Difference in Elo Rating\") 	+ ylab(\"Count\") 							+ ggtitle(\"2. Difference Elo ratings\")				+ xlim(0, 1000)");
				re.eval("p3 = ggplot(df1, aes(differenceElo, moves)) 					+ xlab(\"Difference in Elo Rating\") 	+ ylab(\"Ply per Game\") 						+ ggtitle(\"3. Ply per Game\") 						+ stat_smooth()									+ xlim(0, 1000)");
				re.eval("p4 = ggplot(df2, aes(differenceElo, higherEloWin))				+ xlab(\"Difference in Elo Rating\") 	+ ylab(\"% Games win by Higher Elo Rating\") 	+ ggtitle(\"4. % Games win by Higher Elo Rating\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")	+ xlim(0, 1000)");
				re.eval("p5 = ggplot(df3, aes(whiteElo, whiteWin)) 						+ xlab(\"Elo Rating\") 				+ ylab(\"% Games win by White Player\") 		+ ggtitle(\"5. % Games win by White Player\") 		+ stat_smooth(method=\"glm\", family=\"binomial\")	+ xlim(1600, 2800)");
				re.eval("p6 = ggplot(df4, aes(winnerElo, fill = stringResult)) 			+ xlab(\"Average Elo Rating of Players\") + ylab(\"% Games Resulting in a Win\") 	+ ggtitle(\"6. % Games Resulting in a Win By Color\") + stat_density(aes(y = ..density..), position = \"fill\", color = \"grey\") + scale_fill_manual(values=c(\"#ffffff\", \"#000000\", \"#99CCFF\", \"#cccccc\")) + xlim(1600, 2800)");

			}
		}

		re.eval("whiteElo = games[,1]");
		re.eval("blackElo = games[,2]");
		re.eval("moves = games[,3]");
		re.eval("result = games[,4]");
		re.eval("date = as.numeric(as.character(games[,5]))");
		re.eval("nbUniqueDate = length(unique(date))");
		
		REXP date = re.eval("date");

		re.eval("whiteWin = as.numeric(result == 0)");
		re.eval("winnerElo = c(1:nbData)");
		re.eval("higherEloWin = as.numeric((whiteElo > blackElo & result == 0) | (whiteElo < blackElo & result == 1))");
		re.eval("stringResult = revalue(factor(games[,4]), c(\"0\"=\"White\", \"1\"=\"Black\", \"2\"=\"Draw\"))");
		re.eval("for(i in 1:nbData) { if(result[i] == 0) { winnerElo[i] = whiteElo[i] } else if(result[i] == 1) { winnerElo[i] = blackElo[i] } else { winnerElo[i] = max(whiteElo[i],blackElo[i]) } }");

		REXP nbUniqueDate = re.eval("nbUniqueDate");

		if(nbUniqueDate.asInt() < 10) {
			Log.info("Les parties ne sont pas assez espacées dans le temps pour faire une analyse selon la date");
		} else {
			Log.info("Analyse des " + nbUniqueDate.asInt() + " uniques dates...");

			re.eval("df5 <- data.frame(date, moves)  ");
			re.eval("df6 = subset(data.frame(date, whiteWin, result), result != 2)");
			re.eval("df7 <- subset(data.frame(date, stringResult), stringResult != \"3\")");
			re.eval("p7 = qplot(date, geom=\"histogram\", binwidth = 1) 	+ xlim(1850, 2015)	+ xlab(\"Year\") 	+ ylab(\"Count\") 						+ ggtitle(\"7. Distribution of Date Match\")");
			re.eval("p8 = ggplot(df5, aes(date, moves)) 					+ xlim(1950, 2015)	+ xlab(\"Date\") 	+ ylab(\"Ply per Game\") 					+ ggtitle(\"8. Ply per Game\") 					+ stat_smooth()");
			re.eval("p9 = ggplot(df6, aes(date, whiteWin)) 				+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"% Games win by White Player\") 	+ ggtitle(\"9. % Games win by White Player\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("p10 = ggplot(df7, aes(date, fill = stringResult)) 	+ xlim(1850, 2015)	+ stat_density(aes(y = ..density..), position = \"fill\", color = \"black\") + xlab(\"Date\") + ylab(\"% Games Resulting in a Win\") + ggtitle(\"10. % Games Resulting in a Win By Color\") + scale_fill_manual(values=c(\"#ffffff\", \"#000000\", \"#99CCFF\", \"#cccccc\"))");

			re.eval("firstMoveWhite = revalue(games[,6], c(\"a3\"=\"Other\", \"a4\"=\"Other\", \"b3\"=\"Other\", \"b4\"=\"Other\", \"c3\"=\"Other\", \"d3\"=\"Other\", \"e3\"=\"Other\", \"f4\"=\"Other\", \"g3\"=\"Other\", \"g4\"=\"Other\", \"h3\"=\"Other\", \"h4\"=\"Other\", \"Nc3\"=\"Other\", \"Nh3\"=\"Other\", \"h6\"=\"Other\", \"f3\"=\"Other\", \"Na3\"=\"Other\"))");
			//re.eval("#firstMoveBlack = revalue(games[,7], c(\"a3\"=\"Other\", \"a4\"=\"Other\", \"b3\"=\"Other\", \"b4\"=\"Other\", \"c3\"=\"Other\", \"d3\"=\"Other\", \"e3\"=\"Other\", \"f4\"=\"Other\", \"g3\"=\"Other\", \"g4\"=\"Other\", \"h3\"=\"Other\", \"h4\"=\"Other\", \"Nc3\"=\"Other\", \"Nh3\"=\"Other\"))");
			re.eval("checkMate = games[,8]");
			re.eval("capturedPiecesRate = as.numeric(games[,9]/moves)");
			re.eval("rookMoveRate = as.numeric(games[,10]/moves)");
			re.eval("knightMoveRate = as.numeric(games[,11]/moves)");
			re.eval("pawnMoveRate = as.numeric(games[,12]/moves)");
			re.eval("queenMoveRate = as.numeric(games[,13]/moves)");
			re.eval("promotedRate = as.numeric(games[,14])");
			re.eval("kingSideCastlingRate = as.numeric(games[,15])");
			re.eval("queenSideCastlingRate = as.numeric(games[,16])");
			re.eval("df11 <- data.frame(date, firstMoveWhite)");
			re.eval("p11 = ggplot(df11, aes(date, fill = firstMoveWhite)) 	+ xlim(1900, 2015)	+ stat_density(aes(y = ..density..), position = \"fill\", color = \"black\") + xlab(\"Date\") + ylab(\"Count\") + ggtitle(\"11. First Move White\")");
			re.eval("df12 <- data.frame(date, firstMoveWhite)");
			re.eval("p12 = ggplot(df11, aes(date, fill = firstMoveWhite)) 	+ xlim(1950, 2015)	+ stat_density(aes(y = ..density..), position = \"fill\", color = \"black\") + xlab(\"Date\") + ylab(\"Count\") + ggtitle(\"12. First Move White - Zoom\")");
			re.eval("df13 <- data.frame(date, checkMate)");
			re.eval("p13 = ggplot(df13, aes(date, checkMate)) 				+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"% Games win by check mate\") 	+ ggtitle(\"13. % Games win by check Mate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df14 <- data.frame(date, capturedPiecesRate)");
			re.eval("p14 = ggplot(df14, aes(date, capturedPiecesRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Ratio of Captured Pieces\") 	+ ggtitle(\"14. Ratio of Captured Pieces\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df15 <- data.frame(date, rookMoveRate)");
			re.eval("p15 = ggplot(df15, aes(date, rookMoveRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Rook Move Rate\") 	+ ggtitle(\"15. Rook Move Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df16 <- data.frame(date, knightMoveRate)");
			re.eval("p16 = ggplot(df16, aes(date, knightMoveRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Knight Move Rate\") 	+ ggtitle(\"16. Knight Move Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df17 <- data.frame(date, pawnMoveRate) ");
			re.eval("p17 = ggplot(df17, aes(date, pawnMoveRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Pawn Move Rate\") 	+ ggtitle(\"17. Pawn Move Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df18 <- data.frame(date, queenMoveRate) ");
			re.eval("p18 = ggplot(df18, aes(date, queenMoveRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Queen Move Rate\") 	+ ggtitle(\"18. Queen Move Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df19 <- data.frame(date, promotedRate) ");
			re.eval("p19 = ggplot(df19, aes(date, promotedRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Promoted Rate\") 	+ ggtitle(\"19. Promoted Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df20 <- data.frame(date, kingSideCastlingRate) ");
			re.eval("p20 = ggplot(df20, aes(date, kingSideCastlingRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"King Side Castling Rate\") 	+ ggtitle(\"20. King Side Castling Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");
			re.eval("df21 <- data.frame(date, queenSideCastlingRate) ");
			re.eval("p21 = ggplot(df21, aes(date, queenSideCastlingRate)) 		+ xlim(1850, 2015)	+ xlab(\"Date\") 	+ ylab(\"Queen Side Castling Rate\") 	+ ggtitle(\"21. Queen Side Castling Rate\") 	+ stat_smooth(method=\"glm\", family=\"binomial\")");		

		}

		if(nbClearData.asInt() > 0 && nbUniqueEloRating.asInt() > 100) {
			re.eval("ggsave(\"resources/graphs/1-EloRating.png\", plot = p1)");
			re.eval("ggsave(\"resources/graphs/2-DifferenceEloRating.png\", plot = p2)");
			re.eval("ggsave(\"resources/graphs/3-PlyPerGame.png\", plot = p3)");
			re.eval("ggsave(\"resources/graphs/4-GameWinByHogherEloRating.png\", plot = p4)");
			re.eval("ggsave(\"resources/graphs/5-GamesWinByWhitePlayer.png\", plot = p5)");
			re.eval("ggsave(\"resources/graphs/6-GameResultingWinByColor.png\", plot = p6)");
		}
		if(nbUniqueDate.asInt() > 10) {
			re.eval("ggsave(\"resources/graphs/7-DistributionOfDateMatch.png\", plot = p7)");
			re.eval("ggsave(\"resources/graphs/8-PlyPerGame.png\", plot = p8)");
			re.eval("ggsave(\"resources/graphs/9-GamesWinByWhitePlayer.png\", plot = p9)");
			re.eval("ggsave(\"resources/graphs/10-GamesResultingWinByColor.png\", plot = p10)");
			re.eval("ggsave(\"resources/graphs/11-FirstMoveWhite.png\", plot = p11)");
			re.eval("ggsave(\"resources/graphs/11-FirstMoveWhiteZoom.png\", plot = p12)");
			re.eval("ggsave(\"resources/graphs/13-GameWinByCheckMate.png\", plot = p13)");
			re.eval("ggsave(\"resources/graphs/14-RatioCapturedPieces.png\", plot = p14)");
			re.eval("ggsave(\"resources/graphs/15-RookMoveRate.png\", plot = p15)");
			re.eval("ggsave(\"resources/graphs/16-KnightMoveRate.png\", plot = p16)");
			re.eval("ggsave(\"resources/graphs/17-PawnMoveRate.png\", plot = p17)");
			re.eval("ggsave(\"resources/graphs/18-QueenMoveRate.png\", plot = p18)");
			re.eval("ggsave(\"resources/graphs/19-PromotedRate.png\", plot = p19)");
			re.eval("ggsave(\"resources/graphs/20-KingSideCastlingRate.png\", plot = p20)");
			re.eval("ggsave(\"resources/graphs/21-QueenSideCastlingRate.png\", plot = p21)");
		}
	}
	
	
	private Connection connexion;
	static final String SEPARATOR = ",";
	static final String ENDLINE = "\n";
	public static String PAWN = "P";
	public static String KNIGHT = "N";
	public static String BISHOP = "B";
	public static String ROOK = "R";
	public static String QUEEN = "Q";
	public static String KING = "K";
	static int count = 0;

	private void generateCSV() throws ClassNotFoundException, SQLException, NumberFormatException, IOException {
		Log.info("Connexion with SQL database... RUNNING");
		ConfigSQL config= new ConfigSQL("diverse");
		Class.forName(config.getDriver());
		Log.info("Config URL\t: " + config.getUrl());
		Log.info("Config DB\t: " + config.getDb());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		Log.info("Connexion with SQL database... OK\n");

		out.println("[1] Nombre de résultats souhaités (LIMIT) ?");
		String limit = reader.readLine();
		out.println("[2] Décalage souhaité (OFFSET) ?");
		String offset = reader.readLine();

		Log.info("Get data from database... RUNNING");
		Statement stmt = connexion.createStatement();
		ResultSet games = stmt.executeQuery("select id, whiteElo, blackElo, left(date, 4) as date, totalPlyCount, result, movesSAN from Game LIMIT " + limit + " OFFSET " + offset);
		Log.info("Get data from database... OK");
		
		int debut = Integer.valueOf(offset);
		int fin = Integer.valueOf(offset) + Integer.valueOf(limit);

		FileWriter fileWriter = null;

		try {
			Log.info("Enregistrement dans ./resources/csv/database_"+debut+"_"+fin+".csv");

			//fileWriter.append("sep=" + SEPARATOR + ENDLINE);

			fileWriter.append("whiteElo" + SEPARATOR
					+ "blackElo" + SEPARATOR
					+ "totalPlyCount" + SEPARATOR
					+ "result" + SEPARATOR
					+ "date" + SEPARATOR
					+ "firstMoveWhite" + SEPARATOR
					+ "firstMoveBlack" + SEPARATOR
					+ "checkMate" + SEPARATOR
					+ "nbCapturedPieces" + SEPARATOR
					+ "nbRookMove" + SEPARATOR
					+ "nbKnightMove" + SEPARATOR
					+ "nbPawnMove" + SEPARATOR
					+ "nbQueenMove" + SEPARATOR
					+ "promoted" + SEPARATOR
					+ "kingSideCastling" + SEPARATOR
					+ "queenSideCastling" + ENDLINE);

			while(games.next()) {
				int idGame = games.getInt(1);
				Log.info("Process Game #" + idGame);

				String content = games.getString(7);
				String[] pairs = content.split("\\s*\\d+\\.+\\s*");

				int 	nbCapturedPieces = 0,
						nbRookMove = 0,
						nbKnightMove = 0,
						nbPawnMove = 0,
						nbQueenMove = 0;
				boolean checkMate = false, 
						kingSideCastling = false,
						queenSideCastling = false,
						promoted = false;
				String firstMoveWhite = "";

				int nbP = 0;

				for (String pair : pairs) {
					if (pair.isEmpty()) {
						continue;
					}

					String[] rawMoves;

					if (pair.contains("{")) {
						String[] temp = pair.split("\\s+");
						int i = 0;
						ArrayList<String> list = new ArrayList<String>();

						while (i < temp.length) {
							if (temp[i].startsWith("{")) {
								StringBuilder b = new StringBuilder();

								while (i < temp.length) {
									b.append(temp[i] + " ");

									if (temp[i].endsWith("}")) {
										break;
									}

									i++;
								}

								list.add(b.toString().trim());
							} else {
								list.add(temp[i]);
							}

							i++;
						}

						rawMoves = list.toArray(new String[0]);
					} else {
						rawMoves = pair.split("\\s+");
					}


					for (int i = 0; i < rawMoves.length; i++) {
						if (rawMoves[i].equals("e.p.")) {
							continue;
						} else if (rawMoves[i].startsWith("{") && rawMoves[i].endsWith("}")) {
							// move comment
						} else {
							String strMove = rawMoves[i];

							if(nbP++ == 0) {
								firstMoveWhite = strMove;
							}
							if (strMove.startsWith(PAWN)) {
								nbPawnMove++;
							} else if (strMove.startsWith(KNIGHT)) {
								nbKnightMove++;
							} else if (strMove.startsWith(BISHOP)) {
								// nothing
							} else if (strMove.startsWith(ROOK)) {
								nbRookMove++;
							} else if (strMove.startsWith(QUEEN)) {
								nbQueenMove++;
							} else if (strMove.startsWith(KING)) {
								// nothing
							} else {
								nbPawnMove++;
							}

							if (strMove.contains("x")) {
								nbCapturedPieces++;
								strMove = strMove.replace("x", "");
							}

							if (strMove.contains("+")) {
								strMove = strMove.replace("+", "");
							}

							if (strMove.contains("#")) {
								checkMate = true;
								strMove = strMove.replace("#", "");
							}

							if (strMove.contains("=")) {
								promoted = true;
							}

							if (strMove.equals("0-0") || strMove.equals("O-O")) {
								kingSideCastling = true;
							} else if (strMove.equals("0-0-0") || strMove.equals("O-O-O")) {
								queenSideCastling = true;
							}
						}
					}

				}

				fileWriter.append(games.getInt(2) + SEPARATOR
						+ games.getInt(3) + SEPARATOR
						+ games.getInt(5) + SEPARATOR
						+ games.getString(6) + SEPARATOR
						+ games.getString(4) + SEPARATOR
						+ firstMoveWhite + SEPARATOR
						+ "" + SEPARATOR
						+ ((checkMate) ? 1 : 0) + SEPARATOR
						+ nbCapturedPieces + SEPARATOR
						+ nbRookMove + SEPARATOR
						+ nbKnightMove + SEPARATOR
						+ nbPawnMove + SEPARATOR
						+ nbQueenMove + SEPARATOR
						+ ((promoted) ? 1 : 0) + SEPARATOR
						+ ((kingSideCastling) ? 1 : 0) + SEPARATOR
						+ ((queenSideCastling) ? 1 : 0) + ENDLINE);
			}
		} catch (Exception e) {
			Log.info("Une erreur s'est produite lors de la création du fichier CSV");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				Log.info("Erreur lors de la fermeture du fichier CSV");
				e.printStackTrace();
			}
		}

	}

}
