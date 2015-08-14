import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

public class GenerateArguments {

	public static void main(String[] args) throws IOException {
		
		int min = 1;
		int max = 800;
		String path = "/temp_dd/igrida-fs1/fesnault/SCRATCH2";
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = min; i < max; i++) {
			sb.append("-p " + path + " -i " + i + "\n");
		}
		
		Files.write(sb, new File("./resources/igrida/param-file.txt"), Charset.defaultCharset());
	}
}
