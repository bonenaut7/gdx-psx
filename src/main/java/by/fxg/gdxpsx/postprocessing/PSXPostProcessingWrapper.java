package by.fxg.gdxpsx.postprocessing;

import java.util.logging.Level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import by.fxg.gdxpsx.GDXPSX;

/** Built-in part of previously used {@link PSXPostProcessingLegacy} <br><br>
 *  
 *  Now this part is separated from {@link PSXPostProcessing} and used just for capturing
 *    data and then rendering final image with applied post-processing effects. <br><br>
 *  
 *  You can construct this wrapper with your own {@link PSXPostProcessing} via
 *    {@link #PSXPostProcessingWrapper(PSXPostProcessing)} or create both of them via
 *    {@link #PSXPostProcessingWrapper()}. <br>
 *  If the second variant is chosen then you can acquire {@link PSXPostProcessing} from
 *    {@link #getPostProcessing()}. <br><br>
 *    
 *  To capture data you need to create a framebuffer or use your own, it must have at least
 *    one color buffer texture.
 *  To create use the {@link #createFrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format, int, int, boolean)}. <br>
 *  After you created framebuffer you need to use {@link #beginFrameBufferCapture()}, and after drawing
 *    something call {@link #endFrameBufferCapture()} to stop the capture. <br>
 *    
 *  To get the result you can use {@link #drawPostProcessedTexture(Batch, boolean)} to draw it with your batch.
 */
public class PSXPostProcessingWrapper implements Disposable {
	/** If this option enabled, then draw methods will cache active shader and will bind it after drawing
	 *    {@link #frameBuffer}'s color buffer texture **/
	public static boolean CACHE_BATCH_SHADER = true;

	/** Few checks for {@link #drawPostProcessedTexture(Batch, int, int, int, int, boolean)} used to 
	 *    find out what {@link Batch} is doing(drawing or not drawing). If batch is not drawing, then
	 *    if this option is turned on batch will start to draw and will end and flush after drawing
	 *    {@link #frameBuffer}'s color buffer texture. Otherwise batch will be only flushed. **/
	public static boolean AUTOMATICALLY_BEGIN_BATCH_DRAW = true;
	
	/** {@link PSXPostProcessing} object that contains shader for applying effects **/
	protected PSXPostProcessing postProcessing;
	
	/** {@link FrameBuffer} that will be used to capture things :b **/
	protected FrameBuffer frameBuffer;
	
	/** Constructor that creates {@link PSXPostProcessing} with default parameters **/
	public PSXPostProcessingWrapper() {
		this.postProcessing = new PSXPostProcessing();
		this.postProcessing.setDefaultParametersWithResolution();
	}
	
	/** Construct this wrapper with your own {@link PSXPostProcessing} **/
	public PSXPostProcessingWrapper(PSXPostProcessing postProcessing) {
		this.postProcessing = postProcessing;
	}
	
	/** Sets your own {@link PSXPostProcessing} and disposes previous {@link PSXPostProcessing}
	 *  @param postProcessing - your own {@link PSXPostProcessing} object **/
	public PSXPostProcessingWrapper setPostProcessing(PSXPostProcessing postProcessing) {
		return this.setPostProcessing(postProcessing, true);
	}
	
	/** Sets your own {@link PSXPostProcessing} and disposes previous {@link PSXPostProcessing}(optional)
	 *  @param postProcessing - your own {@link PSXPostProcessing} object
	 *  @param disposePreviousPostProcessing - set as true to dispose previous {@link PSXPostProcessing} **/
	public PSXPostProcessingWrapper setPostProcessing(PSXPostProcessing postProcessing, boolean disposePreviousPostProcessing) {
		if (disposePreviousPostProcessing && this.postProcessing != null) {
			this.postProcessing.dispose();
		}
		
		this.postProcessing = postProcessing;
		return this;
	}
	
	/** Creates {@link FrameBuffer} with {@link Pixmap.Format#RGBA8888} format,
	 *   depth attachment and size of app window ({@link Gdx#graphics})
	 *  @return self **/
	public PSXPostProcessingWrapper createFrameBuffer() {
		return this.createFrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	}
	
	/** Creates {@link FrameBuffer} with {@link Pixmap.Format#RGBA8888} format
	 *   and depth attachment
	 *  @param width - width for framebuffer
	 *  @param height - height for framebuffer
	 *  @return self **/
	public PSXPostProcessingWrapper createFrameBuffer(int width, int height) {
		return this.createFrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
	}
	
	/** Creates {@link FrameBuffer} (wow)
	 *  @param format - {@link Pixmap.Format} for color buffer texture
	 *  @param width - width for framebuffer
	 *  @param height - height for framebuffer
	 *  @param hasDepth - marker for creating depth attachment
	 *  @return self **/
	public PSXPostProcessingWrapper createFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
		if (format == null) {
			GDXPSX.log(Level.SEVERE, "format can't be null!");
			return this;
		}
		
		if (this.frameBuffer != null) {
			this.frameBuffer.dispose();
		}
		
