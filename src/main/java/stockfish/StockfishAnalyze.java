package stockfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;

import jline.internal.Log;

import com.google.common.io.Files;

import engine.Engine;
import engine.EngineFactory;
import engine.EnginePreferences;

/**
 */
public class StockfishAnalyze {

	private static final String STOCKFISH_IGRIDA = "/uci-engine/stockfish-6-igrida/src/stockfish";

	private static EnginePreferences prefs = new EnginePreferences();
	private static int count;
	private static Engine engine;

	/**
	 * Constructor for StockfishAnalyze.
	 * @param file Integer
	 * @throws SQLException
	 * @throws IOException
	 */
	public StockfishAnalyze(Integer file) throws SQLException, IOException {
		prefs.setOption("multipv", "1");
		prefs.setOption("Threads", "5");
		prefs.setDepth(20);
		
		engine = EngineFactory.getInstance().createEngine(System.getProperty("user.home") + STOCKFISH_IGRIDA, prefs);
		
		Log.info("Début de l'analyse dynamique avec Stockfish");
		long startTimeParsed = System.nanoTime();
		initFile(file);
		Log.info("Parsed in " + ((System.nanoTime() - startTimeParsed)/1000000) + " ms.");
		
	}

	/**
	 * Method initFile.
	 * @param i Integer
	 * @throws SQLException
	 * @throws IOException
	 */
	private void initFile(Integer i) throws SQLException, IOException {
		
		URL url = getClass().getResource("/"+i); 
		
		InputStream is = url.openStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String currentFEN;
		StringBuilder sb = new StringBuilder();
		while ((currentFEN = br.readLine()) != null) {
			sb.append(currentFEN + "\n");
			sb.append(engine.computeScore(currentFEN));
			sb.append("---\n");
			System.out.println(sb);
			count++;
			
			if((count%5)==0) {
				Files.append(sb, new File(System.getProperty("user.home") + "/fen/o" + i), Charset.defaultCharset());
				sb.setLength(0);
			}
		}
	}

}