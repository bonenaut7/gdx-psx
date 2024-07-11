// Possible defines generated from code
//#define INPUT_RESOLUTION vec2(x, y)
//#define RESOLUTION_DOWNSCALING vec4(resX, resY, invResX, invResY)
//#define DITHERING
//#define DITHERING_LEGACY - blended dithering, old-fashioned(old gdx-psx)
//#define DITHERING_INTENSITY float(intensity)
//#define DITHERING_INV_SCALE float(invDitheringScale)
//#define DITHERING_TABLE_SIZE_X float(sizeX)
//#define DITHERING_TABLE_SIZE_Y float(sizeY)
//#define DITHERING_TABLE_SIZE int(totalSize)
//#define DITHERING_TABLE float_array[]
//#define COLOR_REDUCTION float(factor)

// Shader
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

#ifdef INPUT_RESOLUTION
const vec2 inputResolution = INPUT_RESOLUTION;
#endif // INPUT_RESOLUTION

#ifdef RESOLUTION_DOWNSCALING
const vec4 resolutionDownscaling = RESOLUTION_DOWNSCALING;
#endif // RESOLUTION_DOWNSCALING

#ifdef DITHERING
float ditheringTable[DITHERING_TABLE_SIZE] = DITHERING_TABLE;

#if defined(DITHERING_LEGACY) && defined(COLOR_REDUCTION)
	vec3 convertRGBtoYUV(vec3 rgb) {
		float grayscale = rgb.r * 0.2126 + 0.7152 * rgb.g + 0.0722 * rgb.b;
		return vec3(grayscale, (rgb.b - grayscale) / 1.8556 + 0.5, (rgb.r - grayscale) / 1.5748 + 0.5);
	}

	vec3 convertYUVtoRGB(vec3 yuv) {
		yuv.gb -= 0.5;
		return vec3(yuv.r + yuv.b * 1.5648, yuv.r + yuv.g * -0.187324 + yuv.b * -0.468124, yuv.r + yuv.g * 1.8556);
	}

	float roundingError(float channel, float minimal, float maximal) {
		return abs(channel - minimal) / abs(minimal - maximal);
	}

	vec3 applyBlendedDithering(vec3 rgb, float ditherValue) {
		vec3 yuv = convertRGBtoYUV(rgb);
		vec3 floored = floor(yuv * COLOR_REDUCTION) / COLOR_REDUCTION;
		vec3 ceiled = ceil(yuv * COLOR_REDUCTION) / COLOR_REDUCTION;

		// flattened dither value, original one is [-1.0 to 1.0], field below is [0.0 to 1.0]
		float normDither = (ditherValue + 1.0) * 0.5;
		yuv.x = mix(floored.x, ceiled.x, step(normDither, roundingError(yuv.x, floored.x, ceiled.x)));
		yuv.y = mix(floored.y, ceiled.y, step(normDither, roundingError(yuv.y, floored.y, ceiled.y)));
		yuv.z = mix(floored.z, ceiled.z, step(normDither, roundingError(yuv.z, floored.z, ceiled.z)));
		return convertYUVtoRGB(yuv);
	}
#endif // DITHERING_LEGACY, COLOR_REDUCTION
#endif // DITHERING

void main() {
	// Resolution Downscaling
	vec2 texCoords;
	#ifdef RESOLUTION_DOWNSCALING
		texCoords = floor(v_texCoords * resolutionDownscaling.xy) * resolutionDownscaling.zw;
	#else
		texCoords = v_texCoords;
	#endif

	// Input color
	HIGH vec4 color = texture2D(u_texture, texCoords);

	// Dithering Matrix
	#if defined(INPUT_RESOLUTION) && defined(DITHERING)
		#ifdef DITHERING_INV_SCALE
			vec2 ditheringResolution = v_texCoords * (inputResolution * DITHERING_INV_SCALE);
		#else
			vec2 ditheringResolution = v_texCoords * inputResolution;
		#endif

		float fpIndex = floor(mod(ditheringResolution.x, DITHERING_TABLE_SIZE_X)) + floor(mod(ditheringResolution.y, DITHERING_TABLE_SIZE_Y)) * DITHERING_TABLE_SIZE_Y;

		#if defined(COLOR_REDUCTION) && defined(DITHERING_LEGACY)
			color.rgb = applyBlendedDithering(color.rgb, ditheringTable[ int(fpIndex) ]);
		#else
			color.rgb += ditheringTable[ int(fpIndex) ] * DITHERING_INTENSITY;
		#endif
	#endif // INPUT_RESOLUTION, DITHERING

	// Color reduction
	#if defined(COLOR_REDUCTION) && !defined(DITHERING_LEGACY)
	color.rgb = floor(color.rgb * COLOR_REDUCTION) / COLOR_REDUCTION;
	#endif // COLOR_REDUCTION

	// Output
	gl_FragColor = color;
	if (gl_FragColor.a < 0.01) discard;
}