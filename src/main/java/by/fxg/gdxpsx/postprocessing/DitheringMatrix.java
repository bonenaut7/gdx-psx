package by.fxg.gdxpsx.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**  <b>DitheringMatrix</b> class, contains data about martix texture ({@link DitheringMatrix#textureWidth}, {@link DitheringMatrix#textureHeight})
 * 	 and path to it ({@link DitheringMatrix#pathToTexture}.
 * 	 Contains 4 bayer matrices 'from the package'.
 * 
 *   Soft-deprecated, but probably someday will be replaced with something better
 */
public final class DitheringMatrix {
	public static final DitheringMatrix BAYER_8x8 = new DitheringMatrix(8, 8, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer8x8.png"));
	public static final DitheringMatrix BAYER_4x4 = new DitheringMatrix(4, 4, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer4x4.png"));
	public static final DitheringMatrix BAYER_3x3 = new DitheringMatrix(3, 3, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer3x3.png"));
	public static final DitheringMatrix BAYER_2x2 = new DitheringMatrix(2, 2, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer2x2.png"));
	
	protected float textureWidth;
	protected float textureHeight;
	protected FileHandle pathToTexture;
	
	private DitheringMatrix() {}
	private DitheringMatrix(float textureWidth, float textureHeight) { this(textureWidth, textureHeight, null); }
	private DitheringMatrix(float textureWidth, float textureHeight, FileHandle pathToTexture) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.pathToTexture = pathToTexture;
	}
	
	/** Loads texture from classpath and applies required parameters
	 *  @return {@link Texture} object with applied parameters 
	 *  @implNote filter looks unnecessary and i'm not sure about it,
	 *    but texture wrap is required because shaders are not 
	 *    <code>mod</code>'ing UV's when doing dithering so it
	 *    will be a problem without {@link TextureWrap#Repeat} wrap. **/
	public Texture obtainTexture() {
		Texture texture = new Texture(this.pathToTexture);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		return texture;
	}
}
