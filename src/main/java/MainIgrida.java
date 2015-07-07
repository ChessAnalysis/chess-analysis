import ictk.boardgame.IllegalMoveException;
import ictk.boardgame.chess.AmbiguousChessMoveException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;

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
		
		if(args.length > 0) {
			
			try {
				Thread.sleep((long) (Math.random()*10000));
			} catch (Exception e) {
				
			}
			
			String[] argv = {"-i", "0"};
			
			File file = new File("/temp_dd/igrida-fs1/fesnault/SCRATCH/num.txt");
			
			FileInputStream in = new FileInputStream(file);
			
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			argv[1] = r.readLine();
			r.close();
			
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(String.valueOf(Integer.valueOf(argv[1])+1));
			output.flush();
			output.close();
			
			try {
				StockfishAnalyze proc = new StockfishAnalyze();
				new JCommander(proc, argv);
				proc.init();
			} catch (Exception e) {
				Log.info(e);
				Log.info("USAGE : java -jar {file-name.jar} -i {input} -pv {multipv} -d {depth} -t {threads}");
			}
		}
	}
}