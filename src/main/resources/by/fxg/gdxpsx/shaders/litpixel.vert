//[Global Variables]=================================================================================
attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
varying vec4 v_position;

#if defined(specularTextureFlag) || defined(specularColorFlag) || defined(fogFlag)
uniform vec4 u_cameraPosition;
#endif //cameraPosition

#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
attribute vec4 a_color;
varying vec4 v_color;
#endif // colorFlag

#ifdef blendedFlag
uniform float u_opacity;
varying float v_opacity;
	#ifdef alphaTestFlag
	uniform float u_alphaTest;
	varying float v_alphaTest;
	#endif //alphaTestFlag
#endif // blendedFlag

//[MATERIAL]=========================================================================================
#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif //textureFlag

#ifdef textureFlag
attribute vec2 a_texCoord0;
#endif //textureFlag

	//[UV]=============================
	#ifdef diffuseTextureFlag
	uniform vec4 u_diffuseUVTransform;
	varying vec2 v_diffuseUV;
	#endif //diffuseTextureFlag
	#ifdef emissiveTextureFlag
	uniform vec4 u_emissiveUVTransform;
	varying vec2 v_emissiveUV;
	#endif //emissiveTextureFlag
	#ifdef specularTextureFlag
	uniform vec4 u_specularUVTransform;
	varying vec2 v_specularUV;
	#endif //specularTextureFlag

//[BONES]============================================================================================
#ifdef boneWeight0Flag
	#define boneWeightsFlag
attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
	#ifndef boneWeightsFlag
	#define boneWeightsFlag
	#endif //boneWeightsFlag
attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
	#if (numBones > 0) 
	#define skinningFlag
	#endif //numBones
#endif //defined numBones

#if defined(numBones)
	#if numBones > 0
	uniform mat4 u_bones[numBones];
	#endif //numBones
#endif //defined numBones

//[LIGHTING]=========================================================================================
#ifdef lightingFlag
	#ifdef shadowMapFlag
	uniform mat4 u_shadowMapProjViewTrans;
	varying vec3 v_shadowMapUv;
	#endif //shadowMapFlag
#endif //lightingFlag

//[ENVIRONMENT]======================================================================================
#ifdef fogFlag
varying float v_fog;
#endif //fogFlag

//[GDX-PSX]==========================================================================================
#ifdef psxVertexJitterFlag
uniform float u_psxVertexJitter;
#endif //psxVertexJitterFlag

#ifdef psxTextureJitterFlag
//uniform float u_psxTextureJitter;
#endif //psxTextureJitterFlag

#ifdef psxTextureAffinenessFlag
uniform float u_psxTextureAffineness;
varying float v_psxTextureAffineness;
#endif //psxTextureAffinenessFlag

vec2 psxModifyUV(vec2 originUV) {
	//#ifdef psxTextureJitterFlag Not implemented
	//originUV = round(originUV * u_psxTextureJitter) / u_psxTextureJitter;
	//#endif //psxTextureJitterFlag
	#ifdef psxTextureAffinenessFlag
	originUV *= v_psxTextureAffineness;
	#endif //psxTextureAffinenessFlag
	return originUV;
}

vec2 manualRound(vec2 value) {
	return vec2(floor(value.x + 0.5), floor(value.y + 0.5));
}

//[SHADER]===========================================================================================
void main() {
	//[TEXTURES]=====================================================================================
	#ifdef diffuseTextureFlag
		v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
	#endif //diffuseTextureFlag
	
	#ifdef emissiveTextureFlag
		v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;
	#endif //emissiveTextureFlag

	#ifdef specularTextureFlag
		v_specularUV = u_specularUVTransform.xy + a_texCoord0 * u_specularUVTransform.zw;
	#endif //specularTextureFlag
	
	#if defined(colorFlag)
		v_color = a_color;
	#endif //colorFlag
	
	//[SKINNING]=====================================================================================
	#ifdef skinningFlag
		mat4 skinning = mat4(0.0);
		#ifdef boneWeight0Flag
			skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
		#endif //boneWeight0Flag
		#ifdef boneWeight1Flag				
			skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
		#endif //boneWeight1Flag
		#ifdef boneWeight2Flag		
			skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
		#endif //boneWeight2Flag
		#ifdef boneWeight3Flag
			skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
		#endif //boneWeight3Flag
		#ifdef boneWeight4Flag
			skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
		#endif //boneWeight4Flag
		#ifdef boneWeight5Flag
			skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
		#endif //boneWeight5Flag
		#ifdef boneWeight6Flag
			skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
		#endif //boneWeight6Flag
		#ifdef boneWeight7Flag
			skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
		#endif //boneWeight7Flag
	#endif //skinningFlag

	#ifdef skinningFlag
		v_position = u_worldTrans * skinning * vec4(a_position, 1.0);
	#else
		v_position = u_worldTrans * vec4(a_position, 1.0);
	#endif //skinningFlag
	gl_Position = u_projViewTrans * v_position;

	//[GDX-PSX]======================================================================================
	#ifdef psxVertexJitterFlag
		gl_Position.xy = manualRound(gl_Position.xy * u_psxVertexJitter) / u_psxVertexJitter;
	#endif //psxVertexJitterFlag
	
	#ifdef psxTextureAffinenessFlag
		v_psxTextureAffineness = 1.0 - ((1.0 - gl_Position.w) * u_psxTextureAffineness);
	#endif //psxTextureAffinenessFlag
	
	#ifdef diffuseTextureFlag
		v_diffuseUV = psxModifyUV(v_diffuseUV);
	#endif //diffuseTextureFlag
	#ifdef emissiveTextureFlag
		v_emissiveUV = psxModifyUV(v_emissiveUV);
	#endif //emissiveTextureFlag
	#ifdef specularTextureFlag
		v_specularUV = psxModifyUV(v_specularUV);
	#endif //specularTextureFlag
	
	//[...]==========================================================================================
	#if defined(normalFlag)
		#if defined(skinningFlag)
			vec3 normal = normalize((u_worldTrans * skinning * vec4(a_normal, 0.0)).xyz);
		#else
			vec3 normal = normalize(u_normalMatrix * a_normal);
		#endif
		v_normal = normal;
	#endif //normalFlag
	
	#ifdef shadowMapFlag
		vec4 spos = u_shadowMapProjViewTrans * pos;
		v_shadowMapUv.xyz = (spos.xyz / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(v_shadowMapUv.z, 0.998);
	#endif //shadowMapFlag
	
	#ifdef blendedFlag
		v_opacity = u_opacity;
		#ifdef alphaTestFlag
			v_alphaTest = u_alphaTest;
		#endif //alphaTestFlag
	#endif //blendedFlag
	
	#ifdef fogFlag
        vec3 fogLength = u_cameraPosition.xyz - v_position.xyz;
        float fog = dot(fogLength, fogLength) * u_cameraPosition.w;
        v_fog = min(fog, 1.0);
    #endif //fogFlag
}
