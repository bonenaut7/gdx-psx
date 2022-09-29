/*******************************************************************************
 * Copyright 2022 Matvey Zholudz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package by.fxg.gdxpsx.postprocessing;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** 
 *  This <b>Post-processing</b> class using for screen resolution downscaling, color depth change & dithering using integrated shader.
 *  @author fxgaming (FXG)
 */
public final class PSXPostProcessing {
	public static final float DEFAULT_INTENSITY = 4.0f;
	public static final float DEFAULT_COLOR_DEPTH = 64.0f;
	public static final DitheringMatrix DEFAULT_DITHERING_MATRIX = DitheringMatrix.BAYER_8x8;
	public static final float DEFAULT_DITHER_SCALE = 1.0f;
	
	private String vertexShader = null;
	private String fragmentShader = null;
	private ShaderProgram program;
	private FrameBuffer frameBuffer;
	private SpriteBatch batch;
	private TextureRegion lastBufferTexture;
	private Texture ditheringTexture = null;
	
	//===================[CONFIGURATION]======================
	private float[] flags = {1, 1, 1, 1};
	
	//[1] Viewport settings / Resolution downscaling
	private float[] resolution = {3, 640, 360};
	
	//[2] Color Depth / Color Depth limiting
	private float[] colorDepth = {32, 32, 32};
	
	//[3] Dithering / Image dithering
	private DitheringMatrix ditheringMatrix;
	private float[] dithering = {1, 8, 8};
	//===================[CONFIGURATION]======================

