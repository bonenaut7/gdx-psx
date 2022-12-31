#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGH highp
precision mediump float;
#else
#define LOWP
#define MEDP
#define HIGH
#endif

//[Global Variables]=================================================================================
varying vec4 v_position;

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#else
const vec4 u_cameraPosition = vec4(0.0);
#endif // cameraPositionFlag

#ifdef normalFlag
varying vec3 v_normal;
#endif //normalFlag

#ifdef colorFlag
varying vec4 v_color;
#endif //colorFlag

#ifdef blendedFlag
varying float v_opacity;
	#ifdef alphaTestFlag
	varying float v_alphaTest;
	#endif //alphaTestFlag
#endif //blendedFlag

//[MATERIAL]=========================================================================================
#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

	//[UV]=============================
	#ifdef diffuseTextureFlag
	varying MEDP vec2 v_diffuseUV;
	#endif //diffuseTextureFlag
	#ifdef specularTextureFlag
	varying MEDP vec2 v_specularUV;
	#endif //specularTextureFlag
	#ifdef emissiveTextureFlag
	varying MEDP vec2 v_emissiveUV;
	#endif //emissiveTextureFlag

	//[TEXTURES]=======================
	#ifdef diffuseTextureFlag
	uniform sampler2D u_diffuseTexture;
	#endif //diffuseTextureFlag
	#ifdef specularTextureFlag
	uniform sampler2D u_specularTexture;
	#endif //specularTextureFlag
	#ifdef normalTextureFlag
	uniform sampler2D u_normalTexture;
	#endif //normalTextureFlag
	#ifdef emissiveTextureFlag
	uniform sampler2D u_emissiveTexture;
	#endif //emissiveTextureFlag
	
	//[COLORS]=========================
	#ifdef diffuseColorFlag
	uniform vec4 u_diffuseColor;
	#endif //diffuseColorFlag
	#ifdef specularColorFlag
	uniform vec4 u_specularColor;
	#endif //specularColorFlag
	#ifdef emissiveColorFlag
	uniform vec4 u_emissiveColor;
	#endif //emissiveColorFlag
	
	//[LUT TEXTURES]===================
	#ifdef LUTFlag
		#ifdef diffuseLUTFlag
		uniform sampler2D u_diffuseLUT;
		#endif //diffuseLUTFlag
		#ifdef specularLUTFlag
		uniform sampler2D u_specularLUT;
		#endif //specularLUTFlag
		#ifdef emissiveLUTFlag
		uniform sampler2D u_emissiveLUT;
		#endif	//emissiveLUTFlag
		
		vec4 applyLUT(vec4 color, sampler2D lookup) {
			MEDP float blueColor = color.b * 63.0;
			MEDP vec2 quad1 = vec2(0, floor(floor(blueColor) / 8.0));
			quad1.x = floor(blueColor) - (quad1.y * 8.0);
			MEDP vec2 quad2 = vec2(0, floor(ceil(blueColor) / 8.0));
			quad2.x = ceil(blueColor) - (quad2.y * 8.0); //UVs are HIGHP
			LOWP vec4 lutColor0 = texture2D(lookup, vec2((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.r), (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.g)));
			LOWP vec4 lutColor1 = texture2D(lookup, vec2((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.r), (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.g)));
			return mix(lutColor0, lutColor1, fract(blueColor));
		}
	#endif
//[LIGHTING]=========================================================================================
#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif //shininessFlag

#ifdef lightingFlag
	//[BINDING FLAGS]================================================================================
	#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
	#define ambientFlag
	#endif //ambientFlag
	
	#ifdef shadowMapFlag
	#define separateAmbientFlag
	#endif //shadowMapFlag
	
	#if numDirectionalLights > 0
	struct DirectionalLight {
		vec3 color;
		vec3 direction;
	};
	#endif //numDirectionalLights
	
	//TODO: light attenuation
	#if numPointLights > 0
	struct PointLight {
		vec3 color;
		vec3 position;
	};
	#endif //numPointLights
	
	#if numSpotLights > 0
	struct SpotLight {
		vec3 color;
		vec3 position;
		vec3 direction;
		float cutoffAngle;
		float exponent;
	};
	#endif //numSpotLights
	
	//[BINDING VARIABLES]============================================================================
	#ifdef ambientLightFlag
	uniform vec3 u_ambientLight;
	#endif //ambientLightFlag
	
	#ifdef ambientCubemapFlag
	uniform vec3 u_ambientCubemap[6];
	#endif //ambientCubemapFlag
	
	#ifdef sphericalHarmonicsFlag
	uniform vec3 u_sphericalHarmonics[9];
	#endif //sphericalHarmonicsFlag

	#if numDirectionalLights > 0
	uniform DirectionalLight u_dirLights[numDirectionalLights];
	#endif //numDirectionalLights

	#if numPointLights > 0
	uniform PointLight u_pointLights[numPointLights];
	#endif //numPointLights
	
	#if numSpotLights > 0
	uniform SpotLight u_spotLights[numSpotLights];
	#endif //numSpotLights

	#ifdef shadowMapFlag
	uniform sampler2D u_shadowTexture;
	uniform float u_shadowPCFOffset;
	varying vec3 v_shadowMapUv;
	
	float getShadowness(vec2 offset) {
		const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
		return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
	}

	float getShadow() {
		return (/*getShadowness(vec2(0,0)) +*/ getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) + getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) + getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) + getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
	}
	#endif //shadowMapFlag
