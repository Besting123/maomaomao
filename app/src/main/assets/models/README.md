# 3D model assets

Place the exported cat model here for Android Studio builds:

- `mao-xiaohei-rigged.glb` — current primary companion model used by `CatModel3DViewer`; rigged “小黑” validation asset with `skins`, a skeleton, and action clip names `Idle`, `Observe`, `Pet`, `Drink`, `Eat`, and `Happy`
- `cat.glb` — legacy rigged source reference; used as input to `tools/prepare_xiaohei_rigged_asset.py` to produce `mao-xiaohei-rigged.glb`

Recommended final format: GLB with embedded textures, `skins`, a `skeleton`, and named animation clips such as `Idle`, `Observe`, `Pet`, `Drink`, `Eat`, and `Happy`.
