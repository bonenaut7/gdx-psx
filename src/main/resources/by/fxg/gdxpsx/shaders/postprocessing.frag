varying vec2 v_texCoords;
varying vec2 v_position;
uniform sampler2D u_texture;

uniform float u_flags[4];
uniform float u_resolution[3];
uniform float u_colorDepth[3];
uniform float u_dithering[3];
uniform sampler2D u_ditherTexture;

//used for color channel degradation lol
float ditherColorChannel(float color, float ditherThreshold, float ditherStep) { 
	float distance = mod(color, ditherStep);
	float baseValue = floor(color / ditherStep) * ditherStep;
	return mix(baseValue, baseValue + ditherStep, step(ditherThreshold, distance / ditherStep - 0.001f));
}

vec3 convertRGBtoYUV(vec3 rgb) {
	vec3 yuv;
	yuv.r = rgb.r * 0.2126 + 0.7152 * rgb.g + 0.0722 * rgb.b;
	yuv.g = (rgb.b - yuv.r) / 1.8556;
	yuv.b = (rgb.r - yuv.r) / 1.5748;
	yuv.gb += 0.5;
	return yuv;
}

vec3 convertYUVtoRGB(vec3 yuv) {
	yuv.gb -= 0.5;
	vec3 rgb;
	rgb.r = yuv.r * 1.0 + yuv.g * 0.0 + yuv.b * 1.5648;
	rgb.g = yuv.r * 1.0 + yuv.g * -0.187324 + yuv.b * -0.468124;
	rgb.b = yuv.r * 1.0 + yuv.g * 1.8556 + yuv.b * 0.0;
	return rgb;
}

float channelError(float color, float colorMin, float colorMax) {
	float range = abs(colorMin - colorMax);
	float aRange = abs(color - colorMin);
	return aRange / range;
}

float ditheredChannel(float error, vec2 ditherBlockUV) {
	float pattern = texture(u_ditherTexture, ditherBlockUV).r;
	if (error > pattern) {
		return 1.0;
	} else {
		return 0.0;
	}
}

vec3 dither(vec3 color, vec3 colorDepth, vec2 resolution, vec2 texCoords) {
	vec3 yuv = convertRGBtoYUV(color);
	vec3 colorMin = floor(yuv * colorDepth) / colorDepth;
	vec3 colorMax = ceil(yuv * colorDepth) / colorDepth;

	float factorX = (u_dithering[1] * u_resolution[0]) * u_dithering[0];
	float factorY = (u_dithering[2] * u_resolution[0]) * u_dithering[0];
	vec2 ditherTextureUV = (gl_FragCoord.xy / resolution.xy) * vec2(resolution.x / factorX, resolution.y / factorY);
	ditherTextureUV.x = mod(ditherTextureUV.x, 1.0);
	ditherTextureUV.y = mod(ditherTextureUV.y, 1.0);

	yuv.x = mix(colorMin.x, colorMax.x, ditheredChannel(channelError(yuv.x, colorMin.x, colorMax.x), ditherTextureUV));
	yuv.y = mix(colorMin.y, colorMax.y, ditheredChannel(channelError(yuv.y, colorMin.y, colorMax.y), ditherTextureUV));
	yuv.z = mix(colorMin.z, colorMax.z, ditheredChannel(channelError(yuv.z, colorMin.z, colorMax.z), ditherTextureUV));

	return convertYUVtoRGB(yuv);
}

void main() {
	//downscaling
	vec2 texCoords = v_texCoords;
	vec2 resolution = vec2(u_resolution[0] * (1.0f / u_resolution[1]), u_resolution[0] * (1.0f / u_resolution[2]));
	if (u_flags[1] > 0) {
		texCoords = vec2(resolution.x * floor(v_texCoords.x / resolution.x), resolution.y * floor(v_texCoords.y / resolution.y));
	}

	//color-depth
	vec4 fragmentColor = texture(u_texture, texCoords);
	vec3 colorDepth = vec3(256.0);
	if (u_flags[2] > 0) {
		colorDepth = vec3(u_colorDepth[0], u_colorDepth[1], u_colorDepth[2]);
		vec3 normColorDepth = 1.0f / max(colorDepth, 1.0f);
		fragmentColor.r = ditherColorChannel(fragmentColor.r, 0.5f, normColorDepth.r);
		fragmentColor.g = ditherColorChannel(fragmentColor.g, 0.5f, normColorDepth.g);
		fragmentColor.b = ditherColorChannel(fragmentColor.b, 0.5f, normColorDepth.b);
	}

	//dithering
	if (u_flags[3] > 0) {
		fragmentColor.rgb = dither(fragmentColor.rgb, colorDepth, resolution, texCoords);
	}
	gl_FragColor = fragmentColor;
	if (u_flags[0] > 0 && gl_FragColor.a < 0.5) { discard; }
}