#endif //lightingFlag

//v_lightDiffuse(+), v_lightSpecular(+)
vec3 lightDiffuse = vec3(0.0);
vec3 lightSpecular = vec3(0.0);
#if defined(ambientLightFlag)
	vec3 lightAmbient = u_ambientLight;
#elif defined(ambientFlag)
	vec3 lightAmbient = vec3(0.0);
#endif //ambientLightFlag

//[ENVIRONMENT]======================================================================================
#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif //fogFlag

//[GDX-PSX]==========================================================================================
#ifdef psxTextureAffinenessFlag
varying float v_psxTextureAffineness;
#endif

vec2 psxModifyUV(vec2 originalUV) {
	#ifdef psxTextureAffinenessFlag
	return originalUV / v_psxTextureAffineness;
	#else
	return originalUV;
	#endif
}

//[SHADER]===========================================================================================
void main() {
	//[TEXTURES]=====================================================================================
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		#if defined(LUTFlag) && defined(diffuseLUTFlag)
			vec4 diffuse = applyLUT(texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)), u_diffuseLUT) * u_diffuseColor * v_color;
		#else
			vec4 diffuse = texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)) * u_diffuseColor * v_color;
		#endif //LUT
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		#if defined(LUTFlag) && defined(diffuseLUTFlag)
			vec4 diffuse = applyLUT(texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)), u_diffuseLUT) * u_diffuseColor;
		#else
			vec4 diffuse = texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)) * u_diffuseColor;
		#endif //LUT
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		#if defined(LUTFlag) && defined(diffuseLUTFlag)
			vec4 diffuse = applyLUT(texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)), u_diffuseLUT) * v_color;
		#else
			vec4 diffuse = texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)) * v_color;
		#endif //LUT
	#elif defined(diffuseTextureFlag)
		#if defined(LUTFlag) && defined(diffuseLUTFlag)
			vec4 diffuse = applyLUT(texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV)), u_diffuseLUT);
		#else
			vec4 diffuse = texture2D(u_diffuseTexture, psxModifyUV(v_diffuseUV));
		#endif //LUT
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif //DIFFUSE
	
	#if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
		#if defined(LUTFlag) && defined(emissiveLUTFlag)
			vec4 emissive =  applyLUT(texture2D(u_emissiveTexture, psxModifyUV(v_emissiveUV)), u_emissiveLUT) * u_emissiveColor;
		#else
			vec4 emissive = texture2D(u_emissiveTexture, psxModifyUV(v_emissiveUV)) * u_emissiveColor;
		#endif //LUT
	#elif defined(emissiveTextureFlag)
		#if defined(LUTFlag) && defined(emissiveLUTFlag)
			vec4 emissive =  applyLUT(texture2D(u_emissiveTexture, psxModifyUV(v_emissiveUV)), u_emissiveLUT);
		#else
			vec4 emissive = texture2D(u_emissiveTexture, psxModifyUV(v_emissiveUV));
		#endif //LUT
	#elif defined(emissiveColorFlag)
		vec4 emissive = u_emissiveColor;
	#else
		vec4 emissive = vec4(0.0);
	#endif //EMISSIVE
	
	//[LIGHTING]=====================================================================================
	#ifdef lightingFlag
		//[AMBIENT LIGHT CALCULATIONS]===============================================================
		#if defined(ambientCubemapFlag) && defined(normalFlag) 
			vec3 squaredNormal = v_normal * v_normal;
			vec3 isPositive  = step(0.0, v_normal);
			lightAmbient += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
		#endif //ambientCubemapFlag
		
		#if defined(sphericalHarmonicsFlag) && defined(normalFlag)
			lightAmbient += u_sphericalHarmonics[0];
			lightAmbient += u_sphericalHarmonics[1] * v_normal.x;
			lightAmbient += u_sphericalHarmonics[2] * v_normal.y;
			lightAmbient += u_sphericalHarmonics[3] * v_normal.z;
			lightAmbient += u_sphericalHarmonics[4] * (v_normal.x * v_normal.z);
			lightAmbient += u_sphericalHarmonics[5] * (v_normal.z * v_normal.y);
			lightAmbient += u_sphericalHarmonics[6] * (v_normal.y * v_normal.x);
			lightAmbient += u_sphericalHarmonics[7] * (3.0 * v_normal.z * v_normal.z - 1.0);
			lightAmbient += u_sphericalHarmonics[8] * (v_normal.x * v_normal.x - v_normal.y * v_normal.y);
		#endif //sphericalHarmonicsFlag
		
		#if defined(ambientFlag) && !defined(separateAmbientFlag)
			lightDiffuse = lightAmbient;
		#endif //ambientFlag
		
		//[LIGHT OBJECTS CALCULATIONS]===============================================================
		vec3 viewVector = normalize(u_cameraPosition.xyz - v_position.xyz);
		
		#if (numDirectionalLights > 0) && defined(normalFlag)
			for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -u_dirLights[i].direction;
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
				vec3 value = u_dirLights[i].color * NdotL;
				lightDiffuse += value;
				#ifdef specularFlag
					float halfDotView = max(0.0, dot(v_normal, normalize(lightDir + viewVector)));
					lightSpecular += value * pow(halfDotView, u_shininess);
				#endif //specularFlag
			}
		#endif //numDirectionalLights

		#if (numPointLights > 0) && defined(normalFlag)
			for (int i = 0; i < numPointLights; i++) {
				vec3 lightDir = u_pointLights[i].position - v_position.xyz;
				float dist2 = dot(lightDir, lightDir);
				lightDir *= inversesqrt(dist2);
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
				vec3 value = u_pointLights[i].color * (NdotL / (1.0 + dist2));
				lightDiffuse += value;
				#ifdef specularFlag
					float halfDotView = max(0.0, dot(v_normal, normalize(lightDir + viewVector)));
					lightSpecular += value * pow(halfDotView, u_shininess);
				#endif // specularFlag
			}
		#endif //numPointLights
		
		#if (numSpotLights > 0) && defined(normalFlag)
			for (int i = 0; i < numSpotLights; i++) {
				vec3 lightDir = u_spotLights[i].position - v_position.xyz;
				float spotFactor = dot(normalize(lightDir), u_spotLights[i].direction);
				if (spotFactor > u_spotLights[i].cutoffAngle) {
					float dist2 = dot(lightDir, lightDir);
					lightDir *= inversesqrt(dist2);
					float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
					vec3 value = u_spotLights[i].color * (NdotL / (1.0 + dist2));
					lightDiffuse += value * (1.0 - (1.0 - spotFactor) * 1.0 / (1.0 - u_spotLights[i].cutoffAngle));
					#ifdef specularFlag
						float halfDotView = max(0.0, dot(v_normal, normalize(lightDir + viewVector)));
						lightSpecular += (value * pow(halfDotView, u_shininess)) * (1.0 - (1.0 - spotFactor) * 1.0 / (1.0 - u_spotLights[i].cutoffAngle));
					#endif // specularFlag
				}
			}
		#endif //numSpotLights
	#endif //lightingFlag
	
	//[SHADOWMAP & APPLY OF LIGHTING]================================================================
	
	#if (!defined(lightingFlag))
		gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (lightAmbient + getShadow() * lightDiffuse)) + emissive.rgb;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (lightAmbient + lightDiffuse)) + emissive.rgb;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * (diffuse.rgb * lightDiffuse) + emissive.rgb;
			#else
				gl_FragColor.rgb = (diffuse.rgb * lightDiffuse) + emissive.rgb;
			#endif //shadowMapFlag
		#endif
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			#if defined(LUTFlag) && defined(specularLUTFlag)
				vec3 specular = applyLUT(texture2D(u_specularTexture, psxModifyUV(v_specularUV)), u_specularLUT).rgb * u_specularColor.rgb * lightSpecular;
			#else
				vec3 specular = texture2D(u_specularTexture, psxModifyUV(v_specularUV)).rgb * u_specularColor.rgb * lightSpecular;
			#endif //LUT
		#elif defined(specularTextureFlag)
			#if defined(LUTFlag) && defined(specularLUTFlag)
				vec3 specular = applyLUT(texture2D(u_specularTexture, psxModifyUV(v_specularUV)), u_specularLUT).rgb * lightSpecular;
			#else
				vec3 specular = texture2D(u_specularTexture, psxModifyUV(v_specularUV)).rgb * lightSpecular;
			#endif //LUT
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * lightSpecular;
		#else
			vec3 specular = lightSpecular;
		#endif //specularTextureFlag

		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (getShadow() * lightDiffuse + lightAmbient)) + specular + emissive.rgb;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (lightDiffuse + lightAmbient)) + specular + emissive.rgb;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * ((diffuse.rgb * lightDiffuse) + specular) + emissive.rgb;
			#else
				gl_FragColor.rgb = (diffuse.rgb * lightDiffuse) + specular + emissive.rgb;
			#endif //shadowMapFlag
		#endif //ambientFlag
	#endif //lightingFlag

	//[ENVIRONMENT]==================================================================================
	#if defined(fogFlag)
		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
	#endif //fogFlag

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (gl_FragColor.a <= v_alphaTest)
				discard;
		#endif //alphaTestFlag
	#else
		gl_FragColor.a = 1.0;
	#endif //blendedFlag
}
