package engine;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class EnginePreferences {

	private long maxComputationTimeMillis = 5000;
	
	private int depth = 20;
	
	/**
	 * Method getDepth.
	 * @return int
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Method setDepth.
	 * @param depth int
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	private Map<String, String> options;
	
	public EnginePreferences() {
		this.options = new HashMap<String, String>();
	}
	
	/**
	 * Method getMaxComputationTimeMillis.
	 * @return long
	 */
	public long getMaxComputationTimeMillis() {
		return maxComputationTimeMillis;
	}
	
	/**
	 * Method setMaxComputationTimeMillis.
	 * @param maxComputationTimeMillis long
	 */
	public void setMaxComputationTimeMillis(long maxComputationTimeMillis) {
		this.maxComputationTimeMillis = maxComputationTimeMillis;
	}
	
	/**
	 * Method setOption.
	 * @param name String
	 * @param value String
	 */
	public void setOption(String name, String value) {
		this.options.put(name, value);
	}
	
	/**
	 * Method removeOption.
	 * @param name String
	 */
	public void removeOption(String name) {
		this.options.remove(name);
	}
	
	/**
	 * Method getOptions.
	 * @return Map<String,String>
	 */
	public Map<String, String> getOptions() {
		return options;
	}
	
}
