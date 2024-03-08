package by.fxg.gdxpsx.postprocessing;

import java.nio.IntBuffer;
import java.util.Locale;
import java.util.logging.Level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import by.fxg.gdxpsx.GDXPSX;

/** Better implementation of previously existed {@link PSXPostProcessingLegacy}
 *  
 *  In this version of post-processing 'object' (it doesn't feel like filter honestly)
 *    there's will be a slight performance boost because you can now choose the approach
 *    with updating values, if you're in debug mode you can easily switch to fully dynamic
 *    updating of values without need of recompiling shaders, while in production stage
 *    you're able to set all values you need and compiler preprocessor will cut out unnecessary
 *    parts of the shader. <br><br>
 *  
 *  To get desired result you need to do a few steps: <br>
 *  1. Create object of PSXPostProcessing <br>
 *  2. Set resolution of input image via {@link #setInputResolution(int, int)} or {@link #setInputResolution(Vector2)} <br>
 *  3. Apply needed you properties like resolution downscaling <br>
 *  4. Compile shader via {@link #compile(boolean)} <br>
 *  5. Use {@link #getShaderProgram()} to get needed effects <br><br>
 *  
 *  @implNote Note that if your original image changes resolution, don't forget to change it here
 *    and if shader is not in dynamic mode recompile shader too.
 *  @implNote Also don't forget to bind the shader before updating values in dynamic mode, otherwise
 *    values will be bound to another shader, this class doesn't verifying active shader :V
 */
public class PSXPostProcessing implements Disposable {
	/** Option to turn off parameters validation in case if you want to do something special.
	 *  Parameter validation is created for stopping compilation if some parameters are in invalid state. **/
	public static boolean VALIDATE_PARAMETERS = true;
	
	/** While post-processing is used in dynamic mode with this option turned on, you will be able to update 
	 *    parameters without recompiling shader only in dynamic mode, but any update requires shader to be
	 *    bound at the moment of changes, otherwise changes will try to be applied on the other bound shader 
	 *    and it will do nothing, or will do something bad, so be careful with this! **/
	public static boolean EXPLICIT_DYNAMIC_UNIFORM_UPDATES = true;
	
	/** @see #setActiveShaderBindingCache(IntBuffer) **/
	private static IntBuffer SHADER_BINDING_BUFFER = null;
	
	/** Option for caching bound shader before binding post-processing shader after compilation, to bind
	 *    previous shader after sending uniform values to compiled shader.
	 * @param intBuffer - Provide default or direct buffer with 4 bytes or 1 integer
	 *   size to enable caching, or NULL to disable it. **/
	public static void setActiveShaderBindingCache(final IntBuffer intBuffer) {
		SHADER_BINDING_BUFFER = intBuffer;
	}
	
	// PSXPostProcessing ==============================================================================================
	protected static final String UNIFORM_RESOLUTIONDOWNSCALING = "u_resDownscaling";			// vec4, [resolutionX, resolutionY, invResolutionX, invResolutionY]
	protected static final String UNIFORM_DITHERINGMATRIX_SAMPLER = "u_ditherMatrixTexture";	// sampler2d, Dithering matrix texture
	protected static final String UNIFORM_DITHERINGMATRIX_DATA = "u_ditheringData";				// vec3, [resolutionX, resolutionY, colorDepth]
	
	/** Vertex shader source **/
	protected String vertexShader;
	/** Fragment shader source **/
	protected String fragmentShader;
	/** {@link ShaderProgram} build from sources with prefix **/
	protected ShaderProgram shaderProgram;
	
	/** flag for selecting shader type(files) and uniform updates without compilation **/
	protected boolean isDynamic = false;
	/** Original(viewport/framebuffer/etc) input texture resolution **/
	protected Vector2 inputResolution = new Vector2();
	
	/** Resolution downscaling type **/
	protected ResolutionDownscalingType rdType = ResolutionDownscalingType.NONE;
	/** Resolution downscaling target resolution **/
	protected Vector2 rdTargetResolution = new Vector2();
	/** Resolution downscaling factor **/
	protected float rdFactor = 1F;

