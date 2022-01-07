//[gdx-psx start]
#ifdef GDXPSX_CAMERA_DISTANCE_JITTER
vec4 snapPosition(vec4 position, vec2 res) { vec4 snapped = position; snapped.xyz = position.xyz / position.w; snapped.xy = floor(res * snapped.xy) / res; snapped.xyz *= position.w; return snapped; }
#endif

//[split]//

#ifdef GDXPSX_CAMERA_DISTANCE_JITTER
vec2 gdxpsxResolution = vec2($VEC0);
gl_Position = snapPosition(gl_Position, gdxpsxResolution);
#endif
#ifdef GDXPSX_RESOLUTION_SNAP_JITTER
vec2 gdxpsxResolution = vec2($VEC0);
float gdxpsxCamDist = clamp(gl_Position.w, -1, 1000);
gl_Position.xy = round(gl_Position.xy * (gdxpsxResolution / gdxpsxCamDist)) / (gdxpsxResolution / gdxpsxCamDist);
#endif
//[gdx-psx end]
