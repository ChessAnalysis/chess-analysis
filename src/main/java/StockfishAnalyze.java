import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.io.Files;

import engine.Engine;
import engine.EngineFactory;
import engine.EnginePreferences;

/**
 * @author François Esnault
 * @date 28 août 2015
 */
public class StockfishAnalyze {
	
	/**
	 * Method main.
	 * @param args String[]
	 * @param -d, -depth	Depth - search x plies only (Default: 20)
	 * @param -e, -engine	Path to engine (Default: /Users/fesnault/Documents/uci-engine/stockfish-6-mac/Mac/stockfish-6-64)
	 * @param -i, -input	Path to input file (Default: <empty string>)
	 * @param -pv, -multipv	Multipv - search x best moves (Default: 1)
	 * @param -o, -output	Path to output file (Default: <empty string>)
	 * @param -t, -thread	Threads (Default: 1)
	 * @param -log, -verbose	Level of verbosity (Default: 0)
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException * @throws SQLException * @throws IOException * @throws InterruptedException * @throws SQLException * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException {
		StockfishAnalyze proc = new StockfishAnalyze();
		JCommander commands = new JCommander(proc, args);
		try {
			proc.init();
		} catch (Exception e) {
			e.printStackTrace();
			commands.usage();
		}
	}

	private static final String STOCKFISH_IGRIDA = "/temp_dd/igrida-fs1/fesnault/SCRATCH/uci-engine/stockfish-6-igrida/src/stockfish";

	private static EnginePreferences prefs = new EnginePreferences();
	private static int count;
	private static Engine engine;
	
	@Parameter
	private List<String> parameters = new ArrayList<String>();
	
	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
	private Integer verbose = 0;
	
	@Parameter(names = { "-d", "-depth" }, description = "Depth - search x plies only")
	private Integer depth = 20;
	
	@Parameter(names = { "-pv", "-multipv" }, description = "Multipv - search x best moves")
	private String multipv = "1";
	
	@Parameter(names = { "-t", "-thread" }, description = "Threads (default 1)")
	private String threads = "1";
	
	@Parameter(names = { "-i", "-input" }, description = "Path to input file", required = true)
	private String input = "";
	
	@Parameter(names = { "-e", "-engine" }, description = "Path to engine")
	private String pathToEngine = "/Users/fesnault/Documents/uci-engine/stockfish-6-mac/Mac/stockfish-6-64";
	
	@Parameter(names = "--help", help = true)
	private boolean help;

	private BufferedReader br;
	
	/**
	 * Method init.
	 * @throws SQLException * @throws IOException * @throws IOException * @throws IOException
	 */
	public void init() throws SQLException, IOException {
		// Set chess engine options
		prefs.setOption("multipv", multipv);
		prefs.setOption("Threads", threads);
		prefs.setDepth(depth);
		
		// Create the chess engine process
		engine = EngineFactory.getInstance().createEngine(pathToEngine, prefs);
		analyse();
	}

	/**
	 * Method initFile.
	 * @throws SQLException * @throws IOException * @throws IOException * @throws IOException * @throws IOException
	 */
	private void analyse() throws SQLException, IOException {
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
		String currentFEN;
		StringBuilder sb = new StringBuilder();
		while ((currentFEN = br.readLine()) != null) {
			// Analyse FEN to get log
			sb.append(currentFEN + "\t" + engine.computeScore(currentFEN) + '\n');
			if(verbose==1) {
				System.out.println(sb.toString());
			}
			// Catch exception when the chess engine is off
			if(sb.length() < 200) {
				if(!sb.toString().contains("info depth 0 score mate 0. bestmove (none).")) {
					sb.setLength(0);
					engine.debugEngine();
					sb.append(currentFEN + "\t" + engine.computeScore(currentFEN) + '\n');
				}
			}
			// Write log
			Files.append(sb, new File(input + "_output"), Charset.defaultCharset());
			sb.setLength(0);
		}
	}

	/**
	 * Method getPrefs.
	
	 * @return EnginePreferences */
	public static EnginePreferences getPrefs() {
		return prefs;
	}

	/**
	 * Method setPrefs.
	 * @param prefs EnginePreferences
	 */
	public static void setPrefs(EnginePreferences prefs) {
		StockfishAnalyze.prefs = prefs;
	}

	/**
	 * Method getCount.
	
	 * @return int */
	public static int getCount() {
		return count;
	}

	/**
	 * Method setCount.
	 * @param count int
	 */
	public static void setCount(int count) {
		StockfishAnalyze.count = count;
	}

	/**
	 * Method getEngine.
	
	 * @return Engine */
	public static Engine getEngine() {
		return engine;
	}

	/**
	 * Method setEngine.
	 * @param engine Engine
	 */
	public static void setEngine(Engine engine) {
		StockfishAnalyze.engine = engine;
	}

	/**
	 * Method getParameters.
	
	 * @return List<String> */
	public List<String> getParameters() {
		return parameters;
	}

	/**
	 * Method setParameters.
	 * @param parameters List<String>
	 */
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Method getDepth.
	
	 * @return Integer */
	public Integer getDepth() {
		return depth;
	}

	/**
	 * Method setDepth.
	 * @param depth Integer
	 */
	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	/**
	 * Method getMultipv.
	
	 * @return String */
	public String getMultipv() {
		return multipv;
	}

	/**
	 * Method setMultipv.
	 * @param multipv String
	 */
	public void setMultipv(String multipv) {
		this.multipv = multipv;
	}

	/**
	 * Method getThreads.
	
	 * @return String */
	public String getThreads() {
		return threads;
	}

	/**
	 * Method setThreads.
	 * @param threads String
	 */
	public void setThreads(String threads) {
		this.threads = threads;
	}

	/**
	 * Method getFile.
	
	 * @return String */
	public String getFile() {
		return input;
	}

	/**
	 * Method setFile.
	 * @param file String
	 */
	public void setFile(String file) {
		this.input = file;
	}

	/**
	 * Method getStockfishIgrida.
	
	 * @return String */
	public static String getStockfishIgrida() {
		return STOCKFISH_IGRIDA;
	}
	
	
	
	

}