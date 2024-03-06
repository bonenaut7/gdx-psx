package by.fxg.gdxpsx.g3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

/** Lookup table texture attribute
 *  @see Wikipedia: <a href="https://en.wikipedia.org/wiki/Lookup_table">Lookup Tables</a> **/
public class LookupTextureAttribute extends Attribute {
	public static final String diffuseLUTAlias = "diffuseLUT";
	public static final long diffuseLUT = register(diffuseLUTAlias);
	public static final String specularLUTAlias = "specularLUT";
	public static final long specularLUT = register(specularLUTAlias);
	public static final String emissiveLUTAlias = "emissiveLUT";
	public static final long emissiveLUT = register(emissiveLUTAlias);
	
	public static LookupTextureAttribute createDiffuseLUT(Texture texture) { return new LookupTextureAttribute(diffuseLUT, texture); }
	public static LookupTextureAttribute createSpecularLUT(Texture texture) { return new LookupTextureAttribute(specularLUT, texture); }
	public static LookupTextureAttribute createEmissiveLUT(Texture texture) { return new LookupTextureAttribute(emissiveLUT, texture); }
	
	protected final TextureDescriptor<Texture> textureDescriptor;
	protected LookupTextureAttribute(long type, Texture texture) {
		super(type);
		this.textureDescriptor = new TextureDescriptor<>();
		this.set(texture);
	}
	
	public LookupTextureAttribute set(Texture texture) {
		this.textureDescriptor.set(texture, texture.getMinFilter(), texture.getMagFilter(), texture.getUWrap(), texture.getVWrap());
		return this;
	}

	public TextureDescriptor<Texture> getTextureDescriptor() {
		return this.textureDescriptor;
	}
	
	public Attribute copy() {
		return new LookupTextureAttribute(this.type, this.textureDescriptor.texture);
	}

	public int compareTo(Attribute attribute) {
		return (int)(this.type - attribute.type);
	}
}