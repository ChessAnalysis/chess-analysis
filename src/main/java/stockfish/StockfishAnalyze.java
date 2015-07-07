package stockfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	
	@Parameter
	private List<String> parameters = new ArrayList<String>();
	
	@Parameter(names = "-d", description = "Depth")
	private Integer depth = 20;
	
	@Parameter(names = "-pv", description = "Multipv")
	private String multipv = "1";
	
	@Parameter(names = "-t", description = "Threads")
	private String threads = "1";
	
	@Parameter(names = "-i", description = "File")
	private Integer file = 0;
	
	public void init() throws SQLException, IOException {
		prefs.setOption("multipv", multipv);
		prefs.setOption("Threads", threads);
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
		
		URL url = getClass().getResource("/x0"+i); 
		
		InputStream is = url.openStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String currentFEN;
		StringBuilder sb = new StringBuilder();
		while ((currentFEN = br.readLine()) != null) {
			sb.append(currentFEN + "\n");
			sb.append(engine.computeScore(currentFEN));
			sb.append("---\n");
			Log.info(count++);
			
			if((count%5)==0) {
				Log.info("Save...");
				Files.append(sb, new File("/temp_dd/igrida-fs1/fesnault/SCRATCH" + "/fen/o" + i), Charset.defaultCharset());
				sb.setLength(0);
			}
		}
	}

	public static EnginePreferences getPrefs() {
		return prefs;
	}

	public static void setPrefs(EnginePreferences prefs) {
		StockfishAnalyze.prefs = prefs;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		StockfishAnalyze.count = count;
	}

	public static Engine getEngine() {
		return engine;
	}

	public static void setEngine(Engine engine) {
		StockfishAnalyze.engine = engine;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public String getMultipv() {
		return multipv;
	}

	public void setMultipv(String multipv) {
		this.multipv = multipv;
	}

	public String getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = threads;
	}

	public Integer getFile() {
		return file;
	}

	public void setFile(Integer file) {
		this.file = file;
	}

	public static String getStockfishIgrida() {
		return STOCKFISH_IGRIDA;
	}
	
	
	
	

}