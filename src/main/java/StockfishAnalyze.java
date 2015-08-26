

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
 */
public class StockfishAnalyze {
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException {
		StockfishAnalyze proc = new StockfishAnalyze();
		JCommander commands = new JCommander(proc, args);
		try {
			proc.init();
		} catch (Exception e) {
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
	
	public void init() throws SQLException, IOException {
		prefs.setOption("multipv", multipv);
		prefs.setOption("Threads", threads);
		//prefs.setOption("Hash", "1024");
		prefs.setDepth(depth);
		
		engine = EngineFactory.getInstance().createEngine(pathToEngine, prefs);
		initFile();
	}

	/**
	 * Method initFile.
	 * @param i Integer
	 * @throws SQLException
	 * @throws IOException
	 */
	private void initFile() throws SQLException, IOException {
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
		String currentFEN;
		StringBuilder sb = new StringBuilder();
		while ((currentFEN = br.readLine()) != null) {
			sb.append(currentFEN + "\t");
			sb.append(engine.computeScore(currentFEN));
			sb.append("\n");
			if(verbose==1) {
				System.out.println(sb.toString());
			}
			if(sb.length() < 200) {
				if(!sb.toString().contains("info depth 0 score mate 0. bestmove (none).")) {
					sb.setLength(0);
					engine.debugEngine();
					sb.append(currentFEN + "\t");
					sb.append(engine.computeScore(currentFEN));
					sb.append("\n");
				}
			}
			Files.append(sb, new File(input + "_output"), Charset.defaultCharset());
			sb.setLength(0);
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

	public String getFile() {
		return input;
	}

	public void setFile(String file) {
		this.input = file;
	}

	public static String getStockfishIgrida() {
		return STOCKFISH_IGRIDA;
	}
	
	
	
	

}