	/** Rectangle bayer dithering matrix **/
	protected DitheringMatrix ditheringMatrix = null;
	/** Dithering color depth **/
	protected float ditheringColorDepth;
	/** Dithering matrix scale **/
	protected float ditheringScale = 1F;
	/** Active texture target for binding Dithering matrix texture **/
	protected int ditheringMatrixTextureTarget = 1;
	/** Dithering matrix texture object **/
	protected Texture ditheringTexture = null;
	
	/** Sets default built-in parameters for post-processing along with input resolution.
	 *  Resolution Downscaling is set with type FACTOR and factor of 4.0,
	 *    DitheringMatrix is set to BAYER_8x8, target to 1 and scale of 1.0,
	 *    Resolution is set from the window from {@link Gdx#graphics}
	 *  @return self **/
	public PSXPostProcessing setDefaultParametersWithResolution() {
		this.setInputResolutionFromApp();
		this.setDefaultParameters();
		return this;
	}
	
	/** Sets default built-in parameters for post-processing. 
	 *  Resolution Downscaling is set with type FACTOR and factor of 3.0,
	 *    DitheringMatrix is set to BAYER_8x8, target to 1, color depth of 5 bit(32) and scale of 1.0 
	 *  @return self **/
	public PSXPostProcessing setDefaultParameters() {
		this.setResolutionDownscalingFactor(3.0F);
		this.setDitheringMatrix(DitheringMatrix.BAYER_8x8, 1, 32, 1);
		return this;
	}
	
