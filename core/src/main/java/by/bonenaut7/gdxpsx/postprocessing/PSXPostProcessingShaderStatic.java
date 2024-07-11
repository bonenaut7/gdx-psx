package by.bonenaut7.gdxpsx.postprocessing;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

public final class PSXPostProcessingShaderStatic extends PSXPostProcessingShaderAbstract {
	private ShaderProgram shaderProgram;
	private final String vertexShader;
	private final String fragmentShader;
	
	public PSXPostProcessingShaderStatic() {
		this.vertexShader = Gdx.files.classpath("by/bonenaut7/gdxpsx/shaders/postprocessing.vert").readString();
		this.fragmentShader = Gdx.files.classpath("by/bonenaut7/gdxpsx/shaders/postprocessing.static.frag").readString();
		this.update();
	}
	
	public PSXPostProcessingShaderStatic(String vertexShader, String fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.update();
	}
	
	@Override
	public boolean update() {
		if (this.shaderProgram != null) {
			this.shaderProgram.dispose();
			this.shaderProgram = null;
		}
		
		final String definitions = this.createDefinitions();
		this.shaderProgram = new ShaderProgram(definitions + this.vertexShader, definitions + this.fragmentShader);
		if (!this.shaderProgram.isCompiled()) {
			Gdx.app.error("GDX-PSX", "Unable to compile shader, shader log:\n" + this.shaderProgram.getLog());
			Gdx.app.error("GDX-PSX", "\n" + definitions + this.fragmentShader);
			return false;
		}
		
		return true;
	}

	@Override
	public ShaderProgram getShaderProgram() {
		return this.shaderProgram;
	}

	@Override
	public void dispose() {
		this.shaderProgram.dispose();
	}
	
	private String createDefinitions() {
		final StringBuilder builder = new StringBuilder();
		// In-built input resolution
		builder.append(String.format(Locale.US, "#define INPUT_RESOLUTION vec2(%.4f, %.4f)\n", this.inputResolution.x, this.inputResolution.y));
		
		// Resolution downscaling
		if (this.isDownscalingEnabled) {
			float resX = this.inputResolution.x, resY = this.inputResolution.y;
			
			switch (this.downscalingType) {
				case SCALE: {
					resX /= this.downscalingScale;
					resY /= this.downscalingScale;
				} break;
				case FIT_TO_RESOLUTION: {
					resX = MathUtils.clamp(this.inputResolution.x / this.downscalingTargetResolution.x, 1, this.inputResolution.x);
					resY = MathUtils.clamp(this.inputResolution.y / this.downscalingTargetResolution.y, 1, this.inputResolution.y);
				} break;
			}

			builder.append(String.format(Locale.US, "#define RESOLUTION_DOWNSCALING vec4(%.4f, %.4f, %.8f, %.8f)\n", resX, resY, 1f / resX, 1f / resY));
		}
		
		// Dithering
		if (this.isDitheringEnabled) {
			builder.append("#define DITHERING\n");
			
			if (this.isLegacyDitheringEnabled) {
				builder.append("#define DITHERING_LEGACY\n");
			}
			
			builder.append(String.format(Locale.US, "#define DITHERING_INTENSITY %.8f\n", this.ditheringIntensity * 0.01f));
			builder.append(String.format(Locale.US, "#define DITHERING_INV_SCALE %.8f\n", 1f / this.ditheringScale));
			builder.append(String.format(Locale.US, "#define DITHERING_TABLE_SIZE_X %.2f\n", (float)this.ditheringMatrix.getSizeX()));
			builder.append(String.format(Locale.US, "#define DITHERING_TABLE_SIZE_Y %.2f\n", (float)this.ditheringMatrix.getSizeY()));
			builder.append(String.format(Locale.US, "#define DITHERING_TABLE_SIZE %d\n", this.ditheringMatrix.getSizeX() * this.ditheringMatrix.getSizeY()));
			builder.append(String.format(Locale.US, "#define DITHERING_TABLE %s\n", this.createDitheringTableDefinition()));
		}
		
		// Color reduction
		if (this.isColorReductionEnabled) {
			builder.append(String.format(Locale.US, "#define COLOR_REDUCTION %.2f\n", this.colorReductionFactor));
		}
		
		return builder.toString();
	}
	
	// Generating dithering table definition is scary(because mostly it's shitcode for my shitty decisions)
	private String createDitheringTableDefinition() {
		final float[] matrix = this.ditheringMatrix.getMatrix();
		final StringBuilder builder = new StringBuilder();
		builder.append("float[").append(matrix.length).append("](");
		
		for (int idx = 0; idx != matrix.length; idx++) {
			builder.append(String.format(Locale.US, "%.4f,", matrix[idx]));
		}
		
		// Removing [,] and replacing it with [)] (without semicolon, because it should be present in shader)
		final int length = builder.length();
		builder.replace(length - 1, length, ")");
		
		return builder.toString();
	}
}