		this.frameBuffer = new FrameBuffer(format, width, height, hasDepth);
		return this;
	}
	
	/** Sets your own {@link FrameBuffer} and disposes previous {@link FrameBuffer}
	 *  @implNote Your {@link FrameBuffer} should contain at least 1 color buffer
	 *    texture!**/
	public PSXPostProcessingWrapper setFrameBuffer(FrameBuffer frameBuffer) {
		return this.setFrameBuffer(frameBuffer, true);
	}
	
	/** Sets your own {@link FrameBuffer} and disposes previous {@link FrameBuffer}(optional)
	 *  @param disposePreviousFrameBuffer - set as true to dispose previous {@link FrameBuffer}
	 *  @implNote Your {@link FrameBuffer} should contain at least 1 color buffer
	 *    texture!**/
	public PSXPostProcessingWrapper setFrameBuffer(FrameBuffer frameBuffer, boolean disposePreviousFrameBuffer) {
		if (disposePreviousFrameBuffer && this.frameBuffer != null) {
			this.frameBuffer.dispose();
		}
		
		this.frameBuffer = frameBuffer;
		return this;
	}
	
	/** Boilerplate {@link #frameBuffer} method, begins capture **/
	public void beginFrameBufferCapture() {
		this.frameBuffer.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}
	
	/** Boilerplate {@link #frameBuffer} method, stops capture **/
	public void endFrameBufferCapture() {
		this.frameBuffer.end();
	}
	
	/** @see #drawPostProcessedTexture(Batch, int, int, int, int)
	 * 
	 *  @param batch - {@link Batch} used for rendering {@link #frameBuffer}'s color buffer texture
	 *  @return status of rendering (false - failure, true - success) :/ **/
	public boolean drawPostProcessedTexture(Batch batch) {
		return this.drawPostProcessedTexture(batch, 0, 0, this.frameBuffer.getWidth(), this.frameBuffer.getHeight());
	}
	
	/** Draws {@link #frameBuffer}'s color buffer texture to
	 *    a {@link Batch} from parameters with <code>x, y, width, height</code>
	 *    dimensions from parameters. <br>
	 *  Automatically begins and ends {@link Batch} if {@link #AUTOMATICALLY_BEGIN_BATCH_DRAW} enabled,
	 *    otherwise just flushes {@link Batch} or throws GdxRuntimeException if batch is not
	 *    ready for rendering {@link #frameBuffer}'s color buffer texture. <br>
	 *  Automatically switches back to used before shader if {@link #CACHE_BATCH_SHADER} enabled.
	 *  
	 *  @param batch - {@link Batch} used for rendering {@link #frameBuffer}'s color buffer texture
	 *  @param x - starting X coordinate
	 *  @param y - starting Y coordinate
	 *  @param width - width of region where texture will be rendered
	 *  @param height - height of region where texture will be rendered
	 *  @throws GdxRuntimeException - if {@link Batch} is null, {@link #postProcessing} is null,
	 *    {@link #frameBuffer} is not created, if {@link Batch#begin()} not used before calling
	 *    this method and {@link #AUTOMATICALLY_BEGIN_BATCH_DRAW} is false (disabled).
	 *  @return status of rendering (false - failure, true - success) :/
	 *  **/
	public boolean drawPostProcessedTexture(Batch batch, int x, int y, int width, int height) {
		if (batch == null) {
			GDXPSX.log(Level.SEVERE, "batch can't be null!");
			return false;
		}
		
		if (this.postProcessing == null) {
			GDXPSX.log(Level.SEVERE, "PSXPostProcessing can't be null, create it first!");
			return false;
		}
		
		if (!this.postProcessing.isCompiled()) {
			GDXPSX.log(Level.WARNING, "PSXPostProcessing is not ready for rendering, compile shader first!");
			return false;
		}
		
		if (this.frameBuffer == null) {
			GDXPSX.log(Level.SEVERE, "FrameBuffer can't be null, create it first!");
			return false;
		}
		
		final ShaderProgram cachedShaderProgram = batch.getShader();
		final boolean isDrawing = batch.isDrawing();
		batch.setShader(this.postProcessing.getShaderProgram());
		
		if (!isDrawing && AUTOMATICALLY_BEGIN_BATCH_DRAW) {
			batch.begin(); // if batch is not drawing then we should start to draw :/
		}
		
		// binding dithering matrix texture
		this.postProcessing.bindDitheringMatrixTexture();
		
		// drawing our frameBuffer's color buffer texture (with inversed by Y texture coordinates)
		batch.draw(this.frameBuffer.getColorBufferTexture(), x, y, width, height, 0, 0, 1, 1);
		batch.flush();
		
		if (!isDrawing && AUTOMATICALLY_BEGIN_BATCH_DRAW) {
			// if batch wasn't drawing before and we initiated drawing process, then we should
			//   end the batch too
			batch.end(); 
		} else {
			batch.flush(); // if batch was drawing, we need to flush it to switch shader later
		}
		
		if (CACHE_BATCH_SHADER) {
			batch.setShader(cachedShaderProgram); // switching shader if it's needed
		}
		
		return true;
	}
	
	/** @return {@link PSXPostProcessing} object used in this wrapper, or null if it doesn't exist **/
	public PSXPostProcessing getPostProcessing() {
		return this.postProcessing;
	}
	
	/** @return {@link FrameBuffer} object, or null if it doesn't exist **/
	public FrameBuffer getFrameBuffer() {
		return this.frameBuffer;
	}
	
	/** Disposes {@link #frameBuffer} **/
	public void disposeFrameBuffer() {
		if (this.frameBuffer != null) {
			this.frameBuffer.dispose();
			this.frameBuffer = null;
		}
	}
	
	@Override
	public void dispose() {
		if (this.frameBuffer != null) {
			this.frameBuffer.dispose();
		}
	}
}
