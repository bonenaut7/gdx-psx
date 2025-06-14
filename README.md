[![](https://jitpack.io/v/fxgaming/gdx-psx.svg)](https://jitpack.io/#fxgaming/gdx-psx)
# gdx-psx
![](https://raw.githubusercontent.com/bonenaut7/gdx-psx/refs/heads/main/.github/gdxpsx.gif)

**gdx-psx** its a library for [**LibGDX**](https://github.com/libgdx/libgdx) designed to assist you in simulation of PlayStation 1
graphics with few simple steps!
If you have questions or suggestions for this project, or you want to just chat about it - welcome to our [discord](https://discord.gg/2FqQQxyFS8).

## 3D Features:
- Vertex Snapping (Vertex Jitter)
- Texture Affine Mapping
- LUT-Mapping (Diffuse, Specular, Emissive)

## 2D Post-processing Features:
- Screen dithering with built-in Bayer matrices (2x2, 4x4, 8x8, 16x16)
- Color reduction (Simulation of low BPP values, sort of...)
- Resolution downscaling to specific resolution and by scale

## Planned / Work in progress Features
- Demo application
- 3D effects support for Decals
- Сustomizable color palettes for post-processing (for example as image from Lospec)

# Installation
1. Add JitPack repository in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    ext {
        ...
        gdxpsxVersion = '1.0.1'
    }
}
```
2. Add the psx-gdx dependency
```
dependencies {
    ...
    implementation "com.github.bonenaut7.gdx-psx:gdx-psx-core:$gdxpsxVersion"
}
```

### [GDX-VFX](https://github.com/crashinvaders/gdx-vfx) Integration (optional)
3. Add vfx-integration dependency to use effects with [GDX-VFX](https://github.com/crashinvaders/gdx-vfx)
```
dependencies {
    ...
    implementation "com.github.bonenaut7.gdx-psx:gdx-psx-vfx-integration:$gdxpsxVersion"
}
```

### GWT (optional)
4. Add the sources dependency to your HTML gradle
```
dependencies {
    ...
    api "com.github.bonenaut7.gdx-psx:gdx-psx-core:$gdxpsxVersion:sources"

    // And if vfx-integration is used, add this line below
    api "com.github.bonenaut7.gdx-psx:gdx-psx-vfx-integration:$gdxpsxVersion:sources"
}
```

5. Inherit the module in your GdxDefinition.gwt.xml
```
<inherits name="by.bonenaut7.gdxpsx.gdx_psx"/>
```

# Quick start
**gdx-psx** have a lot of configurable parameters, recommended to check
the demo before starting work with library!

### 3D Mesh effects (Vertex Jitter/Texture Affine Mapping/LUT)
Library can provide Vertex Jitter effect to your models (via `ModelBatch`)
```java
//Create ModelBatch with PSX shader provider (You can also specify type of shader you need with PSXShaderType enum)
ModelBatch myModelBatch = new ModelBatch(new PSXShaderProvider());
Environmment environment = new Environment();
environment.set(AttributePSXEffect.createVertexSnapping(4.0F)); //add vertex snapping effect with 4.0 strength
environment.set(AttributePSXEffect.createTextureAffineMapping(0.5F)); //add affine texture mapping effect with 50% contribution
//Then you can render your models with environment, or add attributes primarily to your model materials!
```

### Post-processing (Downscaling, Screen dithering, Color reduction)
After the release, gdx-psx are not using in-built Framebuffers **at all**.

1. Create and customize post processing object
```java
// Static variant is in-built one, but you can create yours if you want!
PSXPostProcessingShader shader = new PSXPostProcessingShaderStatic();
shader.setDownscalingEnabled(true); // Enable Resolution downscaling
shader.setDownscalingFromScale(2f); // Set downscaling by scale, and make it twice smaller than input resolution

shader.setDitheringEnabled(true); // Enable Dithering
shader.setDitheringScale(2f); // Set dithering scaling the same as downscaling, so it would look good
shader.setDitheringIntensity(0.5f); // Make dithering intensity higher (default is 0.1)
shader.setDitheringMatrix(DitheringMatrix.BAYER_16x16); // Use Bayer 16x16 dithering matrix

shader.setColorReductionEnabled(true); // Enable Color reduction
shader.setColorReduction(16f); // Set color reduction factor as 16 (255 will produce almost unchanged image)

// Don't forget to set input resolution of your choice!
shader.setInputResolution(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
```

2. Update the shader so all changes made before will be applied!
```java
shader.update();
```

3. Use the shader with your batch! (Anything that will be drawn with batch will be processed with post-processing if shader is applied)
```java
batch.setShader(shader);
// draw anything!
batch.flush();
// * magic * //
```

OR

4. Use framebuffer to capture your 3D data and post-process the result!
```java
// Application resolution
int width = Gdx.graphics.getWidth();
int height = Gdx.graphics.getHeight();

// Creating framebuffer with `RGBA8888` format, app resolution and depth buffer to capture 3D data
Framebuffer framebuffer = new Framebuffer(Format.RGBA8888, width, height, true);

framebuffer.begin(); // Begin capturing with framebuffer
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); // Clear framebuffer
// Render anything with ModelBatch for example
framebuffer.end(); // Stop capturing with framebuffer

batch.setShader(shader); // Apply shader to a batch
batch.begin(); // Starting to draw with a batch
// Draw framebuffer color texture upside down (because all framebuffers capture data upside down :b)
batch.draw(framebuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, 1, 1);
batch.end(); // Flushing everything that we drawn with batch and stop the batch

// * magic * //
```

OR

5. In case if you're using vfx-integration you can do this!
```java
PSXPostProcessingEffect effect = new PSXPostProcessingEffect();
PSXPostProcessingShader shader = effect.getConfiguration();
// Configure shader as you wish, or as described in 1st step
// But keep in mind that's not required to set input resolution while using gdx-vfx
// PSXPostProcessingEffect manages input resolution by itself with help of gdx-vfx!

// GDX-VFX's VfxManager
VfxManager vfxManager = new VfxManager(Format.RGBA8888);
vfxManager.addEffect(effect); // yay!

// Then you can render anything as described in gdx-vfx quick guide!
```
