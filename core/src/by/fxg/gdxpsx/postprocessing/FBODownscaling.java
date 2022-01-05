/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** 
 *  This <b>Post-processing</b> class using for screen resolution downscaling using integrated shader.
 *  @author fxgaming (FXG)
 */
public final class FBODownscaling {
	public static final float DEFAULT_INTENSITY = 2.0f;
	public static final float DEFAULT_COLOR_DEPTH = 64.0f;
	public static final DitherMatrix DEFAULT_DITHER_MATRIX = DitherMatrix.Dither2x2;
	public static final float DEFAULT_DITHER_DEPTH = 32.0f;
	
	private String vertexShader = "attribute vec4 a_position; attribute vec2 a_texCoord0; uniform mat4 u_projTrans; varying vec2 v_texCoords; varying vec2 v_position; void main() { v_texCoords = a_texCoord0; v_position = a_position.xy; gl_Position =  u_projTrans * a_position; }";
	private String fragmentShader = null;
	private ShaderProgram program;
	private FrameBuffer frameBuffer;
	private SpriteBatch batch;
	private TextureRegion lastBufferTexture;
	
	private boolean resolutionFlag;
	private boolean colorDepthFlag;
	private boolean ditheringFlag;
	
	private Vector2 resolution;
	private float intensity;
	private Vector3 colorDepth;
	private DitherMatrix ditherMatrix;
	private float ditherDepth;

	/** Creating downscaling framebuffer with given parameters 
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 */
	public FBODownscaling(float width, float height) { this(Format.RGBA8888, width, height); }
	
	/** Creating downscaling framebuffer with given parameters 
	 *  @param colorFormat - color format of {@link FrameBuffer}
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 */
	public FBODownscaling(Format colorFormat, float width, float height) {
		this.resolution = new Vector2(width, height).set(width, height);
		this.colorDepth = new Vector3(DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH, DEFAULT_COLOR_DEPTH);
		this.ditherMatrix = DEFAULT_DITHER_MATRIX;
		this.ditherDepth = DEFAULT_DITHER_DEPTH;

		this.frameBuffer = new FrameBuffer(colorFormat, (int)width, (int)height, true);
		Texture texture = this.frameBuffer.getColorBufferTexture();
		texture.setAnisotropicFilter(4f);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.lastBufferTexture = new TextureRegion(texture);
		this.lastBufferTexture.flip(false, true);
		this.batch = new SpriteBatch();
		
		this.fragmentShader = Gdx.files.classpath("by/fxg/gdxpsx/shaders/general.fbo.vert").readString();
	}
	
	/** Initialization of shader(shader compile). You can adjust shader abilities with parameters below.
	 *  @param enableAlpha - enabling alpha channel
	 *  @param enableDownscaling - enabling resolution downscaling
	 *  @param enableColorDepth - enabling color depth changing
	 *  @param enableDithering - enabling dithering
	 */
	public FBODownscaling init(boolean enableAlpha, boolean enableDownscaling, boolean enableColorDepth, boolean enableDithering) {
		StringBuilder stringBuilder = new StringBuilder();
		if (enableAlpha) stringBuilder.append("#define GDX_PSX_ENABLEALPHA\n");
		if (this.resolutionFlag = enableDownscaling) stringBuilder.append("#define GDXPSX_RESOLUTION\n");
		if (this.colorDepthFlag = enableColorDepth) stringBuilder.append("#define GDXPSX_COLORDEPTH\n");
		if (this.ditheringFlag = enableDithering) stringBuilder.append("#define GDXPSX_DITHERING\n");
		stringBuilder.append(this.fragmentShader);
		if (this.program != null) this.program.dispose();
		this.program = new ShaderProgram(this.vertexShader, stringBuilder.toString());
		this.batch.setShader(this.program);
		return this;
	}
	
	/** @param width - width of resultion
	 *  @param height - height of resultion
	 */
	public FBODownscaling setResolution(float width, float height) {
		this.resolution.set(width, height);
		return this;	
	}
	
	/** @param intensity - intensity of downscaling. Default: 2. {@link FBODownscaling#DEFAULT_INTENSITY} **/
	public FBODownscaling setIntensity(float intensity) {
		this.intensity = Math.max(1, intensity);
		return this;
	}
	
	/** Sets the color depth of screen.
	 *  @param red - red channel
	 *  @param green - green channel
	 *  @param blue - blue channel
	 */
	public FBODownscaling setColorDepth(float red, float green, float blue) {
		this.colorDepth.set(Math.max(1, red), Math.max(1, green), Math.max(1, blue));
		return this;
	}
	
	/** @param ditherMatrix - dither matrix preset **/
	public FBODownscaling setDitherMatrix(DitherMatrix ditherMatrix) {
		if (ditherMatrix != null) this.ditherMatrix = ditherMatrix;
		return this;
	}
	
	/** @param ditherDepth - color depth of dithering **/
	public FBODownscaling setDitherDepth(float ditherDepth) {
		this.ditherDepth = Math.max(1, ditherDepth);
		return this;
	}
	
	/** Sets filtering of screen-texture. 
	 *  @param min - Minification filter. Default: Nearest. {@link TextureFilter#Nearest}
	 *  @param mag - Magnification filter. Default: Nearest. {@link TextureFilter#Nearest}
	 */
	public FBODownscaling setFilter(TextureFilter min, TextureFilter mag) {
		this.lastBufferTexture.getTexture().setFilter(min, mag);
		return this;
	}
	
	/** Begin of framebuffer. Use method before rendering. **/
	public void capture() {
		if (this.resolutionFlag) {
			this.program.setUniformf("u_resX", this.resolution.x);
			this.program.setUniformf("u_resY", this.resolution.y);
			this.program.setUniformf("u_intensity", this.intensity);
		}
		if (this.colorDepthFlag) {
			this.program.setUniformf("u_colorDepthRed", this.colorDepth.x);
			this.program.setUniformf("u_colorDepthGreen", this.colorDepth.y);
			this.program.setUniformf("u_colorDepthBlue", this.colorDepth.z);
		}
		if (this.ditheringFlag) {
			this.program.setUniformi("u_ditheringMatrix", this.ditherMatrix.ordinal());
			this.program.setUniformf("u_ditheringDepth", this.ditherDepth);
		}
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
	public void drawFrame() { this.drawFrame(0, 0, (int)this.resolution.x, (int)this.resolution.y); }

	/** Downscaled image render. Use after {@link #endCapture()}\
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y) { this.drawFrame(x, y, (int)this.resolution.x, (int)this.resolution.y); };
	
	/** Downscaled image render. Use after {@link #endCapture()}\
	 *  @param x - start position of render in <b>X</b> dimension
	 *  @param y - start position of render in <b>Y</b> dimension
	 *  @param sizeX - size of renderable image in <b>X</b> dimension
	 *  @param sizeY - size of renderable image in <b>Y</b> dimension
	 */
	public void drawFrame(int x, int y, int sizeX, int sizeY) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
		this.batch.begin();
		this.batch.draw(this.lastBufferTexture, x, y, sizeX, sizeY);
		this.batch.end();
	}
	
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
