const float dither2x2[4] = {0, 3, 2, 1};
const float dither4x4[16] = {0,  8,  2,  10, 12, 4,  14, 6, 3,  11, 1,  9, 15, 7, 13, 5};
const float ditherScreenDoor[16] = {1, 9, 3, 11, 13, 5, 15, 7, 4, 12, 2, 10, 16, 8, 14, 6};
const float ditherIndex8x8[64] = {0, 32, 8, 40, 2, 34, 10, 42, 48, 16, 56, 24, 50, 18, 58, 26, 12, 44, 4, 36, 14, 46, 6, 38, 60, 28, 52, 20, 62, 30, 54, 22, 3, 35, 11, 43, 1, 33, 9, 41, 51, 19, 59, 27, 49, 17, 57, 25, 15, 47, 7, 39, 13, 45, 5, 37, 63, 31, 55, 23, 61, 29, 53, 21};

varying vec2 v_texCoords;
varying vec2 v_position;
uniform sampler2D u_texture;

uniform float u_flags[4];
uniform float u_resolution[3];
uniform float u_colorDepth[3];
uniform int u_ditheringMatrix;
uniform float u_ditherDepth;

float ditherColorChannel(float color, float ditherThreshold, float ditherStep) { 
	float distance = mod(color, ditherStep);
	float baseValue = floor(color / ditherStep) * ditherStep;
	return mix(baseValue, baseValue + ditherStep, step(ditherThreshold, distance / ditherStep - 0.001f));
}

void main() {
	vec2 iTexCoords = v_texCoords;
	if (u_flags[1] > 0) {
		float dx = u_resolution[0] * (1.0f / u_resolution[1]);
		float dy = u_resolution[0] * (1.0f / u_resolution[2]);
		iTexCoords = vec2(dx * floor(v_texCoords.x / dx), dy * floor(v_texCoords.y / dy));
	}
	vec4 iFragColor = texture(u_texture, iTexCoords);
	if (u_flags[2] > 0) {
		vec3 colorStep = 1.0f / max(vec3(u_colorDepth[0], u_colorDepth[1], u_colorDepth[2]), 1.0f);
		iFragColor.r = ditherColorChannel(iFragColor.r, 0.5f, colorStep.r);
		iFragColor.g = ditherColorChannel(iFragColor.g, 0.5f, colorStep.g);
		iFragColor.b = ditherColorChannel(iFragColor.b, 0.5f, colorStep.b);
	}
	if (u_flags[3] > 0) {
		float ditherThreshold = 0;
		if (u_ditheringMatrix == 0) { ditherThreshold = dither2x2[int(mod(v_position.x, 2) + mod(v_position.y, 2) * 2)] * 0.25F; }
		else if (u_ditheringMatrix == 1) { ditherThreshold = dither4x4[int(mod(v_position.x, 4) + mod(v_position.y, 4) * 4)] * 0.0625F; }
		else if (u_ditheringMatrix == 2) { ditherThreshold = ditherScreenDoor[int(mod(v_position.x, 4) + mod(v_position.y, 4) * 4)] * 0.0625F; }
		else if (u_ditheringMatrix == 3) { ditherThreshold = ditherIndex8x8[int(mod(v_position.x, 8) + mod(v_position.y, 8) * 8)] * 0.03125F; }
		vec3 ditherStep = 1.0f / max(vec3(u_ditherDepth, u_ditherDepth, u_ditherDepth), 1.0f);
		iFragColor.r = ditherColorChannel(iFragColor.r, ditherThreshold, ditherStep.r);
		iFragColor.g = ditherColorChannel(iFragColor.g, ditherThreshold, ditherStep.g);
		iFragColor.b = ditherColorChannel(iFragColor.b, ditherThreshold, ditherStep.b);
	}
	gl_FragColor = iFragColor;
	if (u_flags[0] > 0 && gl_FragColor.a < 0.5) { discard; }
}
