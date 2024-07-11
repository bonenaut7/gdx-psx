package by.bonenaut7.gdxpsx.postprocessing;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/** PSXPostProcessingShader by it's concept is just
 *    {@link com.badlogic.gdx.graphics.glutils.ShaderProgram shaderProgram}
 *    holder that has some functions to configurate shader inside.
 * 
 * <br><br>
 * <b> Features: </b>
 * <ul>
 * 		<li> Resolution downscaling - Downscale image by scale or to the target resolution </li>
 * 		<li> Dithering - Image dithering with pre-made or custom patterns </li>
 * 		<li> Color reduction - Simulation of physical color reduction by simple math function. </li>
 * </ul>
 * 
 * <br>
 * 
 * <b> Quick start: </b>
 * <ul>
 * 		<li> Create the PSXPostProcessingShader instance, for example with
 * 			{@link by.bonenaut7.gdxpsx.postprocessing.PSXPostProcessingShaderStatic PSXPostProcessingShaderStatic}
 * 		</li>
 * 		<li> Configure PSXPostProcessingShader as you wish </li> 
 * 		<li> Set input resolution with {@link #setInputResolution(int, int) setInputResolution(...)} </li>
 * 		<li> Apply your changes using {@link PSXPostProcessingShader#update() update()} </li>
 * 		<li> Use shader program as you wish! (using {@link #getShaderProgram()}) </li>
 * </ul>
 * <br>
 * 
 * Comparing to pre-release versions of gdx-psx, this one should be the
 *   best of all tries, it utilizes interface as the API, pre-made
 *   implementations (currently static shader only), and you can
 *   extend any class to change the logic as you wish.
 * 
 * @author bonenaut7 **/
public interface PSXPostProcessingShader extends Disposable {
	
	/** @return true if downscaling is enabled **/
	boolean isDownscalingEnabled();
	
	/** Enables or disables downscaling
	 * @param enabled Enables downscaling if following parameter is true
	 * @return self **/
	PSXPostProcessingShader setDownscalingEnabled(boolean enabled);
	
	/** @return downscaling scale, equal or higher than 1 **/
	float getDownscalingScale();

	/** Sets downscaling mode that will use scale as it's modifier 
	 * @param scale Downscaling scale, <b>Can't be less than 1</b>.
	 *   Input resolution will be divided by this parameter.
	 * @return self **/
	PSXPostProcessingShader setDownscalingFromScale(float scale);
	
	/** @return downscaling target resolution width, equal or higher than 1 **/
	int getDownscalingTargetWidth();
	
	/** @return downscaling target resolution height, equal or higher than 1 **/
	int getDownscalingTargetHeight();
	
	/** Sets downscaling mode that will use target resolution.
	 * In other words it will use scale factors based on formula
	 *   <code>inputResolution / targetResolution</code>, and will
	 *   muliply input resolution by the output of this formula scales
	 *   in the shader.
	 * @param width Target resolution width, can't be less than 1
	 * @param height Target resolution height, can't be less than 1
	 * @return self **/
	PSXPostProcessingShader setDownscalingToResolution(int width, int height);
	
	/** Sets downscaling mode that will use target resolution.
	 * In other words it will use scale factors based on formula
	 *   <code>inputResolution / targetResolution</code>, and will
	 *   muliply input resolution by the output of this formula scales
	 *   in the shader.
	 * @param targetResolution Vector with width & height, values can't be less than 1
	 * @return self **/
	PSXPostProcessingShader setDownscalingToResolution(Vector2 targetResolution);
	
	
	
	/** @return true if dithering is enabled **/
	boolean isDitheringEnabled();
	
	/** Enables or disables dithering
	 * @param enabled Enables dithering if following parameter is true
	 * @return self **/
	PSXPostProcessingShader setDitheringEnabled(boolean enabled);
	
	/** @return dithering intensity **/
	float getDitheringIntensity();
	
	/** Sets dithering intensity. Doesn't work with legacy dithering.
	 * @param intensity Dithering intensity, post-multiplied by 0.01.
	 *   Dithering matrix value is being multiplied by this intensity before
	 *   applying to the pixel color.
	 * @return self **/
	PSXPostProcessingShader setDitheringIntensity(float intensity);
	
	/** @return dithering scale, equal or higher than 1 **/
	float getDitheringScale();
	
	/** Sets dithering scale
	 * @param scale Dithering scale, <b>Can't be less than 1</b>.
	 * @return self **/
	PSXPostProcessingShader setDitheringScale(float scale);
	
	/** @return dithering matrix, can't be null **/
	DitheringMatrix getDitheringMatrix();
	
	/** Sets dithering matrix 
	 * @param ditheringMatrix Dithering matrix, can't be null
	 * @return self **/
	PSXPostProcessingShader setDitheringMatrix(DitheringMatrix ditheringMatrix);
	
	/** @return true if legacy dithering enabled **/
	boolean isLegacyDitheringEnabled();
	
	/** Enables or disables legacy dithering.
	 * Legacy dithering is dithering that has been used in previous
	 *   versions of gdx-psx, looks like dithering that has been
	 *   'blended' into the image.
	 * @implNote Legacy dithering doesn't support dithering intensity values
	 * @return true if legacy dithering is enabled **/
	PSXPostProcessingShader setLegacyDitheringEnabled(boolean enabled);
	
	
	
	/** @return true if color reduction is enabled **/
	boolean isColorReductionEnabled();
	
	/** Enables or disables color reduction
	 * @param enabled Enables color reduction if following parameter is true
	 * @return self **/
	PSXPostProcessingShader setColorReductionEnabled(boolean enabled);
	
	/** @return color reduction factor, equal or higher than 1 **/
	float getColorReductionFactor();
	
	/** Sets color reduction factor. <br>
	 * In shader, color will be calculated as <code>Math.floor(color * factor) / factor</code>,
	 *   and with <code>Math.ceil(...)</code> if legacy dithering is enabled.
	 * @param colorReduction Color reduction factor, can't be less than 1 
	 * @return self **/
	PSXPostProcessingShader setColorReduction(float colorReduction);
	
	
	/** Sets input resolution for the downscaling and dithering (framebuffer or app's backbuffer resolution)
	 * @param width Width, can't be less than 1
	 * @param height Height, can't be less than 1
	 * @return self **/
	PSXPostProcessingShader setInputResolution(int width, int height);
	
	/** Sets input resolution for the downscaling and dithering (framebuffer or app's backbuffer resolution)
	 * @param inputResolution Vector with width & height, values can't be less than 1
	 * @return self **/
	PSXPostProcessingShader setInputResolution(Vector2 inputResolution);
	
	/** Updates shader or uniform information(based on implementation specifics).
	 * In most of cases will do pretty big work, and should be counted as heavy method.
	 * For example, in static implementation shader is being compiled from scratch,
	 *   and all definitions for the shader will be created every {@link #update()} call.
	 * @return true if shader updated successfully, or false if error has been ocurred **/
	boolean update();
	
	/** @return {@link com.badlogic.gdx.graphics.glutils.ShaderProgram Post-processing ShaderProgram},
	 *    or null if {@link #update()} hasn't been called before.
	 *    (could be called in implementation's constructor by default, not producing null's at all). **/
	ShaderProgram getShaderProgram();
}
