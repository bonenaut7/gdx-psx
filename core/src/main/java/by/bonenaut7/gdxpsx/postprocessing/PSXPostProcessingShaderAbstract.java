package by.bonenaut7.gdxpsx.postprocessing;

import com.badlogic.gdx.math.Vector2;
import by.bonenaut7.gdxpsx.GdxPsxRuntimeException;

public abstract class PSXPostProcessingShaderAbstract implements PSXPostProcessingShader {
	protected boolean isDownscalingEnabled = false;
	protected ResolutionDownscalingType downscalingType = ResolutionDownscalingType.SCALE;
	protected float downscalingScale = 1.0f;
	protected Vector2 downscalingTargetResolution = new Vector2();
	
	protected boolean isDitheringEnabled = false;
	protected float ditheringIntensity = 0.1f; // post-multiplied by 0.01
	protected float ditheringScale = 1.0f;
	protected DitheringMatrix ditheringMatrix = DitheringMatrix.BAYER_8x8;
	protected boolean isLegacyDitheringEnabled = false;
	
	protected boolean isColorReductionEnabled = false;
	protected float colorReductionFactor = 255f;
	
	protected Vector2 inputResolution = new Vector2(1, 1);
	
	// Resolution downscaling
	
	@Override
	public boolean isDownscalingEnabled() {
		return this.isDownscalingEnabled;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDownscalingEnabled(boolean enabled) {
		this.isDownscalingEnabled = enabled;
		return this;
	}

	@Override
	public float getDownscalingScale() {
		return this.downscalingScale;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDownscalingFromScale(float scale) {
		if (scale < 1) {
			throw new GdxPsxRuntimeException("scale can't be less than 1");
		}
		
		this.downscalingType = ResolutionDownscalingType.SCALE;
		this.downscalingScale = scale;
		return this;
	}

	@Override
	public int getDownscalingTargetWidth() {
		return (int)this.downscalingTargetResolution.x;
	}
	
	@Override
	public int getDownscalingTargetHeight() {
		return (int)this.downscalingTargetResolution.y;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDownscalingToResolution(int width, int height) {
		if (width < 1 || height < 1) {
			throw new GdxPsxRuntimeException("width and height can't be less than 1");
		}
		
		this.downscalingType = ResolutionDownscalingType.FIT_TO_RESOLUTION;
		this.downscalingTargetResolution.set(width, height);
		return this;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDownscalingToResolution(Vector2 targetResolution) {
		if (targetResolution.x < 1 || targetResolution.y < 1) {
			throw new GdxPsxRuntimeException("width and height can't be less than 1");
		}
		
		this.downscalingType = ResolutionDownscalingType.FIT_TO_RESOLUTION;
		this.downscalingTargetResolution.set(targetResolution);
		return this;
	}
	
	// Dithering

	@Override
	public boolean isDitheringEnabled() {
		return this.isDitheringEnabled;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDitheringEnabled(boolean enabled) {
		this.isDitheringEnabled = enabled;
		return this;
	}

	@Override
	public float getDitheringIntensity() {
		return this.ditheringIntensity;
	}

	@Override
	public PSXPostProcessingShaderAbstract setDitheringIntensity(float intensity) {
		this.ditheringIntensity = intensity;
		return this;
	}
	
	@Override
	public float getDitheringScale() {
		return this.ditheringScale;
	}
	
	@Override
	public PSXPostProcessingShaderAbstract setDitheringScale(float scale) {
		if (scale < 1) {
			throw new GdxPsxRuntimeException("scale can't be less than 1");
		}
		
		this.ditheringScale = scale;
		return this;
	}

	@Override
	public DitheringMatrix getDitheringMatrix() {
		return this.ditheringMatrix;
	}

	@Override
	public PSXPostProcessingShader setDitheringMatrix(DitheringMatrix ditheringMatrix) {
		if (ditheringMatrix == null) {
			throw new GdxPsxRuntimeException("ditheringMatrix can't be null");
		}
		
		this.ditheringMatrix = ditheringMatrix;
		return this;
	}
	
	@Override
	public boolean isLegacyDitheringEnabled() {
		return this.isLegacyDitheringEnabled;
	}

	@Override
	public PSXPostProcessingShader setLegacyDitheringEnabled(boolean enabled) {
		this.isLegacyDitheringEnabled = enabled;
		return this;
	}

	// Color reduction
	
	@Override
	public boolean isColorReductionEnabled() {
		return this.isColorReductionEnabled;
	}

	@Override
	public PSXPostProcessingShaderAbstract setColorReductionEnabled(boolean enabled) {
		this.isColorReductionEnabled = enabled;
		return this;
	}

	@Override
	public float getColorReductionFactor() {
		return this.colorReductionFactor;
	}

	@Override
	public PSXPostProcessingShaderAbstract setColorReduction(float colorReduction) {
		if (colorReduction < 1) {
			throw new GdxPsxRuntimeException("colorReduction can't be less than 1");
		}
		
		this.colorReductionFactor = colorReduction;
		return this;
	}
	
	// Internals
	
	@Override
	public PSXPostProcessingShaderAbstract setInputResolution(int width, int height) {
		if (width < 1 || height < 1) {
			throw new GdxPsxRuntimeException("width and height can't be less than 1");
		}
		
		this.inputResolution.set(width, height);
		return this;
	}
	
	@Override
	public PSXPostProcessingShaderAbstract setInputResolution(Vector2 inputResolution) {
		if (inputResolution.x < 1 || inputResolution.y < 1) {
			throw new GdxPsxRuntimeException("width and height can't be less than 1");
		}
		
		this.inputResolution.set(inputResolution);
		return this;
	}
}
