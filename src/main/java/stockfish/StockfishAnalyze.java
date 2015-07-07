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

import com.beust.jcommander.Parameter;
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
	
	@Parameter(names = "-d", description = "Depth")
	private Integer depth = 20;
	
	@Parameter(names = "-pv", description = "Multipv")
	private String multipv = "1";
	
	@Parameter(names = "-i", description = "File")
	private Integer file = 0;

	public StockfishAnalyze() throws SQLException, IOException {
		prefs.setOption("multipv", multipv);
		prefs.setOption("Threads", "1");
		prefs.setDepth(depth);
		
		engine = EngineFactory.getInstance().createEngine("/temp_dd/igrida-fs1/fesnault/SCRATCH" + STOCKFISH_IGRIDA, prefs);
		
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
				Files.append(sb, new File("/temp_dd/igrida-fs1/fesnault/SCRATCH" + "/fen/o" + i), Charset.defaultCharset());
				sb.setLength(0);
			}
		}
	}

}