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

attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;

void main() {
	v_texCoords = a_texCoord0;
	gl_Position = a_position;
}
