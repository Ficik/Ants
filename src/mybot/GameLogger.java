package mybot;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public class GameLogger extends Logger {

	private final static String LOGGER_DOMAIN = "eu.fisoft.aichallenge.ants";
	private final static String LOGGER_NAME = "Ants";
	private final static String LOG_DIR = "logs";
	
	
	protected GameLogger() {
		super(LOGGER_DOMAIN, LOGGER_NAME);
		startLoggingToFile();
	}
	
	public String getRoundChanges(){
		String log = getRoundHeader();
		// TODO: log deads/new food/ants
		return log;
	}
	
	public String getRoundHeader(){
		return "===== Round #"+GameState.getInstance().getRound()+" =====";
	}

	public void logRoundChanges() {
		log(Level.INFO, getRoundChanges());
	}
	
	public void startLoggingToFile(){
		try {
			Handler handler = new FileHandler(LOG_DIR+"/Ants.log", false);
			handler.setFormatter(new SimpleFormatter());
			addHandler(handler);
		} catch (Exception e) {
			log(Level.SEVERE,"Logging to file not started: "+e.getMessage());
		}
	}

}
