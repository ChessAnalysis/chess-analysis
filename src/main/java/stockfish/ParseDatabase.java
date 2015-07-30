package stockfish;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.rosuda.JRI.Rengine;

import com.google.common.io.Files;

import config.ConfigSQL;
import jline.internal.Log;

/**
 */
public class ParseDatabase {

	private Connection connexion;
	
	HashMap<Integer, Moves> games;

	public ParseDatabase(ConfigSQL config) throws ClassNotFoundException, SQLException, IOException {
		
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(true);
		
		getGames();
		
		Set<Integer> keys = games.keySet();
		Log.info(keys);
		Iterator<Integer> itKeys = keys.iterator();
		while(itKeys.hasNext()) {
			Integer idGame = itKeys.next();
			Moves moves = games.get(idGame);
			List<RowLog> scores = moves.getBestScores();
			analyseScores(idGame, scores);
		}
		System.exit(0);
	}

	private void analyseScores(int idGame, List<RowLog> scores) throws IOException {
		
		String SEPARATOR = "\t";
		SEPARATOR = ",";
		
		StringBuilder sb = new StringBuilder("Ply" + SEPARATOR + "Move" + SEPARATOR + "White" + SEPARATOR + "Black" + SEPARATOR + "Score" + SEPARATOR + "Eval" + SEPARATOR + "Comment\n");
		Iterator<RowLog> it = scores.iterator();
		
		int count = 0;
		Integer previous = 0;
		
		
		
		while(it.hasNext()) {
			RowLog tmpRow = it.next();
			if (tmpRow.getScoreType().equals("mate")) {
				if((count%2)==0)
					sb.append(count + SEPARATOR + "[" + ((count/2)+1) + "]" + SEPARATOR + tmpRow.getMove() + SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "#" + tmpRow.getScoreResult() + "#\n");
				else
					sb.append(count + SEPARATOR + "[" + ((count/2)+1) + "]" + SEPARATOR + SEPARATOR + tmpRow.getMove() + SEPARATOR + SEPARATOR + SEPARATOR + "#" + tmpRow.getScoreResult() + "#\n");
				previous = 0;
			} else {
				
				/* Compute the gain gk for each move k. Let sk−1 and sk be the position evaluation before and after move
				k, respectively. If the move was played by white, compute the gain as gk = sk − sk−1; if the move was
				played by black, compute the gain as gk = −(sk − sk−1). */
				
				Integer eval = getEval(count, tmpRow.getScoreResult());
				Integer gain = eval - previous;
				previous = eval;
				String comment = getComment(gain);
				
				if((count%2)==0) {
					sb.append(count + SEPARATOR + "[" + ((count/2)+1) + "]" + SEPARATOR + tmpRow.getMove() + SEPARATOR + SEPARATOR + Double.valueOf(eval)/100 + SEPARATOR + Double.valueOf(gain)/100 + SEPARATOR + comment + "\n");
				} else {
					sb.append(count + SEPARATOR + "[" + ((count/2)+1) + "]" + SEPARATOR + SEPARATOR + tmpRow.getMove() + SEPARATOR + Double.valueOf(eval)/100 + SEPARATOR + Double.valueOf(-gain)/100 + SEPARATOR + comment + "\n");
				}
			}
			count++;
		}
		
		Log.info(sb.toString());
		String filename = "analyse_" + idGame;
		Files.write(sb, new File("resources/"+filename+".csv"), Charset.defaultCharset());
		
		Log.info("Connexion with R engine... RUNNING");
		Log.info(System.getProperty("java.library.path"));
		Rengine re = Rengine.getMainEngine();
		if(re == null)
		    re = new Rengine(new String[] {"--vanilla"}, false, null);

		if (!re.waitForR()) {
			Log.error("Cannot load R");
			return;
		}
		Log.info("Connexion with R engine... OK\n");
		
		re.eval("library(ggplot2)");
		re.eval("library(plyr)");

		re.eval("evaluations = read.csv(\"/Users/fesnault/git/chess-analysis/resources/"+filename+".csv\", head=TRUE, sep=\",\")");
		re.eval("df1 <- data.frame(evaluations$Ply, evaluations$Score)");
		re.eval("p1 = ggplot(df1, aes(evaluations$Ply, evaluations$Score)) + geom_line()");
		re.eval("ggsave(p1, file=\"/Users/fesnault/git/chess-analysis/resources/"+filename+".png\", width=10, height=10)");
		
		
		
	}

	private Integer getEval(int count, int scoreResult) {
		if((count%2)==0) {
			return -Integer.valueOf(scoreResult);
		} else {
			return Integer.valueOf(scoreResult);
		}
	}

	private String getComment(Integer gain) {
		if(Math.abs(gain)>300) {
			return "Gaffe";
		} else if(Math.abs(gain)>100) {
			return "Erreur";
		}
		return "";
	}

	private void getGames() throws SQLException {
		games = new HashMap<Integer, Moves>();
		PreparedStatement selectGames = connexion.prepareStatement("SELECT id FROM Game");
		
		ResultSet rs = selectGames.executeQuery();
		Log.info("SELECT GAMES");
		while (rs.next()) {
			int id = rs.getInt(1);
			Log.info(id);
			games.put(id, getMoves(id));
		}
	}
	
	private Moves getMoves(Integer idGame) throws SQLException {
		PreparedStatement selectMoves = connexion.prepareStatement("SELECT Move.move, Move.halfMove, FEN.log FROM FEN, Move WHERE Move.idGame = '" + idGame + "' AND Move.idFEN = FEN.id ORDER BY Move.halfMove ASC");
		ResultSet rs = selectMoves.executeQuery();
		
		Moves moves = new Moves();
		
		while (rs.next()) {
			int idMove = rs.getInt(2);
			String log = rs.getString(3);
			
			Move move = new Move(rs.getString(1));
			
			String[] lines = log.split("\\.");
			for(int i = 0 ; i < lines.length ; i++) {
				if(!lines[i].trim().isEmpty() && !lines[i].trim().startsWith("bestmove") && !lines[i].trim().contains("mate 0")) {
					StringTokenizer st = new StringTokenizer(lines[i].trim(), " ");
					int k=0;
					RowLog depth = new RowLog(rs.getString(1));
					while(st.hasMoreTokens()) {
						String t = st.nextToken();
						switch(k) {
						case 2 : depth.setDepth(Integer.valueOf(t)); break;
						case 4 : depth.setSeldepth(Integer.valueOf(t)); break;
						case 6 : depth.setMultipv(Integer.valueOf(t)); break;
						case 8 : depth.setScoreType(t); break;
						case 9 : depth.setScoreResult(Integer.valueOf(t)); break;
						}
						k++;
					}
					move.add(depth);
				}
			}
			moves.add(move);
		}
		return moves;
	}
	
}


