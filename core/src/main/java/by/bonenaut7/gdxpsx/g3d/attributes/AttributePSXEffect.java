package by.bonenaut7.gdxpsx.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;

public class AttributePSXEffect extends Attribute {
	public static final String vertexSnappingAlias = "psxVertexSnapping";
	public static final long vertexSnapping = register(vertexSnappingAlias);
	//public static final String textureJitterAlias = "psxTextureJitter";
	//public static final long textureJitter = register(textureJitterAlias);
	public static final String textureAffineMappingAlias = "psxTextureAffineMapping";
	public static final long textureAffineMapping = register(textureAffineMappingAlias);
	
	/** Creates Vertex Snapping effect with specified strength
	 *  @param strength - snapping effect strength, lower means stronger!
	 *  @return created attribute **/
	public static AttributePSXEffect createVertexSnapping(float strength) {
		return new AttributePSXEffect(vertexSnapping, Math.max(0.01F, strength));
	}
	
	/** Creates Texture Affine Mapping effect with specified strength
	 *  @param strength - contribution strength within 0.0-1.0 range
	 *  @return created attribute **/
	public static AttributePSXEffect createTextureAffineMapping(float strength) {
		return new AttributePSXEffect(textureAffineMapping, MathUtils.clamp(strength, 0, 1));
	}
	 
	public float strength;
	
	public AttributePSXEffect(long type, float strength) {
		super(type);
		this.strength = strength;
	}
	
	public AttributePSXEffect set(float strength) {
		if (this.type == vertexSnapping) this.strength = Math.min(0.01F, strength);
		else if (this.type == textureAffineMapping) this.strength = MathUtils.clamp(strength, 0, 1);
		else this.strength = strength;
		return this;
	}
	
	public Attribute copy() {
		return new AttributePSXEffect(this.type, this.strength);
	}

	public int compareTo(Attribute attribute) {
		return (int)(this.type - attribute.type);
	}
}