	/** Sets original(viewport/framebuffer/etc) input resolution for post-processing effects
	 *    from {@link Gdx#graphics}
	 *  @return self **/
	public PSXPostProcessing setInputResolutionFromApp() {
		return this.setInputResolution(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	/** Sets original(viewport/framebuffer/etc) input resolution for post-processing effects
	 *  @param resolution - resolution of input texture
	 *  @throws NullPointerException if <b>resolution</b> is null
	 *  @return self **/
	public PSXPostProcessing setInputResolution(Vector2 resolution) {
		if (resolution == null) {
			if (GDXPSX.THROW_EXCEPTIONS) throw new NullPointerException("resolution can't be null!");
			return this;
		}
		
		return this.setInputResolution(resolution.x, resolution.y);
	}
	
	/** Sets original(viewport/framebuffer/etc) input resolution for post-processing effects
	 *  @param width - width of input texture
	 *  @param height - height of input texture
	 *  @return self **/
	public PSXPostProcessing setInputResolution(float width, float height) {
		this.inputResolution.set(width, height);
		
		// Clamping #rdTargetResolution to not upscale image in any case, upscaling is killing performance
		this.rdTargetResolution.set(Math.min(this.rdTargetResolution.x, width), Math.min(this.rdTargetResolution.y, height));
		return this;
	}
	
	/** Disables resolution downscaling filter
	 *  @return self **/
	public PSXPostProcessing resetResolutionDownscaling() {
		this.rdType = ResolutionDownscalingType.NONE;
		return this;
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscaling(ResolutionDownscalingType type) {
		return this.setResolutionDownscaling(type, this.rdTargetResolution.x, this.rdTargetResolution.y, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FACTOR type
	 *  @param targetFactor - Target resolution factor
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscalingFactor(float targetFactor) {
		return this.setResolutionDownscaling(ResolutionDownscalingType.FACTOR, this.rdTargetResolution.x, this.rdTargetResolution.y, targetFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FIT_TO_RESOLUTION type
	 *  @param targetResolution - Target resolution
	 *  @throws NullPointerException if <b>targetResolution</b> is null
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscalingFitToResolution(Vector2 targetResolution) {
		if (targetResolution == null) {
			if (GDXPSX.THROW_EXCEPTIONS) throw new NullPointerException("targetResolution can't be null!");
			return this;
		}
		
		return this.setResolutionDownscaling(ResolutionDownscalingType.FIT_TO_RESOLUTION, targetResolution.x, targetResolution.y, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FIT_TO_RESOLUTION type
	 *  @param targetWidth - Target resolution width
	 *  @param targetHeight - Target resolution height
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscalingFitToResolution(float targetWidth, float targetHeight) {
		return this.setResolutionDownscaling(ResolutionDownscalingType.FIT_TO_RESOLUTION, targetWidth, targetHeight, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @param targetResolution - Target resolution for FIT_TO_RESOLUTION type
	 *  @param targetFactor - Target resolution factor for FACTOR type
	 *  @throws NullPointerException if <b>targetResolution</b> is null
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscaling(ResolutionDownscalingType type, Vector2 targetResolution, float targetFactor) {
		if (targetResolution == null) {
			if (GDXPSX.THROW_EXCEPTIONS) throw new NullPointerException();
			return this;
		}
		
		return this.setResolutionDownscaling(type, targetResolution.x, targetResolution.y, targetFactor);
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @param targetWidth - Target resolution width for FIT_TO_RESOLUTION type
	 *  @param targetHeight - Target resolution height for FIT_TO_RESOLUTION type
	 *  @param targetFactor - Target resolution factor for FACTOR type
	 *  @return self **/
	public PSXPostProcessing setResolutionDownscaling(ResolutionDownscalingType type, float targetWidth, float targetHeight, float targetFactor) {
		this.rdType = type != null ? type : ResolutionDownscalingType.NONE;
		this.rdTargetResolution.set(MathUtils.clamp(targetWidth, 1, this.inputResolution.x), MathUtils.clamp(targetHeight, 1, this.inputResolution.y));
		this.rdFactor = Math.max(1, targetFactor);
		
		if (this.shaderProgram != null && this.isDynamic && EXPLICIT_DYNAMIC_UNIFORM_UPDATES) {
			this.bindResolutionDownscalingUniform();
		}
		return this;
	}
	
	/** Disables dithering matrix filter 
	 *  @return self **/
	public PSXPostProcessing resetDitheringMatrix() {
		this.ditheringMatrix = null;
		return this;
	}
	
	/** Sets parameters for dithering matrix, it's scale and texture target.
	 *  @param ditheringMatrix - DitheringMatrix object that contains matrix texture and it's sizes
	 *  @param glTarget - Active texture target for dithering matrix texture, from 0 to 31 (inclusive)
	 *  @param colorDepth - colorDepth for dithering from 1 to 255
	 *  @param scale - Dithering matrix scale
	 *  @return self **/
	public PSXPostProcessing setDitheringMatrix(DitheringMatrix ditheringMatrix, int glTarget, float colorDepth, float scale) {
		return this._setDitheringMatrix(ditheringMatrix, glTarget, colorDepth, scale, true);
	}
	
	/** Should be a hidden method copy of {@link #setDitheringMatrix(DitheringMatrix, int, float)},
	 *    but contains additional parameter <code>loadTexture</code>
	 *  Sets parameters for dithering matrix, it's scale and texture target.
	 *  @param ditheringMatrix - DitheringMatrix object that contains matrix texture and it's sizes
	 *  @param glTarget - Active texture target for dithering matrix texture, from 0 to 31 (inclusive)
	 *  @param colorDepth - colorDepth for dithering from 1 to 255
	 *  @param scale - Dithering matrix scale
	 *  @param loadTexture - if true, loads Texture from DitheringMatrix object
	 *  @return self **/
	public PSXPostProcessing _setDitheringMatrix(DitheringMatrix ditheringMatrix, int glTarget, float colorDepth, float scale, boolean loadTexture) {
		final boolean isTheSameMatrix = this.ditheringMatrix == ditheringMatrix;
		
		this.ditheringMatrix = ditheringMatrix;
		this.ditheringScale = Math.max(0.01f, scale);
		this.ditheringColorDepth = MathUtils.clamp(colorDepth, 1, 255);
		
		if (!isTheSameMatrix && this.ditheringTexture != null) {
			this.ditheringTexture.dispose();
			this.ditheringTexture = null;
		}
		
		if (!isTheSameMatrix && ditheringMatrix != null && loadTexture) {
			this.ditheringTexture = ditheringMatrix.obtainTexture();
		}
		
		if (this.shaderProgram != null && this.isDynamic && EXPLICIT_DYNAMIC_UNIFORM_UPDATES) {
			this.bindDitheringMatrixUniform();
		}
		
		return this.setDitheringMatrixTarget(glTarget);
	}
	
	/** Sets some parameters for dithering filter 
	 *  @param colorDepth - colorDepth for dithering from 1 to 255 
	 *  @param scale - Dithering matrix scale
	 *  @return self **/
	public PSXPostProcessing setDitheringMatrixParameters(float colorDepth, float scale) {
		this.ditheringScale = Math.max(0.01f, scale);
		this.ditheringColorDepth = MathUtils.clamp(colorDepth, 1, 255);
		
		if (this.shaderProgram != null && this.isDynamic && EXPLICIT_DYNAMIC_UNIFORM_UPDATES) {
			this.bindDitheringMatrixUniform();
		}
		
		return this;
	}
	
	/** Sets active texture target for dithering matrix texture
	 *  @param glTarget - Active texture target for dithering matrix texture, from 0 to 31 (inclusive)
	 *  @return self **/
	public PSXPostProcessing setDitheringMatrixTarget(int glTarget) {
		if (glTarget > -1 && glTarget < 32) {
			this.ditheringMatrixTextureTarget = glTarget;
			
			if (this.shaderProgram != null && this.isDynamic && EXPLICIT_DYNAMIC_UNIFORM_UPDATES) {
				this.shaderProgram.setUniformi(UNIFORM_DITHERINGMATRIX_SAMPLER, glTarget);
			}
		} else GDXPSX.log(Level.WARNING, "You need to provide GL Active texture ID between 0 and 31 (inclusive)");
		return this;
	}
	
	/** Compiles shaders and creates ShaderProgram object
	 *  @param isDynamic By enabling this value you can change parameters on the fly without the
	 *    need to recompile the shader, otherwise shader should be recompiled for changes to be applied.
	 *  @throws GdxRuntimeException if shader failed compilation and {@link GDXPSX#THROW_EXCEPTIONS} is enabled.
	 *  @return true if compilation was successful */
	public boolean compile(boolean isDynamic) {
		if (this.shaderProgram != null) this.shaderProgram.dispose();
		if (VALIDATE_PARAMETERS && !validateParameters()) return false;
		
		String prefix = GDXPSX.EMPTY;
		if (!isDynamic) {
			prefix += this.getResolutionDownscalingShaderPrefix();
			prefix += this.getDitheringMatrixShaderPrefix();
		}
		
		this.isDynamic = isDynamic;
		this.shaderProgram = new ShaderProgram(prefix + this.getVertexShader(), prefix + this.getFragmentShader());
		if (!this.shaderProgram.isCompiled()) {
			GDXPSX.log(Level.SEVERE, "Unable to compile shader.\n" + this.shaderProgram.getLog());
			return false;
		}
		
		if (SHADER_BINDING_BUFFER != null) {
			// trying to avoid already full buffer, but i'm not sure it will work properly
			if (SHADER_BINDING_BUFFER.position() > 0) SHADER_BINDING_BUFFER.flip();
			Gdx.gl20.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, SHADER_BINDING_BUFFER);
		}
		
		this.shaderProgram.bind();
		if (this.ditheringMatrix != null) {
			this.shaderProgram.setUniformi(UNIFORM_DITHERINGMATRIX_SAMPLER, this.ditheringMatrixTextureTarget);
		}
		
		if (isDynamic) {
			this.bindResolutionDownscalingUniform();
			this.bindDitheringMatrixUniform();
		}
		
		if (SHADER_BINDING_BUFFER != null) {
			SHADER_BINDING_BUFFER.flip();
			Gdx.gl20.glUseProgram(SHADER_BINDING_BUFFER.get());
			SHADER_BINDING_BUFFER.flip();
		}
		
		return true;
	}
	
	/** Validates post-processing parameters and throws an exception if something is wrong
	 *    and {@link GDXPSX#THROW_EXCEPTIONS} is enabled, or returns true if everything is ok.
	 * @return true if validation succeed, or false if something gone wrong and exception
	 *   or log message is dispatched **/
	public boolean validateParameters() {
		if (this.inputResolution == null) {
			GDXPSX.log(Level.SEVERE, "Input resolution can't be null!");
			return false;
		}
		
		if (this.inputResolution.x < 1 || this.inputResolution.y < 1) {
			GDXPSX.log(Level.SEVERE, "Input resolution can't be less than 1!");
			return false;
		}
		
		if (this.rdType == null) {
			GDXPSX.log(Level.SEVERE, "Resolution Downscaling type can't be null!");
			return false;
		}
		
		switch (this.rdType) {
			case FACTOR: {
				if (this.rdFactor < 1) {
					GDXPSX.log(Level.SEVERE, "Resolution Downscaling factor can't be less than 1!");
				}
			} break;
			case FIT_TO_RESOLUTION: {
				if (this.rdTargetResolution == null) {
					GDXPSX.log(Level.SEVERE, "Resolution Downscaling target resolution can't be null!");
					return false;
				}
				
				if (this.rdTargetResolution.x < 1 || this.rdTargetResolution.y < 1) {
					GDXPSX.log(Level.SEVERE, "Resolution Downscaling target resolution can't be less than 1!");
					return false;
				}
			}
		}
		
		if (this.ditheringMatrix != null) {
			if (this.ditheringScale < 0) {
				GDXPSX.log(Level.SEVERE, "Dithering scale can't be less than 0!");
				return false;
			}
			
			if (this.ditheringColorDepth < 1 || this.ditheringColorDepth > 255) {
				GDXPSX.log(Level.SEVERE, "Dithering color depth is out of bounds, it must be between 1 - 255 (inclusive)!");
				return false;
			}
			
			// hmm, this probably will be not very good, but there's no pass for dithering texture...
		}
		
		return true;
	}
	
	/** Binds resolution downscaling parameters to a shader. 
	 *  Slightly strange but i think it's better way to use downscaling to replace
	 *    unnecessary if-else statement in fragment shader with 2 multiplications **/
	public void bindResolutionDownscalingUniform() {
		float resX = this.inputResolution.x, resY = this.inputResolution.y;
		
		switch (this.rdType) {
			case NONE: {
				this.shaderProgram.setUniformf(UNIFORM_RESOLUTIONDOWNSCALING, resX, resY, 0, 0);
				return;
			}
			
			case FACTOR: {
				resX /= this.rdFactor;
				resY /= this.rdFactor;
			} break;
			case FIT_TO_RESOLUTION: {
				resX = MathUtils.clamp(this.inputResolution.x / this.rdTargetResolution.x, 1, this.inputResolution.x);
				resY = MathUtils.clamp(this.inputResolution.y / this.rdTargetResolution.y, 1, this.inputResolution.y);
			} break;
		}
		
		this.shaderProgram.setUniformf(UNIFORM_RESOLUTIONDOWNSCALING, resX, resY, 1.0f / resX, 1.0f / resY);
	}
	
	/** Binds dithering matrix uniform data
	 *  (including resolution downscaling factor as 4th argument) **/
	protected void bindDitheringMatrixUniform() {
		if (this.ditheringMatrix != null) {
			
			// Getting factors for [X, Y] dimensions from Resolution Downscaling to apply it on Dithering
			float rdScaleX = 1, rdScaleY = 1;
			switch (this.rdType) {
				case FACTOR: {
					rdScaleX = rdScaleY = this.rdFactor;
				} break;
				case FIT_TO_RESOLUTION: {
					rdScaleX = Math.max(1, this.inputResolution.x / this.rdTargetResolution.x);
					rdScaleY = Math.max(1, this.inputResolution.y / this.rdTargetResolution.y);
				} break;
			}
			
			// Applying all scaling onto dithering
			this.shaderProgram.setUniformf(UNIFORM_DITHERINGMATRIX_DATA,
				this.ditheringMatrix.textureWidth * rdScaleX * this.ditheringScale,
				this.ditheringMatrix.textureHeight * rdScaleY * this.ditheringScale,
				this.ditheringColorDepth
			);
		} else {
			this.shaderProgram.setUniformf(UNIFORM_DITHERINGMATRIX_DATA, 0, 0, 0);
		}
	}
	
	/** @return String of definition with resolution downscaling information 
	 *    that used in static shader as a preprocessor commands **/
	protected String getResolutionDownscalingShaderPrefix() {
		float resX = this.inputResolution.x, resY = this.inputResolution.y;
		
		switch (this.rdType) {
			case NONE: {
				// nerdy hack to format decimals with dots as separator
				return String.format(Locale.US, "#define RESOLUTION vec4(%.5f, %.5f, 0.0, 0.0)\n", resX, resY);
			}
			
			case FACTOR: {
				resX /= this.rdFactor;
				resY /= this.rdFactor;
			} break;
			case FIT_TO_RESOLUTION: {
				resX = MathUtils.clamp(this.inputResolution.x / this.rdTargetResolution.x, 1, this.inputResolution.x);
				resY = MathUtils.clamp(this.inputResolution.y / this.rdTargetResolution.y, 1, this.inputResolution.y);
			} break;
		}
		
		// nerdy hack to format decimals with dots as separator
		return String.format(Locale.US, "#define DOWNSCALING\n#define RESOLUTION vec4(%.5f, %.5f, %.5f, %.5f)\n", resX, resY, 1.0f / resX, 1.0f / resY);
	}
	
	/** @return String of definition with dithering information that
	 *    used in static shader as a preprocessor commands **/
	protected String getDitheringMatrixShaderPrefix() {
		if (this.ditheringMatrix != null) {
			// Getting factors for [X, Y] dimensions from Resolution Downscaling to apply it on Dithering
			float rdScaleX = 1, rdScaleY = 1;
			switch (this.rdType) {
				case FACTOR: {
					rdScaleX = rdScaleY = this.rdFactor;
				} break;
				case FIT_TO_RESOLUTION: {
					rdScaleX = Math.max(1, this.inputResolution.x / this.rdTargetResolution.x);
					rdScaleY = Math.max(1, this.inputResolution.y / this.rdTargetResolution.y);
				} break;
			}
			
			// nerdy hack to format decimals with dots as separator
			return String.format(Locale.US, "#define DITHERING vec3(%.5f, %.5f, %.5f)\n",
				this.ditheringMatrix.textureWidth * rdScaleX * this.ditheringScale,
				this.ditheringMatrix.textureHeight * rdScaleY * this.ditheringScale,
				this.ditheringColorDepth
			);
		}
		return GDXPSX.EMPTY;
	}
	
	/** Does nothing if dithering is disabled, or binds Dithering Matrix Texture 
	 *    to {@link #ditheringMatrixTextureTarget} active texture target **/
	public void bindDitheringMatrixTexture() {
		if (this.ditheringTexture != null && this.ditheringMatrixTextureTarget > -1 && this.ditheringMatrixTextureTarget < 32) {
			this.ditheringTexture.bind(this.ditheringMatrixTextureTarget);
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		}
	}
	
	/** @return Dithering matrix texture if it's loaded, otherwise it will be a null **/
	public Texture getDitheringMatrixTexture() {
		return this.ditheringTexture;
	}
	
	/** @return Active texture target for dithering matrix texture **/
	public int getDitheringMatrixTextureTarget() {
		return this.ditheringMatrixTextureTarget;
	}
	
	/** @return {@link ShaderProgram} with shader, or null if {@link ShaderProgram} is not created **/
	public ShaderProgram getShaderProgram() {
		return this.shaderProgram;
	}
	
	/** @return true when shaderprogram is compiled **/
	public boolean isCompiled() {
		return this.shaderProgram != null && this.shaderProgram.isCompiled();
	}

	/** Vertex shader source boilerplate method :b
	 * @return vertex shader source **/
	protected String getVertexShader() {
		return Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.vert").readString();
	}
	
	/** Fragment shader source boilerplate method :b
	 * @return fragment shader source **/
	protected String getFragmentShader() {
		return this.isDynamic ? 
				Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.dynamic.frag").readString() :
				Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.static.frag").readString();
	}

	@Override
	public void dispose() {
		if (this.shaderProgram != null) {
			this.shaderProgram.dispose();
			this.shaderProgram = null;
		}
		
		if (this.ditheringTexture != null) {
			this.ditheringTexture.dispose();
			this.ditheringTexture = null;
		}
	}
}
