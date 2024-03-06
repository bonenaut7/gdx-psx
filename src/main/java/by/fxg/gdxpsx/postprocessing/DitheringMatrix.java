package by.fxg.gdxpsx.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * 	<b>DitheringMatrix</b> class, contains data about martix texture ({@link DitheringMatrix#textureWidth}, {@link DitheringMatrix#textureHeight})
 * 	 and path to it ({@link DitheringMatrix#pathToTexture}.
 * 
 * 	Contains 4 bayer matrices 'from the package'.
 */
public class DitheringMatrix {
	public static final DitheringMatrix PSX = new DitheringMatrix(36, 4, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer8x8.png"));
	public static final DitheringMatrix BAYER_8x8 = new DitheringMatrix(8, 8, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer8x8.png"));
	public static final DitheringMatrix BAYER_4x4 = new DitheringMatrix(4, 4, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer4x4.png"));
	public static final DitheringMatrix BAYER_3x3 = new DitheringMatrix(3, 3, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer3x3.png"));
	public static final DitheringMatrix BAYER_2x2 = new DitheringMatrix(2, 2, Gdx.files.classpath("by/fxg/gdxpsx/matrices/bayer2x2.png"));
	
	protected float textureWidth;
	protected float textureHeight;
	protected FileHandle pathToTexture;
	
	public DitheringMatrix() {}
	public DitheringMatrix(float textureWidth, float textureHeight) { this(textureWidth, textureHeight, null); }
	public DitheringMatrix(float textureWidth, float textureHeight, FileHandle pathToTexture) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.pathToTexture = pathToTexture;
	}
	
	public Texture obtainTexture() {
		Texture texture = new Texture(this.pathToTexture);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		return texture;
	}
}
