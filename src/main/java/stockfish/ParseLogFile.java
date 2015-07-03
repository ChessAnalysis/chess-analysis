package stockfish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jline.internal.Log;

public class ParseLogFile {

	public static void main(String[] args) {
		new ParseLogFile();
	}

	public ParseLogFile() {
		Map<String, String> logs = splitLogs();
		Map<String, List<RowLog>> rows = new HashMap<String, List<RowLog>>();
		Log.info("Le fichier contient " + logs.size() + " logs.");
		
		// Parcours pour parser tous les logs
		Set<String> keys = logs.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = logs.get(key);
			String[] lines = value.split("\\n");
			List<RowLog> currentLog = new ArrayList<RowLog>();
			for(int i = 0 ; i < lines.length ; i++) {
				if(!lines[i].startsWith("bestmove") && !lines[i].contains("mate 0")) {
					StringTokenizer st = new StringTokenizer(lines[i], " ");
					int k=0;
					RowLog currentRaw = new RowLog();
					while(st.hasMoreTokens()) {
						String t = st.nextToken();
						switch(k) {
						case 2 : currentRaw.setDepth(t); break;
						case 4 : currentRaw.setSeldepth(t); break;
						case 6 : currentRaw.setMultipv(t); break;
						case 8 : currentRaw.setScoreType(t); break;
						case 9 : currentRaw.setScoreResult(t); break;
						}
						k++;
					}
					currentLog.add(currentRaw);
				}
			}
			rows.put(key, currentLog);
		}
		
		// Parcours pour récupérer les logs
		keys = rows.keySet();
		it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			Log.info(key);
			List<RowLog> value = rows.get(key);
			for(int i = 0 ; i < value.size() ; i++) {
				if(value.get(i).getMultipv().equals("1")) {
					Log.info(value.get(i));
				}
			}
			Log.info("-------------------");
		}
	}

	private HashMap<String, String> splitLogs() {
		URL url = getClass().getResource("/log"); 
		HashMap<String, String> map = new HashMap<String, String>();

		InputStream is;
		try {
			is = url.openStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuilder sb = new StringBuilder();
			String FEN = "";
			boolean isFirst = true;
			while ((line = br.readLine()) != null) {
				if(isFirst) {
					FEN = line;
					isFirst = false;
				}else{
					if(line.contains("---")){
						map.put(FEN, sb.toString());
						sb.setLength(0);
						isFirst = true;
					}else{
						sb.append(line + "\n");
					}
				}
			}
			return map;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private class RowLog {
		
		private String depth, seldepth, multipv, scoreType, scoreResult;

		public RowLog() {
			// TODO Auto-generated constructor stub
		}
		public String getDepth() {
			return depth;
		}
		public void setDepth(String depth) {
			this.depth = depth;
		}
		public String getSeldepth() {
			return seldepth;
		}
		public void setSeldepth(String seldepth) {
			this.seldepth = seldepth;
		}
		public String getMultipv() {
			return multipv;
		}
		public void setMultipv(String multipv) {
			this.multipv = multipv;
		}
		public String getScoreType() {
			return scoreType;
		}
		public void setScoreType(String scoreType) {
			this.scoreType = scoreType;
		}
		public String getScoreResult() {
			return scoreResult;
		}
		public void setScoreResult(String scoreResult) {
			this.scoreResult = scoreResult;
		}
		
		public String toString() {
			return "pv " + multipv + " => depth " + depth + " (" + seldepth + ") => " + scoreType + " " + scoreResult;
		}
		
	}

}


