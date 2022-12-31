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
#ifdef lightingFlag
	varying vec3 v_lightDiffuse;
	
	#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
	#define ambientFlag
	#endif //ambientFlag

	#ifdef specularFlag
	varying vec3 v_lightSpecular;
	#endif //specularFlag

	#ifdef shadowMapFlag
	#define separateAmbientFlag
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

	#if defined(ambientFlag) && defined(separateAmbientFlag)
	varying vec3 v_ambientLight;
	#endif //separateAmbientFlag
#endif //lightingFlag

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
	
	//[SHADOWMAP & APPLY OF LIGHTING]================================================================
	#if (!defined(lightingFlag))
		gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + getShadow() * v_lightDiffuse)) + emissive.rgb;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse)) + emissive.rgb;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse) + emissive.rgb;
			#else
				gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + emissive.rgb;
			#endif //shadowMapFlag
		#endif
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			#if defined(LUTFlag) && defined(specularLUTFlag)
				vec3 specular = applyLUT(texture2D(u_specularTexture, psxModifyUV(v_specularUV)), u_specularLUT).rgb * u_specularColor.rgb * v_lightSpecular;
			#else
				vec3 specular = texture2D(u_specularTexture, psxModifyUV(v_specularUV)).rgb * u_specularColor.rgb * v_lightSpecular;
			#endif //LUT
		#elif defined(specularTextureFlag)
			#if defined(LUTFlag) && defined(specularLUTFlag)
				vec3 specular = applyLUT(texture2D(u_specularTexture, psxModifyUV(v_specularUV)), u_specularLUT).rgb * v_lightSpecular;
			#else
				vec3 specular = texture2D(u_specularTexture, psxModifyUV(v_specularUV)).rgb * v_lightSpecular;
			#endif //LUT
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * v_lightSpecular;
		#else
			vec3 specular = v_lightSpecular;
		#endif

		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
			gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular) + emissive.rgb;
			#else
				gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular + emissive.rgb;
			#endif //shadowMapFlag
		#endif
	#endif //lightingFlag
	
	//[ENVIRONMENT]==================================================================================
	#ifdef fogFlag
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