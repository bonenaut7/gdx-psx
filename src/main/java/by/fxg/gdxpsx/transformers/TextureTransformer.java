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

package by.fxg.gdxpsx.transformers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/** 
 *  This class using for texture modifying (<b>Color depth change, Resizing</b>).
 *  @author fxgaming (FXG)
 *  @deprecated Will be removed in 2.1 and replaced with attribute for the {@link PSXShader} and {@link PSXShaderProvider}
 */
@Deprecated
public final class TextureTransformer {
	public static final ResizeType DEFAULT_RESIZE_TYPE = ResizeType.FORCE;
	public static final float DEFAULT_TEXTURE_SIZE = 192f;
	public static final float DEFAULT_COLOR_DEPTH_FACTOR = 32f;
	public static final Filter DEFAULT_DOWNSCALING_FILTER = Filter.BiLinear;
	public static final TextureFilter DEFAULT_OUTPUT_MIN_FILTER = TextureFilter.Nearest;
	public static final TextureFilter DEFAULT_OUTPUT_MAG_FILTER = TextureFilter.Nearest;
	
	//=======================================================[MULTIPLE USE ACCESS]=====================================================//
	private ResizeType resizeType;
	private float textureSize;
	private float colorDepthFactor;
	private Filter downscalingFilter;
	private TextureFilter outputMinFilter, outputMagFilter;
	
	public TextureTransformer() { this(DEFAULT_RESIZE_TYPE, DEFAULT_TEXTURE_SIZE, DEFAULT_COLOR_DEPTH_FACTOR); }
	public TextureTransformer(float colorDepthFactor) { this(DEFAULT_RESIZE_TYPE, DEFAULT_TEXTURE_SIZE, colorDepthFactor); }
	public TextureTransformer(ResizeType resizeType, float textureSize) { this(resizeType, textureSize, DEFAULT_COLOR_DEPTH_FACTOR); }
	public TextureTransformer(ResizeType resizeType, float textureSize, float colorDepthFactor) { this(resizeType, textureSize, colorDepthFactor, DEFAULT_DOWNSCALING_FILTER); }
	public TextureTransformer(ResizeType resizeType, float textureSize, float colorDepthFactor, Filter filter) { this(resizeType, textureSize, colorDepthFactor, filter, DEFAULT_OUTPUT_MIN_FILTER, DEFAULT_OUTPUT_MAG_FILTER); }
	public TextureTransformer(ResizeType resizeType, float textureSize, float colorDepthFactor, Filter filter, TextureFilter outMinFilter, TextureFilter outMagFilter) {
		this.resizeType = resizeType;
		this.textureSize = textureSize;
		this.colorDepthFactor = colorDepthFactor;
		this.downscalingFilter = filter;
		this.outputMinFilter = outMinFilter;
		this.outputMagFilter = outMagFilter;
	}
	
	/** @param resizeType resize type, check {@link ResizeType} for more information. Default: FORCE. {@link TextureTransformer#DEFAULT_RESIZE_TYPE} **/
	public TextureTransformer setResizeType(ResizeType resizeType) {
		this.resizeType = resizeType;
		return this;
	}
	
	/** @param textureSize factor for texture size modifying. Default: 128. {@link TextureTransformer#DEFAULT_TEXTURE_SIZE} **/
	public TextureTransformer setTextureSize(float textureSize) {
		this.textureSize = textureSize;
		return this;
	}
	
	/** @param colorDepthFactor color depth change factor. Default: 48. {@link TextureTransformer#DEFAULT_COLOR_DEPTH_FACTOR} **/
	public TextureTransformer setColorDepthFactor(float colorDepthFactor) {
		this.colorDepthFactor = colorDepthFactor;
		return this;
	}
	/** @param filter downscaling filter. Default: BiLinear. {@link Filter#BiLinear} **/
	public TextureTransformer setDownscalingFilter(Filter filter) {
		this.downscalingFilter = filter;
		return this;
	}
	
	
	/** Sets output texture filters. Default: Nearest-Nearest. {@link TextureFilter#Nearest} **/
	public TextureTransformer setOutputTextureFilter(TextureFilter minFilter, TextureFilter magFilter) {
		this.outputMinFilter = minFilter;
		this.outputMagFilter = magFilter;
		return this;
	}
	
	/** Disables texture resizing **/
	public TextureTransformer disableImageResize() {
		this.textureSize = -1;
		return this;
	}
	
	/** Disables color depth change of textures **/
	public TextureTransformer disableColorDepthChange() {
		this.colorDepthFactor = -1;
		return this;
	}
	
	/** Texture modify method. Allows you to change the color depth of image, image size.
	 * @param fileHandle input image file handle
	 * @return modified texture object
	 */
	public Texture shrinkTexture(FileHandle fileHandle) { 
		Pixmap pixmap = new Pixmap(fileHandle);
		Texture texture = shrinkTexture(pixmap, this.resizeType, this.textureSize, this.colorDepthFactor, this.downscalingFilter, this.outputMinFilter, this.outputMagFilter);
		pixmap.dispose();
		return texture;
	}
	
