package by.fxg.gdxpsx;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class GDXPSX {
	public static final String EMPTY = "";
	
	public static Logger LOGGER;
	public static boolean LOG_MESSAGES = true;
	public static Level MIN_LOG_LEVEL = Level.INFO;
	public static boolean THROW_EXCEPTIONS = true;
	
	/** Enables debug mode for logging, enables exceptions **/
	public static void enableDebug() {
		LOG_MESSAGES = true;
		MIN_LOG_LEVEL = Level.ALL;
		THROW_EXCEPTIONS = true;
	}
	
	/** Sets defaults for logging and enables exceptions **/
	public static void setDefaults() {
		LOG_MESSAGES = true;
		MIN_LOG_LEVEL = Level.INFO;
		THROW_EXCEPTIONS = true;
	}
	
	/** "Work quietly" mode, disables logging and exceptions **/
	public static void keepSilence() {
		LOG_MESSAGES = false;
		THROW_EXCEPTIONS = false;
	}

	/** Method for logging messages and throwing exceptions :b
	 *  @param level - Logging level for message, starting from SEVERE throws
	 *    {@link GdxRuntimeException} with message from #array parameter 
	 *  @param array - message that will be combined via {@link StringBuilder} **/
	public static void log(Level level, Object... array) {
		if (!LOG_MESSAGES) return;
		if (level.intValue() < MIN_LOG_LEVEL.intValue()) return;
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]);
		}
		
		if (level.intValue() >= Level.SEVERE.intValue() && THROW_EXCEPTIONS) {
			throw new GdxRuntimeException("[" + level.getName() + "] " + builder.toString());
		} else if (LOGGER == null) {
			Gdx.app.log("GDX-PSX [" + level.getName() + "]", builder.toString());
		} else {
			LOGGER.log(level, builder.toString());
		}
	}
	
	// Private constructor, believe me you don't need a GDXPSX object >:(
	GDXPSX() {}
}
