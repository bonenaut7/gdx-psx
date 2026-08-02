package by.bonenaut7.gdxpsx.vfxintegration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.AbstractVfxEffect;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;

import by.bonenaut7.gdxpsx.postprocessing.PSXPostProcessingShader;
import by.bonenaut7.gdxpsx.postprocessing.PSXPostProcessingShaderStatic;

// At the moment of writing this, gdx-vfx doesn't support 3D(depth buffer)...
public class PSXPostProcessingEffect extends AbstractVfxEffect implements ChainVfxEffect {
	private final PSXPostProcessingShader shader;
	
	public PSXPostProcessingEffect() {
		// Re: LostStarInteractive issue with Linux-based OS, requiring GLSL version header on desktop
		final String prefix = Gdx.app.getType() == ApplicationType.Desktop ? "#version 120" : "";
		
		this.shader = new PSXPostProcessingShaderStatic(
			prefix + Gdx.files.classpath("by/bonenaut7/gdxpsx/shaders/postprocessing.notransform.vert").readString(),
			prefix + Gdx.files.classpath("by/bonenaut7/gdxpsx/shaders/postprocessing.static.frag").readString()
		);
		
		this.rebind();
	}
	
	/** @return object with shader configuration **/
	public PSXPostProcessingShader getConfiguration() {
		return this.shader;
	}
	
	@Override
	public void update(float delta) {
		// Nothing is being updated here :b
	}

	@Override
	public void rebind() {
		this.shader.update();
	}

	@Override
	public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
		buffers.getSrcBuffer().getTexture().bind(0);
		
		final VfxFrameBuffer destFramebuffer = buffers.getDstBuffer();
		final boolean manualBinding = !destFramebuffer.isDrawing();
		
		if (manualBinding) {
			destFramebuffer.begin();
		}
		
		this.shader.getShaderProgram().bind();
		context.getViewportMesh().render(this.shader.getShaderProgram());
		
		if (manualBinding) {
			destFramebuffer.end();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		this.shader.setInputResolution(width, height);
		this.shader.update();
	}
	
	@Override
	public void dispose() {
		this.shader.dispose();
	}
}
