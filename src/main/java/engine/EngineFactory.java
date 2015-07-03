package engine;

public enum EngineFactory {
	
	INSTANCE;
	
	private EngineFactory() {
	}
	
	public static EngineFactory getInstance() {
		return INSTANCE;
	}
	
	public Engine createEngine(String uciEngineStartCommand) {
		return createEngine(uciEngineStartCommand, new EnginePreferences());
	}
	
	public Engine createEngine(String uciEngineStartCommand, EnginePreferences preferences) {
		if(uciEngineStartCommand==null || preferences==null) {
			throw new IllegalArgumentException("null parameters");
		}
		
		SimpleUciEngine engine = new SimpleUciEngine(uciEngineStartCommand, preferences);
		return engine;
	}
	
	
}
