package by.fxg.gdxpsx.postprocessing;

public enum ResolutionDownscalingType {
	/** None of methods below will be used **/
	NONE,
	
	/** Factor type is about modifying current specified resolution 
	 * 	by a specified factor. For example:
	 * 	<code>output = resolution / factor</code>
	 * 	E.g. if the factor will be 2.0 then output will be twice smaller **/
	FACTOR,
	
	/** Fit-to-resolution requires to specify output resolution 
	 *  so program will find the factor itself **/
	FIT_TO_RESOLUTION;
}
