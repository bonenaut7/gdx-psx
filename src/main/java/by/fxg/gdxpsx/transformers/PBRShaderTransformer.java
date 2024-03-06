package by.fxg.gdxpsx.transformers;

/** 
 *  This <b>Post-processing</b> class using for render enhance. <br>
 *  {@link PBRShaderTransformer} is extension, and needs the <b>gdx-gltf</b> library.
 *  @deprecated Experimental code; Replaced with {@link PSXShader} and {@link PSXShaderProvider}
 */
@Deprecated
public class PBRShaderTransformer extends ShaderTransformer {
//	public PBRShaderTransformer() {
//		super(Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.vs.glsl"), Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.fs.glsl"));
//	}
//	
//	public ShaderProvider createShaderProvider(DefaultShader.Config config) {
//		String vertexShader = this.injectVertexShader();
//		String fragmentShader = this.fragmentShader;
//		if (config != null && config instanceof PBRShaderConfig) {
//			config.vertexShader = vertexShader;
//			config.fragmentShader = fragmentShader;
//			return PBRShaderProvider.createDefault((PBRShaderConfig)config);
//		} else {
//			PBRShaderConfig example = PBRShaderProvider.createDefaultConfig();
//			if (config != null) {
//				example.numDirectionalLights = config.numDirectionalLights;
//				example.numPointLights = config.numPointLights;
//				example.numSpotLights = config.numSpotLights;
//				example.numBones = config.numBones;
//				example.ignoreUnimplemented = config.ignoreUnimplemented;
//				example.defaultCullFace = config.defaultCullFace;
//				example.defaultDepthFunc = config.defaultDepthFunc;
//			}
//			example.vertexShader = vertexShader;
//			example.fragmentShader = fragmentShader;
//			return PBRShaderProvider.createDefault(example);
//		}
//	}
}
