package engine;

/**
 * @author Nicola Ferraro, François Esnault
 * @date 28 août 2015
 */
public enum EngineFactory {
	
	INSTANCE;
	
	private EngineFactory() {
	}
	
	/**
	 * Method getInstance.
	
	 * @return EngineFactory */
	public static EngineFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Method createEngine.
	 * @param uciEngineStartCommand String
	
	 * @return Engine */
	public Engine createEngine(String uciEngineStartCommand) {
		return createEngine(uciEngineStartCommand, new EnginePreferences());
	}
	
	/**
	 * Method createEngine.
	 * @param uciEngineStartCommand String
	 * @param preferences EnginePreferences
	
	 * @return Engine */
	public Engine createEngine(String uciEngineStartCommand, EnginePreferences preferences) {
		if(uciEngineStartCommand==null || preferences==null) {
			throw new IllegalArgumentException("null parameters");
		}
		
		SimpleUciEngine engine = new SimpleUciEngine(uciEngineStartCommand, preferences);
		return engine;
	}
	
	
}
