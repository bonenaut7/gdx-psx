package by.fxg.gdxpsx.vfxintegration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.AbstractVfxEffect;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

import by.fxg.gdxpsx.postprocessing.DitheringMatrix;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessing;
import by.fxg.gdxpsx.postprocessing.ResolutionDownscalingType;

// At the moment of writing this, gdx-vfx doesn't support 3D(depth buffer)...
public class PSXPostProcessingEffect extends AbstractVfxEffect implements ChainVfxEffect {
	protected ShaderProgram shaderProgram;
	
	/** Original(viewport/framebuffer/etc) input texture resolution **/
	protected Vector2 inputResolution = new Vector2();
	
	/** Resolution downscaling type **/
	protected ResolutionDownscalingType rdType = ResolutionDownscalingType.NONE;
	
	/** Resolution downscaling target resolution **/
	protected Vector2 rdTargetResolution = new Vector2(1, 1);
	
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
	
	public PSXPostProcessingEffect() {
		this.shaderProgram = VfxGLUtils.compileShader(
			Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
			Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.dynamic.frag")
		);
		this.rebind();
	}
	
	@Override
	public void update(float delta) {
		// Nothing is being updated here :b
	}

	@Override
	public void rebind() {
		this.shaderProgram.bind();
		this.bindResolutionDownscalingUniform();
		this.shaderProgram.setUniformi(PSXPostProcessing.UNIFORM_DITHERINGMATRIX_SAMPLER, this.ditheringMatrixTextureTarget);
		this.bindDitheringMatrixUniform();
	}

	@Override
	public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
		buffers.getSrcBuffer().getTexture().bind(0);
		
		if (this.ditheringTexture != null) {
			this.ditheringTexture.bind(this.ditheringMatrixTextureTarget);
		}
		
		final VfxFrameBuffer destFramebuffer = buffers.getDstBuffer();
		final boolean manualBinding = !destFramebuffer.isDrawing();
		
		if (manualBinding) {
			destFramebuffer.begin();
		}
		
		this.shaderProgram.bind();
		context.getViewportMesh().render(this.shaderProgram);
		
		if (manualBinding) {
			destFramebuffer.end();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		if (this.inputResolution.epsilonEquals(width, height)) {
			return;
		}
		
		this.inputResolution.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.shaderProgram.bind();
		this.bindResolutionDownscalingUniform();
	}
	
	@Override
	public void dispose() {
		if (this.shaderProgram != null) {
			this.shaderProgram.dispose();
			this.shaderProgram = null;
		}
	}
	
	// Actually methods below are just copy-pasted from the PSXPostProcessing :b
	