	/** Texture modify method. Allows you to change the color depth of image, image size.
	 * @param pixmap input image pixmap
	 * @return modified texture object
	 */
	public Texture shrinkTexture(Pixmap pixmap) { return shrinkTexture(pixmap, this.resizeType, this.textureSize, this.colorDepthFactor, this.downscalingFilter, this.outputMinFilter, this.outputMagFilter); }

	//=========================================================[STATIC ACCESS]=========================================================//
	
	/** Texture modify method. Allows you to change the color depth of image, image size.
	 * @param fileHandle input image file handle
	 * @param resizeType type of imageResize
	 * @param textureSize factor of resizing, use less than one to disable resizing
	 * @param colorDepthFactor factor of color depth change
	 * @param minFilter texture min filter
	 * @param magFilter texture mag filter
	 * @return modified texture object
	 */
	public static Texture shrinkTexture(FileHandle fileHandle, ResizeType resizeType, float textureSize, float colorDepthFactor, Filter filter, TextureFilter minFilter, TextureFilter magFilter) {
		Pixmap pixmap = new Pixmap(fileHandle);
		Texture texture = shrinkTexture(pixmap, resizeType, textureSize, colorDepthFactor, filter, minFilter, magFilter);
		pixmap.dispose();
		return texture;
	}
	
	/** Texture modify method. Allows you to change the color depth of image, image size.
	 * @param pixmap input image pixmap
	 * @param resizeType type of imageResize
	 * @param textureSize factor of resizing, use less than one to disable resizing
	 * @param colorDepthFactor factor of color depth change
	 * @param minFilter texture min filter
	 * @param magFilter texture mag filter
	 * @return modified texture object
	 */
	public static Texture shrinkTexture(Pixmap pixmap, ResizeType resizeType, float textureSize, float colorDepthFactor, Filter filter, TextureFilter minFilter, TextureFilter magFilter) {
		if (pixmap == null || resizeType == null) return null;
		if (colorDepthFactor > 1) downgradeColorDepth(pixmap, colorDepthFactor);
		if (textureSize > 1 && resizeType != null) {
			float factor = Math.max(1, resizeType == ResizeType.ACCORDING_TO_WIDTH ? pixmap.getWidth() / textureSize : resizeType == ResizeType.ACCORDING_TO_HEIGHT ? pixmap.getHeight() / textureSize : textureSize);
			int sizeX = resizeType == ResizeType.FORCE ? (int)textureSize : textureSize > 1 ? (int)Math.max(1, pixmap.getWidth() / factor) : pixmap.getWidth(), sizeY = resizeType == ResizeType.FORCE ? (int)textureSize : textureSize > 0 ? (int)Math.max(1, pixmap.getHeight() / factor) : pixmap.getHeight();
			Pixmap downscaled = new Pixmap(sizeX, sizeY, Format.RGBA8888);
			downscaled.setFilter(filter);
			downscaled.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, downscaled.getWidth(), downscaled.getHeight());
			Texture finalTexture = new Texture(downscaled);
			finalTexture.setFilter(minFilter, magFilter);
			downscaled.dispose();
			return finalTexture;
		}
		return new Texture(pixmap);
	}
	
	/* needs update */
	private static Pixmap downgradeColorDepth(Pixmap pixmap, float colorDepthFactor) {
		for (int y = 0; y != pixmap.getHeight(); y++) {
			for (int x = 0; x != pixmap.getWidth(); x++) {
				int value = pixmap.getPixel(x, y);
				int r = ((value & 0xff000000) >>> 24);
				int g = ((value & 0x00ff0000) >>> 16);
				int b = ((value & 0x0000ff00) >>> 8);
				r -= r % colorDepthFactor;
				g -= g % colorDepthFactor;
				b -= b % colorDepthFactor;
				pixmap.drawPixel(x, y, Color.rgba8888(r / 256f, g / 256f, b / 256f, (value & 0x000000ff) / 256f));
			}
		}
		return pixmap;
	}
	
	/** <br>Used in the texture {@link TextureTransformer#shrinkTexture(Pixmap, ResizeType, float, float)} method for the texture resizing</br>
	 *  - <b>FORCE</b> resizing texture by <b>xy / factor</b> method where 'factor' is given argument<br>
	 *  - <b>ACCORDING_TO_WIDTH</b> resizing texture by <b>factor = x / dimension; xy / factor</b> where 'dimension' is given 'factor' argument<br>
	 *  - <b>ACCORDING_TO_HEIGHT</b> resizing texture by <b>factor = y / dimension; xy / factor</b> where 'dimension' is given 'factor' argument */
	public enum ResizeType {
		FORCE,
		ACCORDING_TO_WIDTH,
		ACCORDING_TO_HEIGHT;
	}
}
