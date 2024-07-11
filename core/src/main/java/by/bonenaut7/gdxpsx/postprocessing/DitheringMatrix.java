package by.bonenaut7.gdxpsx.postprocessing;

import java.util.Locale;

public class DitheringMatrix {
	// Predefined matrices
	public static final DitheringMatrix BAYER_2x2 = new DitheringMatrix(2, 2, generateBayerMatrix(1));
	public static final DitheringMatrix BAYER_4x4 = new DitheringMatrix(4, 4, generateBayerMatrix(2));
	public static final DitheringMatrix BAYER_8x8 = new DitheringMatrix(8, 8, generateBayerMatrix(3));
	public static final DitheringMatrix BAYER_16x16 = new DitheringMatrix(16, 16, generateBayerMatrix(4));
	
	// Values came from dither_lut field, https://github.com/whaison/psxact/blob/develop/src/gpu/gpu_draw.cpp
	// It's actually very close to 4x4 Bayer matrix, but some values are slightly changed
	public static final DitheringMatrix PSXACT_EMULATOR = new DitheringMatrix(4, new float[]{
		-1f, 0f, -0.75f, 0.25f,
		0.5f, -0.5f, 0.75f, -0.25f,
		-0.75f, 0.25f, -1f, 0f,
		0.75f, -0.25f, 0.5f, -0.5f
	});
	
	private final int sizeX;
	private final int sizeY;
	private final float[] matrix;
	
	public DitheringMatrix(int size, float[] array) {
		this(size, size, array);
	}
	
	public DitheringMatrix(int sizeX, int sizeY, float[] array) {
		if (array.length != sizeX * sizeY) {
			throw new IllegalArgumentException("Size doesn't match array length");
		}
		
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.matrix = array;
	}
	
	public int getSizeX() {
		return this.sizeX;
	}
	
	public int getSizeY() {
		return this.sizeY;
	}
	
	/** @return Dithering pattern compacted in 1D array.
	 *    Accessed as <code>array[x % sizeX + y % sizeY * sizeY]</code>,
	 *    left-to-right, top-to-down every next line, i.e. compacted [y][x] 2D array.
	 *    Array should contain values in [-1 to 1] range. **/
	public float[] getMatrix() {
		return this.matrix;
	}
	
	public String generateDefinition() {
		final StringBuilder builder = new StringBuilder();
		builder.append("float[").append(this.matrix.length).append("](");
		for (int idx = 0; idx != this.matrix.length; idx++) {
			builder.append(String.format(Locale.US, "%.4f, ", this.matrix[idx]));
		}
		final int length = builder.length();
		builder.replace(length - 2, length, ") ");
		return builder.toString();
	}
	
	// Takes bayer level, 1 - 2x2, 2 - 4x4, 3 - 8x8 and so on
	// This exists thanks to Kevin Cruijssen, https://codegolf.stackexchange.com/a/259685
	public static float[] generateBayerMatrix(int bayerLevel) {
		final int size = 1 << bayerLevel;
		final float[] array = new float[size * size];
		int sizeSq = size * size, g, i;

		for (/**/; sizeSq-- > 0; array[sizeSq] = g) {
			for (g = i = 0; i < bayerLevel; /**/) {
				g = 4 * g |	2 * (sizeSq % size >> i) + 3 * (sizeSq / size >> i++ & 1) & 3;
			}
		}
		
		// Rearrange array
		// Values originally produced are just indices, we should convert them
		//   to match DitheringMatrix array format (values from [-1 to 1])
		final float half = array.length / 2f;
		for (int idx = 0; idx != array.length; idx++) {
			array[idx] = (array[idx] - half) / half;
		}
		
	    return array; //  Return the resulting matrix after the loops
	}
}
