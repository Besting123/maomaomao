# 3D model assets

Place the exported cat model here for Android Studio builds:

- `mao-lihua-animated.glb` — primary cat model used by `CatModel3DViewer`; contains named presentation animations for UI feedback, but is not a rigged/skeletal character asset
- `cat.glb` — legacy static model kept as a fallback/source reference

Recommended final format: GLB with embedded textures, `skins`, a `skeleton`, and named animation clips such as `Idle`, `Pet`, `Drink`, `Eat`, and `Observe`.
