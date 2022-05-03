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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * 	<b>DitheringMatrix</b> class, contains data about martix texture ({@link DitheringMatrix#textureWidth}, {@link DitheringMatrix#textureHeight})
 * 	 and path to it ({@link DitheringMatrix#pathToTexture}.
 * 
 * 	Contains 4 bayer matrices 'from the package'.
 * 
 * 	@author fxgaming (FXG)
 */
public class DitheringMatrix {
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
