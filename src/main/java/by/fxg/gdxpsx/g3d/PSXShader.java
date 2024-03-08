package by.fxg.gdxpsx.g3d;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import by.fxg.gdxpsx.g3d.attributes.AttributePSXEffect;
import by.fxg.gdxpsx.g3d.attributes.LookupTextureAttribute;

public class PSXShader extends DefaultShader {
	public final static Uniform psxVertexJitterUniform = new Uniform("u_psxVertexSnapping", AttributePSXEffect.vertexSnapping);
	public final static Setter psxVertexJitterSetter = new LocalSetter() {
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			AttributePSXEffect psxEffect = combinedAttributes.get(AttributePSXEffect.class, AttributePSXEffect.vertexSnapping);
			shader.set(inputID, psxEffect.strength);
		}
	};
	
	public final static Uniform psxTextureAffinenessUniform = new Uniform("u_psxTextureAffineMapping", AttributePSXEffect.textureAffineMapping);
	public final static Setter psxTextureAffinenessSetter = new LocalSetter() {
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			AttributePSXEffect psxEffect = combinedAttributes.get(AttributePSXEffect.class, AttributePSXEffect.textureAffineMapping);
			shader.set(inputID, psxEffect.strength);
		}	
	};
	
	
	public final static Uniform diffuseLUTUniform = new Uniform("u_diffuseLUT", LookupTextureAttribute.diffuseLUT);
	public final static Setter diffuseLUTSetter = new LocalSetter() {
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			shader.set(inputID, shader.context.textureBinder.bind(combinedAttributes.get(LookupTextureAttribute.class, LookupTextureAttribute.diffuseLUT).getTextureDescriptor()));
		}
	};
	
	public final static Uniform specularLUTUniform = new Uniform("u_specularLUT", LookupTextureAttribute.specularLUT);
	public final static Setter specularLUTSetter = new LocalSetter() {
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			shader.set(inputID, shader.context.textureBinder.bind(combinedAttributes.get(LookupTextureAttribute.class, LookupTextureAttribute.specularLUT).getTextureDescriptor()));
		}
	};
	
	public final static Uniform emissiveLUTUniform = new Uniform("u_emissiveLUT", LookupTextureAttribute.emissiveLUT);
	public final static Setter emissiveLUTSetter = new LocalSetter() {
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			shader.set(inputID, shader.context.textureBinder.bind(combinedAttributes.get(LookupTextureAttribute.class, LookupTextureAttribute.emissiveLUT).getTextureDescriptor()));
		}
	};
	
	public PSXShader(final Renderable renderable) {
		this(renderable, new Config());
	}

	public PSXShader(final Renderable renderable, final Config config) {
		this(renderable, config, createPSXPrefix(renderable, config));
	}

	public PSXShader(final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, config.vertexShader, config.fragmentShader);
	}

	public PSXShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader, final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}
	
	public PSXShader(final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		super(renderable, config, shaderProgram);
		this.register(psxVertexJitterUniform, psxVertexJitterSetter);
		this.register(psxTextureAffinenessUniform, psxTextureAffinenessSetter);	
		this.register(diffuseLUTUniform, diffuseLUTSetter);
		this.register(specularLUTUniform, specularLUTSetter);
		this.register(emissiveLUTUniform, emissiveLUTSetter);
	}
	
	public static String createPSXPrefix(final Renderable renderable, final Config config) {
		final String prefix$ = createPrefix(renderable, config);
		String prefix = prefix$;
		Attributes attributes = combineAttributes(renderable);
		final long attributesMask = attributes.getMask();
		
		if (hasFlag(attributesMask, AttributePSXEffect.vertexSnapping)) prefix += "#define " + AttributePSXEffect.vertexSnappingAlias + "Flag\n";
		if (hasFlag(attributesMask, AttributePSXEffect.textureAffineMapping)) prefix += "#define " + AttributePSXEffect.textureAffineMappingAlias + "Flag\n";
		if (hasFlag(attributesMask, LookupTextureAttribute.diffuseLUT) || hasFlag(attributesMask, LookupTextureAttribute.specularLUT) || hasFlag(attributesMask, LookupTextureAttribute.emissiveLUT)) {
			prefix += "#define " + LookupTextureAttribute.LUTMappingAlias + "Flag\n";
			if (hasFlag(attributesMask, LookupTextureAttribute.diffuseLUT)) prefix += "#define " + LookupTextureAttribute.diffuseLUTAlias + "Flag\n";
			if (hasFlag(attributesMask, LookupTextureAttribute.specularLUT)) prefix += "#define " + LookupTextureAttribute.specularLUTAlias + "Flag\n";
			if (hasFlag(attributesMask, LookupTextureAttribute.emissiveLUT)) prefix += "#define " + LookupTextureAttribute.emissiveLUTAlias + "Flag\n";
		}
		
		return prefix;
	}
	
	protected final static Attributes tmpAttributes = new Attributes();
	protected static final Attributes combineAttributes(final Renderable renderable) {
		tmpAttributes.clear();
		if (renderable.environment != null) tmpAttributes.set(renderable.environment);
		if (renderable.material != null) tmpAttributes.set(renderable.material);
		return tmpAttributes;
	}
	
	protected static boolean hasFlag(long masks, long flag) {
		return (masks & flag) == flag;
	}
}
