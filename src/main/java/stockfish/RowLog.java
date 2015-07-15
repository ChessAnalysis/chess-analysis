package stockfish;

public class RowLog {

	private String depth, seldepth, multipv, scoreType, scoreResult;

	public RowLog() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Method getDepth.
	 * @return String
	 */
	public String getDepth() {
		return depth;
	}
	/**
	 * Method setDepth.
	 * @param depth String
	 */
	public void setDepth(String depth) {
		this.depth = depth;
	}
	/**
	 * Method getSeldepth.
	 * @return String
	 */
	public String getSeldepth() {
		return seldepth;
	}
	/**
	 * Method setSeldepth.
	 * @param seldepth String
	 */
	public void setSeldepth(String seldepth) {
		this.seldepth = seldepth;
	}
	/**
	 * Method getMultipv.
	 * @return String
	 */
	public String getMultipv() {
		return multipv;
	}
	/**
	 * Method setMultipv.
	 * @param multipv String
	 */
	public void setMultipv(String multipv) {
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
	public String getScoreResult() {
		return scoreResult;
	}
	/**
	 * Method setScoreResult.
	 * @param scoreResult String
	 */
	public void setScoreResult(String scoreResult) {
		this.scoreResult = scoreResult;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "pv " + multipv + " => depth " + depth + " (" + seldepth + ") => " + scoreType + " " + scoreResult;
	}


}
