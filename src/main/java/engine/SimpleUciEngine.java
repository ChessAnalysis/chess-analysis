package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Nicola Ferraro, François Esnault
 * @date 28 août 2015
 */
class SimpleUciEngine implements Engine {

	private String command;

	private EnginePreferences preferences;

	private Process process;

	private PrintWriter toEngine;

	private BufferedReader fromEngine;

	/**
	 * Constructor for SimpleUciEngine.
	 * @param command String
	 * @param preferences EnginePreferences
	 */
	public SimpleUciEngine(String command, EnginePreferences preferences) {
		this.command = command;
		this.preferences = preferences;
	}

	private void connect() {
		
		if(this.process == null) {
			try {
				this.process = new ProcessBuilder(this.command).start();

				this.toEngine = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
				this.fromEngine = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
	
				skipLines(1);
	
				write("uci");
	
				readUntil("uciok");
	
				Map<String, String> options = preferences.getOptions();
				for(String name : options.keySet()) {
					String value = options.get(name);
					write("setoption name " + name + " value " + value);
				}
	
				write("isready");
	
				assertReply("readyok");
	
				write("ucinewgame");
	
			} catch(Exception e) {
				throw new IllegalStateException("Unable to initialize", e);
			}
		} else {
			write("isready");
			assertReply("readyok");
			write("ucinewgame");
		}
	}
	
	/**
	 * Method debugEngine.
	 * @see engine.Engine#debugEngine()
	 */
	@Override
	public void debugEngine() {
		this.process = null;
	}

	/**
	 * Method read.
	
	 * @return String */
	private String read() {
		try {
			String read = fromEngine.readLine();
			return read;
		} catch(IOException e) {
			throw new IllegalStateException("Unable to read from stream", e);
		}
	}

	/**
	 * Method assertReply.
	 * @param result String
	 */
	private void assertReply(String result) {
		String reply = read();
		if(!result.equalsIgnoreCase(reply)) {
			throw new IllegalStateException("Unexpected reply: " + reply);
		}
	}

	/**
	 * Method skipLines.
	 * @param number int
	 */
	private void skipLines(int number) {
		for(int i=0; i<number; i++) {
			read();
		}
	}

	/**
	 * Method readUntil.
	 * @param expectedResult String
	 */
	private void readUntil(String expectedResult) {
		String line;
		while((line=read())!=null) {
			if(line.equalsIgnoreCase(expectedResult)) {
				return;
			}
		}

		throw new IllegalStateException("No more input from uci engine");
	}

	/**
	 * Method write.
	 * @param command String
	 */
	private void write(String command) {
		toEngine.println(command);
		toEngine.flush();
	}

	/**
	 * Method computeScore.
	 * @param fen String
	 * @return String * @see engine.Engine#computeScore(String) * @see engine.Engine#computeScore(String) * @see engine.Engine#computeScore(String) * @see engine.Engine#computeScore(String)
	 */
	@Override
	public String computeScore(String fen) {
		
		try {
			connect();
			write("position fen " + fen);
			write("go depth " + preferences.getDepth());

			StringBuilder sb = new StringBuilder();
			String line;
			while((line=read())!=null) {
				if(line.contains("bestmove")) {
					sb.append(line + ". ");
					break;
				}
				if(!line.contains("currmove")) {
					sb.append(line + ". ");
				}
			}
			
			return sb.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return fen;
	}

}
