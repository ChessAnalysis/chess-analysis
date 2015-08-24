import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

public class GenerateArguments {

	public static void main(String[] args) throws IOException {
		
		int min = 0;
		int max = 9;
		String path = "/temp_dd/igrida-fs1/fesnault/LAST";
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = min; i <= max; i++) {
			sb.append("-p " + path + " -i " + i + " -t 2\n");
		}
		
		Files.write(sb, new File("./resources/igrida/param-file.txt"), Charset.defaultCharset());
	}
}
