import java.io.IOException;
import java.sql.SQLException;

import com.beust.jcommander.JCommander;

import stockfish.StockfishAnalyze;

/**
 */
public class MainIgrida {

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
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException {
		
		try {
			StockfishAnalyze proc = new StockfishAnalyze();
			new JCommander(proc, args);
			proc.init();
		} catch (Exception e) {
			System.out.print("USAGE : java -jar {file-name.jar} -p {path} -i {filename} -pv {multipv} -d {depth} -t {threads}");
		}
		
	}
}