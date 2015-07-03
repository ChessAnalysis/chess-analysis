package engine;

import java.util.List;

public interface Engine {
	
	public String computeBestMove(List<String> moves);
	
	public String computeScoreFast(String fen);
	
	public String computeScore(String fen);
	
}