	/** Disables resolution downscaling filter
	 *  @return self **/
	public PSXPostProcessingEffect resetResolutionDownscaling() {
		this.rdType = ResolutionDownscalingType.NONE;
		this.bindResolutionDownscalingUniform();
		return this;
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscaling(ResolutionDownscalingType type) {
		return this.setResolutionDownscaling(type, this.rdTargetResolution.x, this.rdTargetResolution.y, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FACTOR type
	 *  @param targetFactor - Target resolution factor
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscalingFactor(float targetFactor) {
		return this.setResolutionDownscaling(ResolutionDownscalingType.FACTOR, this.rdTargetResolution.x, this.rdTargetResolution.y, targetFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FIT_TO_RESOLUTION type
	 *  @param targetResolution - Target resolution
	 *  @throws NullPointerException if <b>targetResolution</b> is null
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscalingFitToResolution(Vector2 targetResolution) {
		return this.setResolutionDownscaling(ResolutionDownscalingType.FIT_TO_RESOLUTION, targetResolution.x, targetResolution.y, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter with FIT_TO_RESOLUTION type
	 *  @param targetWidth - Target resolution width
	 *  @param targetHeight - Target resolution height
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscalingFitToResolution(float targetWidth, float targetHeight) {
		return this.setResolutionDownscaling(ResolutionDownscalingType.FIT_TO_RESOLUTION, targetWidth, targetHeight, this.rdFactor);
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @param targetResolution - Target resolution for FIT_TO_RESOLUTION type
	 *  @param targetFactor - Target resolution factor for FACTOR type
	 *  @throws NullPointerException if <b>targetResolution</b> is null
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscaling(ResolutionDownscalingType type, Vector2 targetResolution, float targetFactor) {
		return this.setResolutionDownscaling(type, targetResolution.x, targetResolution.y, targetFactor);
	}
	
	/** Sets parameters for resolution downscaling filter
	 * 	@param type - Resolution downscaling filter type
	 *  @param targetWidth - Target resolution width for FIT_TO_RESOLUTION type
	 *  @param targetHeight - Target resolution height for FIT_TO_RESOLUTION type
	 *  @param targetFactor - Target resolution factor for FACTOR type
	 *  @return self **/
	public PSXPostProcessingEffect setResolutionDownscaling(ResolutionDownscalingType type, float targetWidth, float targetHeight, float targetFactor) {
		this.rdType = type != null ? type : ResolutionDownscalingType.NONE;
		this.rdTargetResolution.set(MathUtils.clamp(targetWidth, 1, this.inputResolution.x), MathUtils.clamp(targetHeight, 1, this.inputResolution.y));
		this.rdFactor = Math.max(1, targetFactor);
		
		this.bindResolutionDownscalingUniform();
		return this;
	}

	/** Disables dithering matrix filter 
	 *  @return self **/
	public PSXPostProcessingEffect resetDitheringMatrix() {
		this.ditheringMatrix = null;
		this.bindDitheringMatrixUniform();
		return this;
	}
	
	/** Sets parameters for dithering matrix, it's scale and texture target.
	 *  @param ditheringMatrix - DitheringMatrix object that contains matrix texture and it's sizes
	 *  @param glTarget - Active texture target for dithering matrix texture, from 0 to 31 (inclusive)
	 *  @param colorDepth - colorDepth for dithering from 1 to 255
	 *  @param scale - Dithering matrix scale
	 *  @return self **/
	public PSXPostProcessingEffect setDitheringMatrix(DitheringMatrix ditheringMatrix, int glTarget, float colorDepth, float scale) {
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
	public PSXPostProcessingEffect _setDitheringMatrix(DitheringMatrix ditheringMatrix, int glTarget, float colorDepth, float scale, boolean loadTexture) {
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
		
		this.bindDitheringMatrixUniform();
		return this.setDitheringMatrixTarget(glTarget);
	}
	
	/** Sets some parameters for dithering filter 
	 *  @param colorDepth - colorDepth for dithering from 1 to 255 
	 *  @param scale - Dithering matrix scale
	 *  @return self **/
	public PSXPostProcessingEffect setDitheringMatrixParameters(float colorDepth, float scale) {
		this.ditheringScale = Math.max(0.01f, scale);
		this.ditheringColorDepth = MathUtils.clamp(colorDepth, 1, 255);
		
		this.bindDitheringMatrixUniform();
		return this;
	}
	
	/** Sets active texture target for dithering matrix texture
	 *  @param glTarget - Active texture target for dithering matrix texture, from 0 to 31 (inclusive)
	 *  @return self **/
	public PSXPostProcessingEffect setDitheringMatrixTarget(int glTarget) {
		if (glTarget > -1 && glTarget < 32) {
			this.ditheringMatrixTextureTarget = glTarget;
			
			this.shaderProgram.setUniformi(PSXPostProcessing.UNIFORM_DITHERINGMATRIX_SAMPLER, glTarget);
		}
		return this;
	}
	
	protected void bindResolutionDownscalingUniform() {
		float resX = this.inputResolution.x, resY = this.inputResolution.y;
		
		switch (this.rdType) {
			case NONE: {
				this.shaderProgram.setUniformf(PSXPostProcessing.UNIFORM_RESOLUTIONDOWNSCALING, resX, resY, 0, 0);
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
	
		this.shaderProgram.setUniformf(PSXPostProcessing.UNIFORM_RESOLUTIONDOWNSCALING, resX, resY, 1.0f / resX, 1.0f / resY);
	}
	
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
			this.shaderProgram.setUniformf(PSXPostProcessing.UNIFORM_DITHERINGMATRIX_DATA,
				this.ditheringMatrix.textureWidth * rdScaleX * this.ditheringScale,
				this.ditheringMatrix.textureHeight * rdScaleY * this.ditheringScale,
				this.ditheringColorDepth
			);
		} else {
			this.shaderProgram.setUniformf(PSXPostProcessing.UNIFORM_DITHERINGMATRIX_DATA, 0, 0, 0);
		}
	}
}
