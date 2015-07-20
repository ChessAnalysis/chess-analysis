package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import stockfish.RowLog;
import stockfish.RowsLog;
import jline.internal.Log;
import config.ConfigSQL;

public class AnalyseFromDatabase {
	
	private Connection connexion;
	
	public AnalyseFromDatabase() throws SQLException, ClassNotFoundException {
		init();
	}

	private void init() throws SQLException, ClassNotFoundException {
		/*
		ConfigSQL config= new ConfigSQL("diverse");
		Class.forName(config.getDriver());
		this.connexion = DriverManager.getConnection(config.getUrl() + config.getDb() + "?user=" + config.getUser() + "&password=" + config.getPass() + "&rewriteBatchedStatements=true");
		this.connexion.setAutoCommit(true);
		Statement stmt = connexion.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT Move.move, FEN.id, FEN.log FROM Move, FEN WHERE Move.idFEN = FEN.id AND Move.idGame = 1 ORDER BY Move.halfMove ASC");
		
		String crtFEN = "";
		String crtLOG = "";
		
		while (rs.next()) {
			crtFEN = rs.getString(2);*/
			//crtLOG = rs.getString(3);
			String crtLOG = "info depth 1 seldepth 1 multipv 1 score cp 0 nodes 70 nps 35000 time 2 pv e4b4. info depth 2 seldepth 2 multipv 1 score cp 117 nodes 191 nps 63666 time 3 pv e4f4 b8c7 f4g5 c7d6 g5f6 d8c7 f8h6. info depth 3 seldepth 3 multipv 1 score cp 117 nodes 243 nps 81000 time 3 pv e4f4 b8c7 f4g5. info depth 4 seldepth 4 multipv 1 score cp 117 nodes 351 nps 117000 time 3 pv e4f4 b8c7 f4g5 c7d6. info depth 5 seldepth 6 multipv 1 score cp 34 nodes 1559 nps 389750 time 4 pv e4f4 d8c8 f4g5 b8d6 f8d6. info depth 6 seldepth 6 multipv 1 score cp 34 nodes 2241 nps 560250 time 4 pv e4g2 d8c8 g2g5 b8d6 f8d6 c6d6. info depth 7 seldepth 8 multipv 1 score cp 22 nodes 3906 nps 781200 time 5 pv e4g2 d8c8 g2g5 b8d6 f8d6 c6d6 g5h6. info depth 8 seldepth 9 multipv 1 score cp 22 nodes 5566 nps 927666 time 6 pv e4e5 d8c8 e5g5 b8d6 f8d6 c6d6 g5h6 d6d8 h6f8. info depth 9 seldepth 14 multipv 1 score cp 0 nodes 13377 nps 1216090 time 11 pv e4e5 d8c8 e5f5 c8b7 f5b1 b7c7 b1f5 c7b7. info depth 10 seldepth 14 multipv 1 score cp 0 nodes 18385 nps 1225666 time 15 pv e4e5 d8c8 e5f5 c8b7 f5b1 b7c7 b1f5 c7b7. info depth 11 seldepth 21 multipv 1 score cp 0 nodes 29630 nps 1346818 time 22 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 12 seldepth 21 multipv 1 score cp 0 nodes 33952 nps 1358080 time 25 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 13 seldepth 21 multipv 1 score cp 0 nodes 38669 nps 1432185 time 27 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 14 seldepth 21 multipv 1 score cp 0 nodes 51391 nps 1427527 time 36 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 15 seldepth 21 multipv 1 score cp 0 nodes 59579 nps 1241229 time 48 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 16 seldepth 21 multipv 1 score cp 0 nodes 72354 nps 1292035 time 56 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 17 seldepth 21 multipv 1 score cp 0 nodes 86874 nps 1336523 time 65 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 18 seldepth 21 multipv 1 score cp 0 nodes 107197 nps 1392168 time 77 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 19 seldepth 21 multipv 1 score cp 0 nodes 133170 nps 1431935 time 93 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. info depth 20 seldepth 21 multipv 1 score cp 0 nodes 166078 nps 1456824 time 114 pv e4e5 b8c7 e5f4 c6e8 f4g5 c7d6 g5f6 d6e7 f6b6 d8c8 b6a6 c8b8 a6b6 b8a8 b6a6 a8b8. bestmove e4e5 ponder b8c7. ";
			
			RowsLog rows = new RowsLog();
			
			System.out.println();
			
			String[] lines = crtLOG.split("\\. ");
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
					rows.add(currentRaw);
					//currentLog.add(currentRaw);
				}
			}
			
			System.out.println(rows.getByPV(1));
			
			System.out.println(rows.getBlunders());
		}
		
		
	//	stmt.close();
	//}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		new AnalyseFromDatabase();
	}
	


}
