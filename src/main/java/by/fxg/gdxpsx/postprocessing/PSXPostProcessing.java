/*******************************************************************************
 * Copyright 2021 Matvey Zholudz
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
 *  This <b>Post-processing</b> class using for screen resolution downscaling using integrated shader.
 *  @author fxgaming (FXG)
 */
public final class PSXPostProcessing {
	public static final float DEFAULT_INTENSITY = 3.0f;
	public static final float DEFAULT_COLOR_DEPTH = 64.0f;
	public static final DitherMatrix DEFAULT_DITHER_MATRIX = DitherMatrix.Dither2x2;
	public static final float DEFAULT_DITHER_DEPTH = 32.0f;
	
	private String vertexShader = null;
	private String fragmentShader = null;
	private ShaderProgram program;
	private FrameBuffer frameBuffer;
	private SpriteBatch batch;
	private TextureRegion lastBufferTexture;
	
	//===================[CONFIGURATION]======================
	private float[] flags = {1, 1, 1, -1};
	
	//[1] Viewport settings / Resolution downscaling
	private float[] resolution = {3, 640, 360};
	
	//[2] Color Depth / Color Depth limiting
	private float[] colorDepth = {32, 32, 32};
	
	//[3] Dithering / Image dithering
	private DitherMatrix ditheringMatrix;
	private float ditherDepth;
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
		
		this.resolution = new float[] {DEFAULT_INTENSITY, width, height};
		this.colorDepth = new float[] {DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH};
		this.ditheringMatrix = DEFAULT_DITHER_MATRIX;
		this.ditherDepth = DEFAULT_DITHER_DEPTH;

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
		this.program.setUniformi("u_ditheringMatrix", this.ditheringMatrix.ordinal());
		this.program.setUniformf("u_ditherDepth", this.ditherDepth);
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
	public PSXPostProcessing setFlagState(FlagType flagType, boolean enabled) {
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
	public PSXPostProcessing setDitheringMatrix(DitherMatrix ditheringMatrix) {
		if (ditheringMatrix != null) {
			this.ditheringMatrix = ditheringMatrix;
			this.flushBatch();
			this.program.setUniformi("u_ditheringMatrix", this.ditheringMatrix.ordinal());
		}
		return this;
	}
	
	/** @param ditherDepth - color depth of dithering **/
	public PSXPostProcessing setDitherDepth(float ditherDepth) {
		this.ditherDepth = Math.max(1, ditherDepth);
		this.flushBatch();
		this.program.setUniformf("u_ditherDepth", this.ditherDepth);
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

	/** Downscaled image render. Use after {@link #endCapture()}\
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y) { this.drawFrame(x, y, (int)this.resolution[1], (int)this.resolution[2]); };
	
	/** Downscaled image render. Use after {@link #endCapture()}\
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 *  @param sizeX - size of renderable image in <b>X</b> dimension
	 *  @param sizeY - size of renderable image in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y, int sizeX, int sizeY) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
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
	
	/** DitheringMatrix is enum of currently available configurable dithering matrices **/
	public enum DitherMatrix {
		Dither2x2(0.25F, new float[]{0, 3, 2, 1}),
		Dither4x4(0.0625F, new float[]{0,  8,  2,  10, 12, 4,  14, 6, 3,  11, 1,  9, 15, 7, 13, 5}),
		ScreenDoor4x4(0.0625F, new float[]{1, 9, 3, 11, 13, 5, 15, 7, 4, 12, 2, 10, 16, 8, 14, 6}),
		index8x8(0.03125F, new float[]{0, 32, 8, 40, 2, 34, 10, 42, 48, 16, 56, 24, 50, 18, 58, 26, 12, 44, 4, 36, 14, 46, 6, 38, 60, 28, 52, 20, 62, 30, 54, 22, 3, 35, 11, 43, 1, 33, 9, 41, 51, 19, 59, 27, 49, 17, 57, 25, 15, 47, 7, 39, 13, 45, 5, 37, 63, 31, 55, 23, 61, 29, 53, 21});
		
		public float[] values;
		public float ditheringMatrixSize;
		public float thresholdMultiplier;
		
		DitherMatrix(float thresholdMultiplier, float[] values) {
			this.thresholdMultiplier = thresholdMultiplier;
			this.ditheringMatrixSize = (float)Math.sqrt(values.length);
			this.values = values;
		}
	}
}
