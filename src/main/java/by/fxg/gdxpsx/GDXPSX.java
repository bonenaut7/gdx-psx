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
	
	public static void enableDebug() {
		LOG_MESSAGES = true;
		MIN_LOG_LEVEL = Level.ALL;
		THROW_EXCEPTIONS = true;
	}
	
	public static void setDefaults() {
		LOG_MESSAGES = true;
		MIN_LOG_LEVEL = Level.INFO;
		THROW_EXCEPTIONS = true;
	}
	
	public static void keepSilence() {
		LOG_MESSAGES = false;
		THROW_EXCEPTIONS = false;
	}

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
	
	/** If some advanced functions like {@link PSXPostProcessing#CACHE_ACTIVE_SHADER_BINDING} are used
	 *    in the end of your app call this method to dispose static resources **/
	public static void disposeResources() {
		
	}
	
	// Private constructor, believe me you don't need a GDXPSX object >:(
	GDXPSX() {}
}
