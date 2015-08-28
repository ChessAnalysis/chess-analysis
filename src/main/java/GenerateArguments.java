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
