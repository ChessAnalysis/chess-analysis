import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

import com.google.common.io.Files;

/**
 * @author François Esnault
 * @date 28 août 2015
 */
public class GenerateArguments {

	/**
	 * Method main.
	 * @param args String[]
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		/* Usage: <main class> [options] 
		  Options:
		    -d, -depth
		       Depth - search x plies only
		       Default: 20
		    -e, -engine
		       Path to engine
		       Default: /Users/fesnault/Documents/uci-engine/stockfish-6-mac/Mac/stockfish-6-64
		  * -i, -input
		       Path to input file
		       Default: <empty string>
		    -pv, -multipv
		       Multipv - search x best moves
		       Default: 1
		    -o, -output
		       Path to output file
		       Default: <empty string>
		    -t, -thread
		       Threads (default 1)
		       Default: 1
		    -log, -verbose
		       Level of verbosity
		       Default: 0*/
		
		int min = 381;
		int max = 492;
		
		StringBuilder sb = new StringBuilder();
		DecimalFormat nf = new DecimalFormat("0000");
		
		for(int i = min; i <= max; i++) {
			sb.append("-e /temp_dd/igrida-fs1/fesnault/SCRATCH/uci-engine/stockfish-6-igrida/src/stockfish -i /temp_dd/igrida-fs1/fesnault/PASSE5/input/" + nf.format(i) + "\n");
		}
		
		Files.write(sb, new File("./resources/igrida/param-file.txt"), Charset.defaultCharset());
	}
}
