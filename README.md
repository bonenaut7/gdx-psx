
[![](https://jitpack.io/v/fxgaming/gdx-psx.svg)](https://jitpack.io/#fxgaming/gdx-psx) [![](https://img.shields.io/badge/Support-Patreon-FF424D)](https://www.patreon.com/bePatron?u=34511646) [![](https://img.shields.io/badge/Community-Discord-5865F2)](https://discord.gg/2FqQQxyFS8)
# gdx-psx
**gdx-psx** its a library for [**LibGDX**](https://github.com/libgdx/libgdx) designed to assist you in simulation of PlayStation 1
graphics with few simple steps!

If you have questions or suggestions for this project, or you want to just chat about it - welcome to our [discord](https://discord.gg/2FqQQxyFS8).

## Features:
- Vertex Jitter(Inacuraccy)
- Screen dithering
- Color depth change
- Texture shrink(compression)
- Resolution downscaling

# Install
1. Add JitPack repository in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. Add the psx-gdx dependency
```
dependencies {
    ...
    implementation 'com.github.fxgaming:gdx-psx:0.1.2a'
}
```

# Quick start
**gdx-psx** have a lot of configurable parameters, recommended to check
the demo before starting work with library!

### Textures (Texture shrinking)
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

### 3D effects (Vertex Jitter)
Library can provide Vertex Jitter effect to your models (via `ModelBatch`)
```java
//You can provide your own shaders, but using libgdx's default shader by default.
ShaderTransformer transformer = new ShaderTransformer(); 
transformer.setResolution(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
transformer.setFactor(4f); //Jitter factor
//You can provide Config for ShaderProvider if needed
ModelBatch myModelBatch = new ModelBatch(transformer.createShaderProvider());
```
Library also supports [gdx-gltf](https://github.com/mgsx-dev/gdx-gltf) by [mgsx](https://github.com/mgsx-dev)
To use `ShaderTransformer` with `PBRShaderProvider` use `PBRShaderTransformer`.

### Post-processing (Downscaling, Color depth, Screen dithering)
Also library provides few things to work with post-processing!

1. Create and customize post processing tool
```java
PSXPostProcessing postProcessing = new PSXPostProcessing(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
postProcessing.setDownscalingIntensity(4f);
postProcessing.setColorDepth(32f, 32f, 32f);
postProcessing.setDitheringMatrix(DitherMatrix.Dither4x4);
postProcessing.setFlagState(FlagType.DITHERING, true);
```
2. Put this in the render loop
```java
postProcessing.capture();
//rendering everything
postProcessing.endCapture();
postProcessing.drawImage();
```
