package stockfish;

public class RowLog {

	private int depth, seldepth, multipv, scoreResult;
	private String scoreType, move;

	

	public RowLog() {
		// TODO Auto-generated constructor stub
	}
	
	public RowLog(String move) {
		this.move = move;
	}
	/**
	 * Method getDepth.
	 * @return String
	 */
	public int getDepth() {
		return depth;
	}
	/**
	 * Method setDepth.
	 * @param depth String
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	/**
	 * Method getSeldepth.
	 * @return String
	 */
	public int getSeldepth() {
		return seldepth;
	}
	/**
	 * Method setSeldepth.
	 * @param seldepth String
	 */
	public void setSeldepth(int seldepth) {
		this.seldepth = seldepth;
	}
	/**
	 * Method getMultipv.
	 * @return String
	 */
	public int getMultipv() {
		return multipv;
	}
	/**
	 * Method setMultipv.
	 * @param multipv String
	 */
	public void setMultipv(int multipv) {
		this.multipv = multipv;
	}
	/**
	 * Method getScoreType.
	 * @return String
	 */
	public String getScoreType() {
		return scoreType;
	}
	/**
	 * Method setScoreType.
	 * @param scoreType String
	 */
	public void setScoreType(String scoreType) {
		this.scoreType = scoreType;
	}
	/**
	 * Method getScoreResult.
	 * @return String
	 */
	public int getScoreResult() {
		return scoreResult;
	}
	/**
	 * Method setScoreResult.
	 * @param scoreResult String
	 */
	public void setScoreResult(int scoreResult) {
		this.scoreResult = scoreResult;
	}
	
	public String getMove() {
		return move;
	}

	public void setMove(String move) {
		this.move = move;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "pv " + multipv + " => depth " + depth + " (" + seldepth + ") => " + scoreType + " " + scoreResult;
	}


}
