package by.fxg.gdxpsx;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class GDXPSX {
	public static final String EMPTY = "";
	
	public static Logger logger;
	public static boolean logMessages = true;
	public static Level minLogLevel = Level.INFO;
	public static boolean throwExceptions = true;
	
	/** Enables debug mode for logging, enables exceptions **/
	public static void enableDebug() {
		logMessages = true;
		minLogLevel = Level.ALL;
		throwExceptions = true;
	}
	
	/** Sets defaults for logging and enables exceptions **/
	public static void setDefaults() {
		logMessages = true;
		minLogLevel = Level.INFO;
		throwExceptions = true;
	}
	
	/** "Work quietly" mode, disables logging and exceptions **/
	public static void keepSilence() {
		logMessages = false;
		throwExceptions = false;
	}

	/** Method for logging messages and throwing exceptions :b
	 *  @param level - Logging level for message, starting from SEVERE throws
	 *    {@link GdxRuntimeException} with message from #array parameter 
	 *  @param array - message that will be combined via {@link StringBuilder} **/
	public static void log(Level level, Object... array) {
		if (!logMessages) return;
		if (level.intValue() < minLogLevel.intValue()) return;
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]);
		}
		
		if (level.intValue() >= Level.SEVERE.intValue() && throwExceptions) {
			throw new GdxRuntimeException("[" + level.getName() + "] " + builder.toString());
		} else if (logger == null) {
			Gdx.app.log("GDX-PSX [" + level.getName() + "]", builder.toString());
		} else {
			logger.log(level, builder.toString());
		}
	}
	
	// Private constructor, believe me you don't need a GDXPSX object >:(
	GDXPSX() {}
}
