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

package by.fxg.gdxpsx.transformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector2;

/** 
 *  This <b>Post-processing</b> class using for render enhance.
 *  
 *  <b>WARNING! This have a very critical problem. Resolution change cannot be done without recompiling shader because it will cause problems with downscaling!</b>
 *  
 *  @author fxgaming (FXG)
 */
public class ShaderTransformer {
	public static final float DEFAULT_DOWNSCALE_FACTOR = 3.0f;
	
	protected final Pattern regexPattern = Pattern.compile(".*void main\\(\\) \\{.*", Pattern.MULTILINE);
	protected String vertexShader;
	protected String fragmentShader;
	
	protected TransformType transformType;
	protected Vector2 resolution;
	protected float downscaleFactor;
	
	/** Creating transformer with libgdx {@link DefaultShader} and default transformer settings */
	public ShaderTransformer() {
		this(DefaultShader.getDefaultVertexShader(), DefaultShader.getDefaultFragmentShader());
	}
	
	/** Creating transformer with given parameters
	 *  @param vertexShader - vertex shader for transform
	 *  @param fragmentShader - fragment shader for transform
	 */
	public ShaderTransformer(FileHandle vertexShader, FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}
	
	/** Creating transformer - with given parameters
	 *  @param vertexShader - vertex shader for transform
	 *  @param fragmentShader - fragment shader for transform
	 */
	public ShaderTransformer(String vertexShader, String fragmentShader) {
		this(DEFAULT_DOWNSCALE_FACTOR, vertexShader, fragmentShader);
	}
	
	/** Creating transformer with given parameters
	 *  @param downscaleFactor - jitter factor
	 *  @param vertexShader - vertex shader for transform
	 *  @param fragmentShader - fragment shader for transform
	 */
	public ShaderTransformer(float downscaleFactor, String vertexShader, String fragmentShader) {
		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), downscaleFactor, vertexShader, fragmentShader);
	}
	
	/** Creating transformer with given parameters
	 *  @param resolutionWidth - width of viewport
	 *  @param resolutionHeight - height of viewport
	 *  @param downscaleFactor - jitter factor
	 *  @param vertexShader - vertex shader for transform
	 *  @param fragmentShader - fragment shader for transform
	 */
	public ShaderTransformer(float resolutionWidth, float resolutionHeight, float downscaleFactor, String vertexShader, String fragmentShader) { 
		this(TransformType.RESOLUTION_SNAP_JITTER, resolutionWidth, resolutionHeight, downscaleFactor, vertexShader, fragmentShader);
	}
	
	/** Creating transformer with given parameters
	 *	@param transformType - vertex jitter type
	 *  @param resolutionWidth - width of viewport
	 *  @param resolutionHeight - height of viewport
	 *  @param downscaleFactor - jitter factor
	 *  @param vertexShader - vertex shader for transform
	 *  @param fragmentShader - fragment shader for transform
	 */
	public ShaderTransformer(TransformType transformType, float resolutionWidth, float resolutionHeight, float downscaleFactor, String vertexShader, String fragmentShader) {
		this.transformType = transformType;
		this.resolution = new Vector2(resolutionWidth, resolutionHeight);
		this.downscaleFactor = downscaleFactor;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}
	
	/** @param vertex - Vertex Shader. 
	 *  @param fragment - Fragment Shader.
	 **/
	public ShaderTransformer setShaders(String vertex, String fragment) {
		this.vertexShader = vertex;
		this.fragmentShader = fragment;
		return this;
	}
	
	/** @param transformType - jitter type. Default: RESOLUTION_SNAP_JITTER **/
	public ShaderTransformer setTransformType(TransformType transformType) {
		this.transformType = transformType;
		return this;
	}
	
	/** @param resolutionWidth - viewport width. Default: Libgdx window width 
	 *  @param resolutionHeight - viewport height. Default: Libgdx window height
	 */
	public ShaderTransformer setResolution(float resolutionWidth, float resolutionHeight) {
		this.resolution.set(resolutionWidth, resolutionHeight);
		return this;
	}
	
	/** @param downscaleFactor - factor of vertex jitter. Default: 2.0 **/
	public ShaderTransformer setFactor(float downscaleFactor) {
		this.downscaleFactor = Math.max(1f, downscaleFactor);
		return this;
	}
	
	/** @return jitter type **/
	public TransformType getTransformType() { return this.transformType; }
	
	/** @return original resolution width **/
	public float getWidth() { return this.resolution.x; }
	
	/** @return original resolution height **/
	public float getHeight() { return this.resolution.y; }
	
	/** @return factor of vertex jitter **/
	public float getFactor() { return this.downscaleFactor; }
	
	/** @return <b>ShaderProvider</b> with modified shader. **/
	public ShaderProvider createShaderProvider() { return this.createShaderProvider(null); }
	
	/** @param - config ShaderProvider configuration
	 *  @return <b>ShaderProvider</b> with modified shader.
	 */
	public ShaderProvider createShaderProvider(DefaultShader.Config config) {
		String vertexShader = this.injectVertexShader();
		String fragmentShader = this.fragmentShader;
		if (config != null) {
			config.vertexShader = vertexShader;
			config.fragmentShader = fragmentShader;
		}
		return config == null ? new DefaultShaderProvider(vertexShader, fragmentShader) : new DefaultShaderProvider(config);
	}
	
	/** @return <b>Modified Vertex Shader</b> code for further compiling. **/
	protected String injectVertexShader() {
		Matcher matcher = this.regexPattern.matcher(this.vertexShader);
		if (matcher.find()) {
			String[] shaderMod = Gdx.files.classpath("by/fxg/gdxpsx/shaders/psx.3d.vert").readString().split(Pattern.quote("//[split]//"));
			switch (this.transformType) {
				default: {
					String $VEC0 = String.format("%.2f! %.2f", Math.max(1, this.resolution.x / this.downscaleFactor), Math.max(1, this.resolution.y / this.downscaleFactor));
					shaderMod[1] = shaderMod[1].replaceAll(Pattern.quote("$VEC0"), $VEC0.replaceAll(Pattern.quote(","), ".").replaceAll("!", ","));
				} break;
			}
			String[] vertexShaderParts = this.vertexShader.split(Pattern.quote(matcher.group()));
			StringBuilder stringBuilder = new StringBuilder("#define GDXPSX_").append(this.transformType.name());
			stringBuilder.append(shaderMod[0]).append(vertexShaderParts[0]).append("void main() {");
			vertexShaderParts = vertexShaderParts[1].split(Pattern.quote("}"));
			for (int i = 0; i != vertexShaderParts.length - 1; i++) stringBuilder.append(vertexShaderParts[i]).append(i < vertexShaderParts.length - 2 ? "}" : "");
			stringBuilder.append(shaderMod[1]).append("}").append(vertexShaderParts[vertexShaderParts.length - 1]);
			return stringBuilder.toString();
		} else System.out.println("[GDX-PSX] Unnable to patch shaders because they are not matching pattern.");
		return this.vertexShader;
		//return Gdx.files.classpath("by/fxg/gdxpsx/shaders/psx.aff.vert").readString();
	}
	
	protected String injectFragmentShader() {
		return Gdx.files.classpath("by/fxg/gdxpsx/shaders/psx.aff.frag").readString();
	}

	public enum TransformType {
		CAMERA_DISTANCE_JITTER,
		RESOLUTION_SNAP_JITTER;
	}
}
