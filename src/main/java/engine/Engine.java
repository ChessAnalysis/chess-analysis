package engine;

import java.util.List;

/**
 */
public interface Engine {
	
	/**
	 * Method computeBestMove.
	 * @param moves List<String>
	 * @return String
	 */
	public String computeBestMove(List<String> moves);
	
	/**
	 * Method computeScoreFast.
	 * @param fen String
	 * @return String
	 */
	public String computeScoreFast(String fen);
	
	/**
	 * Method computeScore.
	 * @param fen String
	 * @return String
	 */
	public String computeScore(String fen);

	public void debugEngine();
	
}
