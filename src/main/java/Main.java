import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.chess.AmbiguousChessMoveException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import jline.console.ConsoleReader;
import jline.internal.Log;
import config.ConfigSQL;
import database.GenerateFENFromDatabase;
import database.InsertECOToDatabase;
import database.InsertPGNToDatabase;

/**
 */
public class Main {

	/**
	 * Method main.
	 * @param args String[]
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AmbiguousChessMoveException
	 * @throws IllegalMoveException
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException, AmbiguousChessMoveException, IllegalMoveException {

		ConfigSQL connexion= new ConfigSQL("diverse");
		ConsoleReader reader = new ConsoleReader();
		PrintWriter out = new PrintWriter(reader.getOutput());
		out.print("Quel traitement ?\n[1] Insert Database\n[2] Chess Analysis\n");
		/*String line;
		while ((line = reader.readLine("> ")) != null) {
			switch(line) {
			case "1" :
				// INSERT OPENINGS
				Log.info("Insertion des codes ECO dans la base de données");
				//new InsertECOToDatabase(connexion);

				// INSERT PGN FILES
				Log.info("Insertion des parties PGN dans la base de données");
				String[] filesName;
				File[] files;

				filesName = new File("resources/tmp2").list();
				files = new File[filesName.length];
				for(int i = 0; i < filesName.length; i++) {
					files[i] = new File("resources/tmp2/" + filesName[i]);
				}
				for(File file: files) {
					if (!file.exists() || file.isHidden() || file.getName().equals(".DS_Store")) {
						Log.warn("Le fichier " + file.getName() + " n'existe pas.");
					} else {
						Log.info("> " + file.getAbsolutePath());
						new InsertPGNToDatabase(file.getAbsolutePath(), connexion);
					}
				} 
				break;
			case "2" :
				//new StockfishAnalyze(connexion);
				break;
			}
		}*/
		for(int i = 1; i < 100; i++) {
			new GenerateFENFromDatabase(connexion, i);
		}
	}
}