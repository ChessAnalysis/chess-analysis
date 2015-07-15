import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.chess.AmbiguousChessMoveException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;

import jline.internal.Log;

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
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException, AmbiguousChessMoveException, IllegalMoveException {
		
		try {
			StockfishAnalyze proc = new StockfishAnalyze();
			DecimalFormat nf = new DecimalFormat("0000");
			System.out.println(nf.format(Integer.valueOf(args[0])));
			String[] argv = {"-d", "20", "-t", "1", "pv", "1", "-i", nf.format(Integer.valueOf(args[0]))};
			new JCommander(proc, argv);
			proc.init();
		} catch (Exception e) {
			Log.info(e);
			Log.info("USAGE : java -jar {file-name.jar} -i {input} -pv {multipv} -d {depth} -t {threads}");
		}
		
	}
}