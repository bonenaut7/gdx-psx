package by.fxg.gdxpsx.vfxintegration;

import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.AbstractVfxEffect;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;

import by.bonenaut7.gdxpsx.postprocessing.PSXPostProcessingShader;

// At the moment of writing this, gdx-vfx doesn't support 3D(depth buffer)...
public class PSXPostProcessingEffect extends AbstractVfxEffect implements ChainVfxEffect {
	private final PSXPostProcessingShader shader;
	
	public PSXPostProcessingEffect(PSXPostProcessingShader shader) {
		this.shader = shader;
		this.rebind();
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
