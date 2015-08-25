package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
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
				this.process = Runtime.getRuntime().exec(this.command);
	
				// Use the system charset
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
	
	@Override
	public void debugEngine() {
		this.process = null;
	}

	private synchronized void disconnect() {
		/*try {
			process.destroy();
		} catch(Exception e) {
			Log.info("Unable to disconnect " + e);
		} finally {
			toEngine = null;
			fromEngine = null;
			process = null;
		}*/
	}

	/**
	 * Method computeBestMove.
	 * @param moves List<String>
	 * @return String
	 * @see engine.Engine#computeBestMove(List<String>)
	 */
	public synchronized String computeBestMove(List<String> moves) {

		try {
			connect();

			StringBuilder moveList = new StringBuilder();
			for(String m : moves) {
				moveList.append(" " + m);
			}

			write("position startpos moves" + moveList);
			//write("go movetime " + preferences.getMaxComputationTimeMillis());
			write("go depth 10");

			String res = readStartsWith("bestmove");

			return getToken(res, 1);
		} catch(Exception e) {
			System.out.print("Best move not found, exception");
			throw new IllegalStateException(e);
		} finally {
			disconnect();
		}
	}

	/**
	 * Method read.
	 * @return String
	 */
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
	 * Method readStartsWith.
	 * @param expectedResult String
	 * @return String
	 */
	private String readStartsWith(String expectedResult) {
		String line;
		while((line=read())!=null) {
			//System.out.println(line);
			if(line.startsWith(expectedResult)) {
				return line;
			}
		}
		throw new IllegalStateException("No more input from uci engine");
	}

	/**
	 * Method readStartsWithUntil.
	 * @param expectedResult String
	 * @return String
	 */
	private String readStartsWithUntil(String expectedResult) {
		String line;
		while((line=read())!=null) {
			//System.out.println(line);
			if(line.startsWith(expectedResult) && line.contains("seldepth")) {
				return line;
			}
			return "EOF";
		}
		return "EOF";
		//throw new IllegalStateException("No more input from uci engine");
	}


	/**
	 * Method getToken.
	 * @param s String
	 * @param pos int
	 * @return String
	 */
	private String getToken(String s, int pos) {
		StringTokenizer st = new StringTokenizer(s, " ");
		int i=0;
		while(st.hasMoreTokens()) {
			String t = st.nextToken();
			if(i==pos) {
				return t;
			}
			i++;
		}
		return null;
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
	 * @return String
	 * @see engine.Engine#computeScore(String)
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
				//System.out.println(line);
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
			//Log.error("Best move not found, exception");
			//throw new IllegalStateException(e);
		} finally {
			disconnect();
		}
		
		
		return fen;
	}

	/**
	 * Method computeScoreFast.
	 * @param fen String
	 * @return String
	 * @see engine.Engine#computeScoreFast(String)
	 */
	@Override
	public String computeScoreFast(String fen) {
		try {
			connect();

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

			write("position fen " + fen);
			write("go depth " + preferences.getDepth());

			String res = "";
			boolean ended = false;
			while(!ended) {
				String tmp = readStartsWithUntil("info depth");
				if(!tmp.equals("EOF")) {
					res = tmp;
				} else {
					ended = true;
				}
			}

			if(res.contains("mate"))
				try {
					return "#" + getToken(res, 9);
				} catch(NullPointerException e) {
					return "#";
				}
			else
				return getToken(res, 9);

		} catch(Exception e) {
			return "#";
		} finally {
			disconnect();
		}
	}

	

}
