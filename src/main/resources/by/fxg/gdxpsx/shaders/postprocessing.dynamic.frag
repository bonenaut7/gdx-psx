#ifdef GL_ES
	#define LOWP lowp
	#define MED mediump
	#define HIGH highp
	precision mediump float;
#else
	#define MED
	#define LOWP
	#define HIGH
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

// Resolution Downscaling
uniform vec4 u_resDownscaling;

// Dithering Matrix
uniform sampler2D u_ditherMatrixTexture;
uniform vec3 u_ditheringData;

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

float roundError(float channel, float cmin, float cmax) {
	float minMaxRange = abs(cmin - cmax);
	float channelMinRange = abs(channel - cmin);
	return channelMinRange / minMaxRange;
}

vec3 applyDithering(vec3 color) {
	vec3 yuv = convertRGBtoYUV(color);
	vec3 cmin = floor(yuv * u_ditheringData.z) / u_ditheringData.z;
	vec3 cmax = ceil(yuv * u_ditheringData.z) / u_ditheringData.z;

	vec2 ditherMatrixUV = (gl_FragCoord.xy / u_resDownscaling.xy) * (u_resDownscaling.xy / u_ditheringData.xy);
	float dither = texture2D(u_ditherMatrixTexture, ditherMatrixUV).r;
	yuv.x = mix(cmin.x, cmax.x, step(dither, roundError(yuv.x, cmin.x, cmax.x)));
	yuv.y = mix(cmin.y, cmax.y, step(dither, roundError(yuv.y, cmin.y, cmax.y)));
	yuv.z = mix(cmin.z, cmax.z, step(dither, roundError(yuv.z, cmin.z, cmax.z)));

	return convertYUVtoRGB(yuv);
}

void main() {
	// Resolution Downscaling
	vec2 texCoords = floor(v_texCoords * u_resDownscaling.xy) * u_resDownscaling.zw;

	// Color
	vec4 color = texture2D(u_texture, texCoords);

	// Dithering Matrix
	if (u_ditheringData.x > 0.5) {
		 color.rgb = applyDithering(color.rgb);
	}

	// Output
	gl_FragColor = color;
	if (gl_FragColor.a < 0.01) discard;
}