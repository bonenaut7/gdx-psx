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

package by.fxg.gdxpsx2.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

/** 
 *  This <b>Post-processing</b> class using for screen resolution downscaling using integrated shader.
 *  @author fxgaming (FXG)
 */
//TODO: Filter selection
public final class FBODownscaling {
	public static float DEFAULT_INTENSITY = 2.0f;
	
	private String vertexShader = "attribute vec4 a_position; attribute vec2 a_texCoord0; uniform mat4 u_projTrans; varying vec2 v_texCoords; void main() { v_texCoords = a_texCoord0; gl_Position = u_projTrans * a_position; }";
	private String fragmentShader = "varying vec2 v_texCoords; uniform sampler2D u_texture; uniform float u_resX; uniform float u_resY; uniform float u_intensity; void main() { float dx = u_intensity*(1./u_resX); float dy = u_intensity*(1./u_resY); vec2 coord = vec2(dx * floor(v_texCoords.x / dx), dy * floor(v_texCoords.y / dy)); gl_FragColor = texture(u_texture, coord); if (gl_FragColor.a < 0.5) { discard; } }";
	private ShaderProgram program;
	
	private FrameBuffer frameBuffer;
	private SpriteBatch batch;
	private TextureRegion lastBufferTexture;
	
	private Vector2 resolution;
	private float intensity;

	/** Creating downscaling framebuffer with default settings */
	public FBODownscaling() { this(2.0F); }
	
	/** Creating downscaling framebuffer with given parameters 
	 *  @param intensity - intensity of downscaling
	 */
	public FBODownscaling(float intensity) { this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), intensity); }

	/** Creating downscaling framebuffer with given parameters 
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 *  @param intensity - intensity of downscaling
	 */
	public FBODownscaling(float width, float height, float intensity) { this(Format.RGBA8888, width, height, intensity); }
	
	/** Creating downscaling framebuffer with given parameters 
	 *  @param colorFormat - color format of {@link FrameBuffer}
	 *  @param width - width of resultion, width of framebuffer {@link FrameBuffer}
	 *  @param height - height of resultion, height of framebuffer {@link FrameBuffer}
	 *  @param intensity - intensity of downscaling
	 */
	public FBODownscaling(Format colorFormat, float width, float height, float intensity) {
		this.resolution = new Vector2(width, height);
		this.intensity = intensity;
		
		this.frameBuffer = new FrameBuffer(colorFormat, (int)width, (int)height, true);
		Texture texture = this.frameBuffer.getColorBufferTexture();
		texture.setAnisotropicFilter(4f);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.MipMapNearestNearest);
		this.lastBufferTexture = new TextureRegion(texture);
		this.lastBufferTexture.flip(false, true);
		this.batch = new SpriteBatch();
		
		this.program = new ShaderProgram(this.vertexShader, this.fragmentShader);
		this.batch.setShader(this.program);
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
		this.intensity = intensity;
		return this;
	}
	
	/** Begin of framebuffer. Use method before rendering. **/
	public void capture() {
		this.program.setUniformf("u_resX", this.resolution.x);
		this.program.setUniformf("u_resY", this.resolution.y);
		this.program.setUniformf("u_intensity", this.intensity);
		this.frameBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
	}
	
	/** Begin of framebuffer. Use method before rendering.
	 *  @param clearColor - color for clearing buffers
	 */
	public void capture(Color clearColor) {
		this.frameBuffer.begin();
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
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
}
