package engine;

/**
 * @author Nicola Ferraro, François Esnault
 * @date 28 août 2015
 */
public interface Engine {
	
	/**
	 * Method computeScore.
	 * @param fen String
	
	 * @return String */
	public String computeScore(String fen);

	public void debugEngine();
	
}