	/** Creating downscaling framebuffer with default parameters **/
	public PSXPostProcessing() { this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); }
	
	/** Creating downscaling framebuffer with given parameters 
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 */
	public PSXPostProcessing(float width, float height) { this(Format.RGBA8888, width, height); }
	
	/** Creating downscaling framebuffer with given parameters 
	 *  @param colorFormat - color format of {@link FrameBuffer}
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 */
	public PSXPostProcessing(Format colorFormat, float width, float height) {
		this.vertexShader = Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.vert").readString();
		this.fragmentShader = Gdx.files.classpath("by/fxg/gdxpsx/shaders/postprocessing.frag").readString();
		this.program = new ShaderProgram(this.vertexShader, this.fragmentShader); 
		if (!this.program.isCompiled()) {
			int prevLogLevel = Gdx.app.getLogLevel();
			Gdx.app.setLogLevel(Application.LOG_ERROR);
			Gdx.app.log("GDX-PSX", "PSXPostProcessing unable to compile shader.\nShader log:\n" + this.program.getLog());
			Gdx.app.setLogLevel(prevLogLevel);
		}
		
		this.resolution = new float[] {DEFAULT_INTENSITY, width, height};
		this.colorDepth = new float[] {DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH};
		this.ditheringMatrix = DEFAULT_DITHERING_MATRIX;
		this.dithering = new float[] {DEFAULT_DITHER_SCALE, DEFAULT_DITHERING_MATRIX.textureWidth, DEFAULT_DITHERING_MATRIX.textureHeight};
		this.ditheringTexture = this.ditheringMatrix.obtainTexture();

		this.frameBuffer = new FrameBuffer(colorFormat, (int)width, (int)height, true);
		Texture texture = this.frameBuffer.getColorBufferTexture();
		texture.setAnisotropicFilter(4f);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.lastBufferTexture = new TextureRegion(texture);
		this.lastBufferTexture.flip(false, true);
		this.batch = new SpriteBatch();
		this.batch.setShader(this.program);
	
		this.program.bind();
		this.program.setUniform1fv("u_flags", this.flags, 0, this.flags.length);
		this.program.setUniform1fv("u_resolution", this.resolution, 0, this.resolution.length);
		this.program.setUniform1fv("u_colorDepth", this.colorDepth, 0, this.colorDepth.length);
		this.program.setUniform1fv("u_dithering", this.dithering, 0, this.dithering.length);
		this.program.setUniformi("u_ditherTexture", 1);
	}

	/** Resets(recreates) {@link FrameBuffer}. **/
	public PSXPostProcessing resetFrameBuffer() { return this.resetFrameBuffer(Format.RGBA8888, (int)this.resolution[1], (int)this.resolution[2], true); }
	
	/** Resets(recreates) {@link FrameBuffer}.
	 * @param width - viewport width of {@link FrameBuffer}
	 * @param height - viewport height of {@link FrameBuffer}
	 * @param hasDepth - will {@link FrameBuffer} have depth?
	 */
	public PSXPostProcessing resetFrameBuffer(int width, int height, boolean hasDepth) { return this.resetFrameBuffer(Format.RGBA8888, width, height, hasDepth); }
	
	/** Resets(recreates) {@link FrameBuffer}.
	 * @param colorFormat - Color format of {@link FrameBuffer}
	 * @param width - viewport width of {@link FrameBuffer}
	 * @param height - viewport height of {@link FrameBuffer}
	 * @param hasDepth - will {@link FrameBuffer} have depth?
	 */
	public PSXPostProcessing resetFrameBuffer(Format colorFormat, int width, int height, boolean hasDepth) {
		if (this.lastBufferTexture.getTexture() != null) this.lastBufferTexture.getTexture().dispose();
		if (this.frameBuffer != null) this.frameBuffer.dispose();
		this.frameBuffer = new FrameBuffer(colorFormat, width, height, hasDepth);
		this.lastBufferTexture = new TextureRegion(this.frameBuffer.getColorBufferTexture());
		this.lastBufferTexture.flip(false, true);
		return this;
	}
	
	/** Changes state of the <b>flagType</b> and enables/disables it.
	 *  @param flagType - type of function
	 *  @param enabled - state
	 */
	public PSXPostProcessing setFlagState(boolean enabled, FlagType flagType) {
		if (flagType != null) {
			this.flags[flagType.ordinal()] = enabled ? 1f : -1f;
			this.flushBatch();
			this.program.setUniform1fv("u_flags", this.flags, 0, 4);
		}
		return this;
	}
	
	/** Changes state of the <b>flags[]</b> and enables/disables it.
	 *  @param enabled - state
	 *  @param flags - types of functions
	 */
	public PSXPostProcessing setFlagsState(boolean enabled, FlagType... flags) {
		for (FlagType flagType : flags) if (flagType != null) this.flags[flagType.ordinal()] = enabled ? 1f : -1f;
		this.flushBatch();
		this.program.setUniform1fv("u_flags", this.flags, 0, 4);
		return this;
	}
	
	/** Changes resolution <b>ONLY IN SHADER!</b>. If you want
	 *  to change resolution of {@link FrameBuffer}, use {@link}
	 *  @param width - width of viewport resultion
	 *  @param height - height of viewport resultion
	 */
	public PSXPostProcessing setViewportResolution(float width, float height) {
		this.resolution[1] = width;
		this.resolution[2] = height;
		this.flushBatch();
		this.program.setUniform1fv("u_resolution", this.resolution, 0, 3);
		return this;	
	}
	
	/** @param intensity - intensity of downscaling. Default: 2. {@link PSXPostProcessing#DEFAULT_INTENSITY} **/
	public PSXPostProcessing setDownscalingIntensity(float intensity) {
		this.resolution[0] = Math.max(1, intensity);
		this.flushBatch();
		this.program.setUniform1fv("u_resolution", this.resolution, 0, 3);
		return this;
	}
	
	/** Sets the color depth of screen.
	 *  @param red - red channel
	 *  @param green - green channel
	 *  @param blue - blue channel
	 */
	public PSXPostProcessing setColorDepth(float red, float green, float blue) {
		this.colorDepth[0] = Math.max(1, red);
		this.colorDepth[1] = Math.max(1, green);
		this.colorDepth[2] = Math.max(1, blue);
		this.flushBatch();
		this.program.setUniform1fv("u_colorDepth", this.colorDepth, 0, 3);
		return this;
	}
	
	/** @param ditheringMatrix - dithering matrix preset **/
	public PSXPostProcessing setDitheringMatrix(DitheringMatrix ditheringMatrix) {
		if (ditheringMatrix != null) {
			this.ditheringTexture.dispose();
			this.ditheringMatrix = ditheringMatrix;
			this.ditheringTexture = ditheringMatrix.obtainTexture();
			this.dithering[1] = ditheringMatrix.textureWidth;
			this.dithering[2] = ditheringMatrix.textureHeight;
			this.flushBatch();
			this.program.setUniform1fv("u_dithering", this.dithering, 0, 3);
		}
		return this;
	}
	
	/** @param ditherScale - scale of per-pixel dithering **/
	public PSXPostProcessing setDitherScale(float ditherScale) {
		this.dithering[0] = Math.max(1, ditherScale);
		this.flushBatch();
		this.program.setUniform1fv("u_dithering", this.dithering, 0, 3);
		return this;
	}
	
	/** Sets filtering of screen-texture. 
	 *  @param min - Minification filter. Default: Nearest. {@link TextureFilter#Nearest}
	 *  @param mag - Magnification filter. Default: Nearest. {@link TextureFilter#Nearest}
	 */
	public PSXPostProcessing setFilter(TextureFilter min, TextureFilter mag) {
		this.lastBufferTexture.getTexture().setFilter(min, mag);
		return this;
	}
	
	/** Begin of framebuffer. Use method before rendering. **/
	public void capture() {
		this.frameBuffer.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
	}

	/** End of framebuffer. Use after rendering. **/
	public void endCapture() {
		this.frameBuffer.end();
		this.lastBufferTexture = new TextureRegion(this.frameBuffer.getColorBufferTexture());
		this.lastBufferTexture.flip(false, true);
	}
	/** Downscaled image render. Use after {@link #endCapture()} **/
	public void drawFrame() { this.drawFrame(0, 0, (int)this.resolution[1], (int)this.resolution[2]); }

	/** Downscaled image render. Use after {@link #endCapture()}
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y) { this.drawFrame(x, y, (int)this.resolution[1], (int)this.resolution[2]); };
	
	/** Downscaled image render. Use after {@link #endCapture()}
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 *  @param sizeX - size of renderable image in <b>X</b> dimension
	 *  @param sizeY - size of renderable image in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y, int sizeX, int sizeY) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
		this.ditheringTexture.bind(1);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		this.batch.begin();
		this.batch.draw(this.lastBufferTexture, x, y, sizeX, sizeY);
		this.batch.end();
	}

	/** <b>WARNING! This method setting uniforms every call, used for debugging!</b><br>
	 *  Downscaled image render. Use after {@link #endCapture()}
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 *  @param sizeX - size of renderable image in <b>X</b> dimension
	 *  @param sizeY - size of renderable image in <b>Y</b> dimension
	 */
	public void drawDebugFrame(int x, int y, int sizeX, int sizeY) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
		this.program.setUniform1fv("u_flags", this.flags, 0, 4);
		this.program.setUniform1fv("u_resolution", this.resolution, 0, 3);
		this.program.setUniform1fv("u_colorDepth", this.colorDepth, 0, 3);
		this.program.setUniform1fv("u_dithering", this.colorDepth, 0, 3);
		this.batch.begin();
		this.batch.draw(this.lastBufferTexture, x, y, sizeX, sizeY);
		this.batch.end();
	}
	
	/** Flushing batch to finish drawcall and set new uniforms. **/
	private void flushBatch() {
		if (this.batch.isDrawing()) this.batch.flush();
	}
	
	/** FlagType is enum of currently available configurable functions
	 *  of post-processing unit. Used in the {@link PSXPostProcessing#setFlagEnabled(FlagType, boolean)}
	 */
	public enum FlagType {
		ALPHA_CHANNEL,
		RESOLUTION_DOWNSCALING,
		COLOR_DEPTH_LIMITING,
		DITHERING;
	}
}
