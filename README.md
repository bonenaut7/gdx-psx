
[![](https://jitpack.io/v/fxgaming/gdx-psx.svg)](https://jitpack.io/#fxgaming/gdx-psx) [![](https://img.shields.io/badge/Community-Discord-5865F2)](https://discord.gg/2FqQQxyFS8)
# gdx-psx
![](https://m3.fxg.by/psxlogo.gif) 

**gdx-psx** its a library for [**LibGDX**](https://github.com/libgdx/libgdx) designed to assist you in simulation of PlayStation 1
graphics with few simple steps!
If you have questions or suggestions for this project, or you want to just chat about it - welcome to our [discord](https://discord.gg/2FqQQxyFS8).

## Features:
- Vertex Jitter (Vertex Snapping)
- Screen dithering with Bayer matrices based on Color depth reduction
- Resolution downscaling to specific resolution and by factor
With few deprecated features like texture color depth reduction :b

## Demo:
You can look at demo [here](https://m3.fxg.by/gdxpsx.mp4)

# Install
1. Add JitPack repository in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    ext {
        ...
        gdxpsxVersion = '0.2.0'
    }
}
```
2. Add the psx-gdx dependency
```
dependencies {
    ...
    implementation 'com.github.bonenaut7:gdx-psx:$gdxpsxVersion'
}
```

### GWT (optional)

3. Add the sources dependency to your HTML gradle
```
dependencies {
    ...
    api "com.github.bonenaut7:gdx-psx:$gdxpsxVersion:sources"
}
```

4. Inherit the module in your GdxDefinition.gwt.xml
```
<inherits name="by.fxg.gdxpsx.gdx_psx"/>
```

# Quick start
**gdx-psx** have a lot of configurable parameters, recommended to check
the demo before starting work with library!

### 3D Mesh effects (Vertex Jitter/Texture Affineness/LUT)
Library can provide Vertex Jitter effect to your models (via `ModelBatch`)
```java
//Create ModelBatch with PSX shader provider (You can also specify type of shader you need with PSXShaderType enum)
ModelBatch myModelBatch = new ModelBatch(new PSXShaderProvider());
Environmment environment = new Environment();
environment.set(AttributePSXEffect.createVertexJitter(4.0F)); //add vertex jitter effect with 4.0 strength
environment.set(AttributePSXEffect.createTextureAffineness(0.5F)); //add texture affineness effect with 50% contribution
//Then you can render your models with environment, or add attributes primarily to your model materials!
```

### Post-processing (Downscaling, Color depth, Screen dithering)
After 0.2 update things became slightly bigger than before. But believe me, this works faster than before!
Along with speed, post-processing split to 2 different parts and now you can integrate Post-processing into something else with reference how `PSXPostProcessingWrapper` is made, or reuse FrameBuffer from it!

1. Create and customize post processing object
```java
PSXPostProcessing postProcessing = new PSXPostProcessing(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
postProcessing.setDownscalingIntensity(4f);
postProcessing.setColorDepth(32f, 32f, 32f);
postProcessing.setDitheringMatrix(DitheringMatrix.BAYER_8x8);

// Simple - Use default filters and their parameters
PSXPostProcessing postProcessing = new PSXPostProcessing();
postProcessing.setDefaultParametersWithResolution();

// Flexible - Use your own values as you wish!
PSXPostProcessing postProcessing = new MyPSXPostProcessingImpl(); // If you wish to add something
postProcessing.setInputResolution(800, 600);
postProcessing.setResolutionDownscalingFitToResolution(320, 240);
postProcessing.setDitheringMatrix(DitheringMatrix.BAYER_4x4, 1, /* color depth */ 48f, /* scale */ 2f);
```

2. After you set everything like you want, choose the shader type. There are 2 types of shaders: Static and Dynamic. Dynamic ones are more suitable for development stage because they're allowing to change parameters in real-time, Static type is more optimized and requires to recompile shader after any changes to apply them.
```java
postProcessing.compile(true); // To compile dynamic shader
postProcessing.compile(false); // To compile static shader
```

3. Setup wrapper for your post-processing object if you don't have any free framebuffer :)
```java
PSXPostProcessingWrapper wrapper = new PSXPostProcessingWrapper(postProcessing);
wrapper.createFrameBuffer(); // create framebuffer with default parameters
// or
wrapper.createFrameBuffer(Format.RGB565, 400, 300, true); // create one with your parameters!
```

4. Put everything into a render loop! You need to prepare a Batch before doing this
```java
wrapper.beginFrameBufferCapture();
// rendering best horror game ever
wrapper.endFrameBufferCapture();

wrapper.drawPostProcessedTexture(batch);
// or you can do this in a fashion way :*
wrapper.drawPostProcessedTexture(batch, 0, 0, 1337, 420);
```

### Textures (Texture shrinking, Deprecated)
Library provides tool that helping you process textures and and downgrade their quality! <br/>
Fast texture processing: <br/>
`TextureTransformer.shrinkTexture(Pixmap, ResizeType, textureSizeFactor, colorDepthFactor);` <br/>
or with recommended parameters: <br/>
`TextureTransformer.shrinkTexture(pixmap, ResizeType.FORCE, 192f, 32f);` <br/>
Instead of using `Pixmap` you can use `FileHandle` with your texture's path.

If you need to process bigger amount of textures with specified parameters:
```java
TextureTransformer transformer = new TextureTransformer(); //using recommended parameters by default
transformer.setResizeType(ResizeType.ACCORDING_TO_HEIGHT);
transformer.setColorDepthFactor(32f);
//...
Texture newTexture = transformer.shrinkTexture(fileHandle);
```
