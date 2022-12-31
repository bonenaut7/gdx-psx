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

package by.fxg.gdxpsx.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;

public class AttributePSXEffect extends Attribute {
	public static final String vertexJitterAlias = "psxVertexJitter";
	public static final long vertexJitter = register(vertexJitterAlias);
	//public static final String textureJitterAlias = "psxTextureJitter";
	//public static final long textureJitter = register(textureJitterAlias);
	public static final String textureAffinenessAlias = "psxTextureAffineness";
	public static final long textureAffineness = register(textureAffinenessAlias);

	/** Creates Vertex Jitter effect with specified strength
	 *  @param strength - rounding effect strength in 0.0-1.0 range 
	 */
	public static AttributePSXEffect createVertexJitter(float strength) {
		return new AttributePSXEffect(vertexJitter, Math.max(0.01F, strength));
	}
	
	/** Creates Texture Affineness effect with specified strength
	 *  @param strength - contribution strength in 0.0-1.0 range 
	 */
	public static AttributePSXEffect createTextureAffineness(float strength) {
		return new AttributePSXEffect(textureAffineness, MathUtils.clamp(strength, 0, 1));
	}
	
	public float strength;
	public AttributePSXEffect(long type, float strength) {
		super(type);
		this.strength = strength;
	}
	
	public AttributePSXEffect set(float strength) {
		if (this.type == vertexJitter) this.strength = Math.min(0.01F, strength);
		else if (this.type == textureAffineness) this.strength = MathUtils.clamp(strength, 0, 1);
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
