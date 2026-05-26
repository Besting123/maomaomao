# 3D model assets

Place the exported cat model here for Android Studio builds:

- `mao-xiaohei-rigged.glb` — current primary companion model used by `CatModel3DViewer`; first rigged “小黑” validation asset with `skins`, a skeleton, and action clip names `Idle`, `Observe`, `Pet`, `Drink`, `Eat`, and `Happy`
- `mao-lihua-animated.glb` — previous primary cat model; contains named presentation animations for UI feedback, but is not a rigged/skeletal character asset
- `cat.glb` — legacy rigged source reference used to prepare the first `mao-xiaohei-rigged.glb` validation asset

Recommended final format: GLB with embedded textures, `skins`, a `skeleton`, and named animation clips such as `Idle`, `Pet`, `Drink`, `Eat`, and `Observe`.
