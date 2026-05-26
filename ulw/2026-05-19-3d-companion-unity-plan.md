# ULW 计划：陪伴界面 3D 猫模型替换与 Unity 引擎引入

## 1. 目标

把当前“云陪伴”界面的过渡型 3D 猫模型升级为更适合产品气质的可爱、低压力、可互动校园猫形象，并为后续 Unity 引擎制作建立清晰落地路径。

本计划只规划本轮工作，不直接扩大到后端、真实猫谱接入、账号系统或完整游戏化系统。

---

## 2. 当前代码读取结论

### 2.1 当前项目类型

- 当前项目是 **Android Kotlin + Jetpack Compose** 应用。
- 当前主要模块是 `:app`。
- 当前 UI 已覆盖首页、校园地图、云陪伴、猫咪档案、新手学堂、论坛和个人页。

### 2.2 当前 3D 渲染实现

- 当前 3D 渲染组件：`app/src/main/java/com/example/myapplication/ui/components/CatModel3DViewer.kt`
- 当前使用渲染栈：`SceneView 4.0.1` + Filament。
- 当前模型资源路径：`app/src/main/assets/models/mao-lihua-animated.glb`
- 当前环境光资源：`app/src/main/assets/environments/studio.hdr`
- 当前备用模型：`app/src/main/assets/models/cat.glb`

### 2.3 当前陪伴界面接入点

- 云陪伴页面：`app/src/main/java/com/example/myapplication/ui/screens/CompanionScreen.kt`
  - `CompanionHeroCard` 中调用 `CatModel3DViewer`。
  - 当前传入模型：`models/mao-lihua-animated.glb`。
  - 当前动作映射：
    - 安抚 → `Pet`
    - 观察 → `Observe`
    - 补水 → `Drink`
    - 添粮 → `Eat`
- 猫咪档案页面：`app/src/main/java/com/example/myapplication/ui/screens/CatProfileScreen.kt`
  - `CatProfileHeroSection` 中调用 `CatModel3DViewer`。
  - 当前同样使用 `models/mao-lihua-animated.glb`。

### 2.4 当前模型的问题

项目内 `app/src/main/assets/models/README.md` 已明确说明：

- `mao-lihua-animated.glb` 是当前主模型；
- 它包含用于 UI 反馈的命名表现动画；
- 但它不是最终理想资产；
- 它不是带真实骨骼 / 蒙皮的角色资产。

因此，下一步不应继续包装当前模型，而应替换为真正带 `skins`、`skeleton` 和命名动画 clips 的猫咪 GLB。

---

## 3. 新 3D 模型方案

### 3.1 模型定位

新模型建议制作为：**治愈系低多边形校园狸花猫“小黑”**。

选择理由：

- 与当前页面文案中的“小黑”一致，不需要改动用户叙事；
- 狸花猫贴近校园流浪猫真实形象；
- 低多边形 / 卡通半写实风格更适合移动端性能；
- 可爱但不夸张，符合“科学接近、安静陪伴、不打扰”的产品语气。

### 3.2 模型美术要求

- 体型：坐姿或半蹲姿为主，适合放在陪伴页舞台中。
- 风格：卡通半写实、温和、干净，不做攻击性或过度拟人化表达。
- 颜色：狸花纹理，深灰棕主色，脸部和胸口可保留浅色区域增强识别度。
- 面数：移动端优先，控制在可流畅加载的范围内。
- 材质：PBR 基础材质即可，不使用过重的透明、毛发或复杂 shader。
- 贴图：优先嵌入 GLB，减少 Android assets 管理复杂度。

### 3.3 模型技术要求

最终模型文件建议命名为：

```text
app/src/main/assets/models/mao-xiaohei-rigged.glb
```

必须包含：

- `skins`
- `skeleton`
- 嵌入贴图
- 合理缩放与原点
- 命名动画 clips

建议动画 clips：

| Clip 名称 | 用途 | 对应当前 UI 动作 |
|---|---|---|
| `Idle` | 默认待机、轻微呼吸、眨眼 | 页面初始状态 |
| `Observe` | 抬头、转头、保持距离观察 | 观察 |
| `Pet` | 放松、眯眼、尾巴轻摆 | 安抚 |
| `Drink` | 低头饮水或靠近水碗 | 补水 |
| `Eat` | 少量进食或靠近食盆 | 添粮 |
| `Happy` | 短反馈动作，可选 | 成功互动反馈 |
| `Nervous` | 轻微后退或警觉，可选 | 错误接近 / 余额不足 / 风险提示 |

### 3.4 建模工具链

优先路径：

1. Blender 制作或整理模型；
2. Rigify / 手动骨架完成基础绑定；
3. 制作上述命名动画；
4. 导出 GLB；
5. 用 glTF 查看器检查 `skins`、`skeleton`、动画 clips 和贴图是否完整；
6. 放入 `app/src/main/assets/models/`；
7. 先用现有 SceneView 组件替换验证。

---

## 4. 先替换当前陪伴界面模型

### 4.1 最小替换范围

只改以下接入点：

- `CatModel3DViewer.kt` 默认模型路径；
- `CompanionScreen.kt` 中 `CatModel3DViewer` 的 `modelAssetPath`；
- `CatProfileScreen.kt` 中 `CatModel3DViewer` 的 `modelAssetPath`；
- `app/src/main/assets/models/README.md` 中的资产说明。

不改：

- 页面布局结构；
- 底部导航；
- ViewModel 状态结构；
- 地图；
- 后端；
- 论坛和任务系统。

### 4.2 替换后的代码策略

当前 `CompanionScreen` 已经用 `animationName` 驱动模型动作，因此新模型只要保持动画 clip 名称一致，就可以最大限度减少代码改动。

建议保持现有动作名：

```kotlin
"Pet"
"Observe"
"Drink"
"Eat"
"Idle"
```

若模型导出的动画名称不同，应优先在 Blender / Unity 侧改动画命名，而不是在 Compose 侧新增复杂映射。

### 4.3 替换验收

- 云陪伴页可以正常加载 `mao-xiaohei-rigged.glb`。
- 猫咪档案页可以正常加载同一模型。
- `Idle` 默认动作可播放。
- 点击安抚、观察、补水、添粮时，模型能切换到对应动作。
- 拖动旋转、双击安抚、前景特效、地台阴影仍保留。
- 模型加载失败时仍能显示现有缺失提示，不出现崩溃。

---

## 5. Unity 引擎引入方案

### 5.1 技术判断

当前项目是 Android 原生应用，不是 Web 项目。因此 Unity 引入应优先考虑：

```text
Unity as a Library for Android
```

而不是 Unity WebGL。

依据：

- Unity 官方 Android 集成方式会导出 `unityLibrary` 模块；
- 原生 Android 项目可在 `settings.gradle` 中引入该模块；
- Unity Runtime Library 可用于 3D/2D 实时渲染、模型交互或小型互动场景；
- 但 Unity as a Library 在 Android 上通常更适合完整 Unity 页面或全屏渲染，不适合直接嵌入当前 Compose 卡片区域。

### 5.2 推荐分阶段策略

#### 阶段 A：先用 SceneView 完成模型资产替换

目标：最快让当前 App 的陪伴页换上真正可爱的骨骼动画猫模型。

原因：

- 当前代码已经接好 SceneView；
- 现有 UI 已围绕 `CatModel3DViewer` 完成；
- 替换 GLB 风险低、改动小；
- 能保留现有 Compose 页面、状态和教学反馈。

#### 阶段 B：建立 Unity 制作工程

建议在项目中新增 Unity 工程目录：

```text
unity/companion-cat/
```

Unity 工程职责：

- 导入 `mao-xiaohei-rigged.glb` 或源模型；
- 配置 Animator Controller；
- 制作 Idle / Pet / Observe / Drink / Eat 等状态机；
- 制作简单地台、水碗、食盆、柔光环境；
- 暴露动作切换接口，例如 `PlayAction("Pet")`；
- 导出 Android Gradle Project。

#### 阶段 C：Android 引入 Unity Library

Unity 导出 Android Gradle Project 后，预期结构包含：

```text
unity-export/
  launcher/
  unityLibrary/
```

Android 项目侧后续再做：

1. 在 `settings.gradle.kts` 引入 `unityLibrary`；
2. 在 `app/build.gradle.kts` 增加 Unity library 依赖；
3. 增加 Unity 入口 Activity 或封装页面；
4. 从 Compose 的“云陪伴”页面跳转到 Unity 全屏互动页；
5. 通过 Android / Unity 消息桥传递动作：`Pet`、`Observe`、`Drink`、`Eat`；
6. 返回 Compose 页面时同步当前互动结果。

### 5.3 Unity 与当前 Compose 页面的边界

不建议第一步就把当前 `CompanionHeroCard` 内部模型直接替换成 Unity 视图。

原因：

- 当前页面是 Compose 卡片式布局；
- Unity as a Library 在 Android 上更偏全屏 Unity Runtime；
- Unity Runtime 内存成本较高；
- 直接嵌入会增加生命周期、返回栈、音频、渲染线程和包体复杂度。

推荐边界：

- Compose 继续负责普通云陪伴页面；
- SceneView 继续负责卡片内轻量 3D 展示；
- Unity 负责“沉浸式陪伴”或“高级互动演示”全屏入口。

---

## 6. 具体任务拆分

### P0：资产确认与命名

- [x] 确认新模型名称为 `mao-xiaohei-rigged.glb`。
- [x] 确认模型授权或自制来源：本轮先由项目内 `cat.glb` 生成第一个带骨骼验证资产，保留其原始 CC-BY 4.0 来源信息；后续可用同名最终美术资产替换。
- [x] 检查模型是否包含 `skins`、`skeleton`、嵌入贴图和命名 clips。
- [x] 更新 `app/src/main/assets/models/README.md`。

### P1：SceneView 替换当前陪伴模型

- [x] 将 `mao-xiaohei-rigged.glb` 放入 `app/src/main/assets/models/`。
- [x] 修改 `CatModel3DViewer.kt` 默认路径。
- [x] 修改 `CompanionScreen.kt` 的 `modelAssetPath`。
- [x] 修改 `CatProfileScreen.kt` 的 `modelAssetPath`。
- [ ] 检查模型缩放、站位、旋转中心和地台接触阴影。
- [ ] 验证四个动作按钮是否能触发对应动画。

### P2：Unity 工程制作

- [x] 新建 `unity/companion-cat/` Unity 工程。
- [x] 导入新猫模型和场景道具。
- [x] 建立 Animator Controller。
- [x] 建立动作状态：Idle / Observe / Pet / Drink / Eat。
- [x] 增加 C# 控制脚本，例如 `CompanionCatController`。
- [x] 提供统一方法：`PlayAction(string actionName)`。
- [x] 制作低复杂度灯光、相机和舞台。

### P3：Unity Android 导出

- [ ] Unity 切换 Android 平台。
- [ ] 开启 Export Project。
- [ ] 导出 Android Gradle Project 到 `unity-export/`。
- [ ] 保留 `unityLibrary`，不直接使用 Unity 生成的 `launcher` 替代当前 app。
- [ ] 记录 Unity 版本、Android Gradle Plugin 版本和导出设置。

### P4：Android 接入 Unity Library

- [ ] 在 `settings.gradle.kts` 中引入 `unity-export/unityLibrary`。
- [ ] 在 `app/build.gradle.kts` 中增加 Unity library 依赖。
- [ ] 新增 Unity 入口 Activity 或封装页面。
- [ ] 从 `CompanionScreen.kt` 增加“沉浸式陪伴”入口。
- [ ] 建立 Android → Unity 的动作消息桥。
- [ ] 建立 Unity → Android 的互动完成回调。
- [ ] 返回 Compose 页面后复用 `MainViewModel.interactWithCat(...)` 更新状态。

---

## 7. 验收标准

### 7.1 模型替换验收

- [ ] 当前陪伴页不再使用 `mao-lihua-animated.glb` 作为主展示模型。
- [ ] 新模型能在 `CompanionScreen` 和 `CatProfileScreen` 中正常展示。
- [ ] 新模型具备真实骨骼 / 蒙皮动画。
- [ ] 四个陪伴动作对应动画清晰可见。
- [ ] APK 体积和加载速度仍可接受。

### 7.2 Unity 引入验收

- [ ] 项目中存在可打开的 Unity 工程目录。
- [ ] Unity 工程能播放同一只猫的互动动作。
- [ ] Unity 可导出 Android Gradle Project。
- [ ] Android 项目可识别 `unityLibrary`。
- [ ] Compose 页面可以进入 Unity 全屏互动体验。
- [ ] Unity 返回后，当前云陪伴状态不丢失。

---

## 8. 风险与限制

| 风险 | 影响 | 处理方式 |
|---|---|---|
| 模型没有真实骨骼动画 | 无法达成“真实动作”目标 | 资产进入项目前先检查 `skins` / `skeleton` |
| GLB 贴图过大 | APK 体积和加载时间增加 | 压缩贴图，控制面数，优先移动端规格 |
| 动画名称不一致 | 当前按钮无法正确触发动作 | 统一动画 clip 命名，避免在代码中堆映射 |
| Unity 接入过重 | 包体、内存、启动耗时增加 | 保持 SceneView 作为默认轻量展示，Unity 只做沉浸式入口 |
| Unity as a Library 生命周期复杂 | 返回栈、暂停恢复、内存释放可能出问题 | 单独阶段验证，不和模型替换混在一个 PR / 一次提交中 |
| Unity 版本和 Gradle 版本冲突 | Android 构建失败 | 记录 Unity 版本，单独分支验证导出工程 |

---

## 9. 不做范围

本计划明确不包含：

- 不接真实后端；
- 不接真实 BJTU 猫谱数据；
- 不重构整体导航；
- 不重写现有 Compose 页面；
- 不把地图功能改回实时找猫；
- 不新增复杂商业化喂养系统；
- 不承诺所有猫都有 3D 模型；
- 不在 Unity 验证前删除现有 SceneView 方案。

---

## 10. 推荐执行顺序

1. 先制作 / 获取 `mao-xiaohei-rigged.glb`。
2. 先在当前 SceneView 中替换模型，完成陪伴页和档案页验证。
3. 再建立 `unity/companion-cat/` 工程，复用同一模型资产制作互动场景。
4. 再导出 `unityLibrary`，以全屏“沉浸式陪伴”方式接入 Android。
5. 最后再评估是否保留 SceneView + Unity 双路径，或 Unity 只作为高级展示入口。

---

## 11. 参考依据

- 当前项目计划：`plan.md`
- 当前设计目标：`design-description.md`
- 当前产品目标：`goal.md`
- 当前 3D 组件：`app/src/main/java/com/example/myapplication/ui/components/CatModel3DViewer.kt`
- 当前陪伴页：`app/src/main/java/com/example/myapplication/ui/screens/CompanionScreen.kt`
- 当前猫咪档案页：`app/src/main/java/com/example/myapplication/ui/screens/CatProfileScreen.kt`
- 当前模型说明：`app/src/main/assets/models/README.md`
- Unity Android 集成参考：Unity Manual, “Integrating Unity into Android applications / Unity as a Library”
- Unity Web 交互参考：Unity Manual, “JavaScript interface in Unity Web builds / Interaction with browser scripting”

---

## 12. 执行记录

### 2026-05-19 第一次执行

- 新增 `tools/prepare_xiaohei_rigged_asset.py`，用于从项目内 legacy rigged asset 生成 `mao-xiaohei-rigged.glb`。
- 生成目标模型：`app/src/main/assets/models/mao-xiaohei-rigged.glb`。
- 将 `CatModel3DViewer.kt`、`CompanionScreen.kt`、`CatProfileScreen.kt` 的默认 / 显式模型路径切换到 `models/mao-xiaohei-rigged.glb`。
- 更新 `app/src/main/assets/models/README.md`，记录当前主模型、旧模型和源参考资产。
- Unity 阶段暂不手写 `unityLibrary` / Gradle 接入；需等 Unity Editor 导出 `unity-export/unityLibrary` 后再进入 P3 / P4。

### 2026-05-19 第二次执行

- 在本机发现可用编辑器为 `D:\unity\Editor\Tuanjie.exe`（`2022.3.62t7` / `m_TuanjieEditorVersion: 1.8.5`），并在仓库内创建 `unity/companion-cat/`。
- 通过 Tuanjie batchmode 创建工程，并新增 `Assets/Editor/CompanionCatProjectBuilder.cs` 与 `Assets/Scripts/CompanionCatController.cs`。
- 在 `Packages/manifest.json` 中加入 `com.unity.cloud.gltfast: 6.17.0`，成功导入 `Assets/Models/mao-xiaohei-rigged.glb`。
- 已自动生成 `Assets/Scenes/CompanionCat.unity`、`Assets/Animations/XiaoheiCompanion.controller`、`Assets/Materials/CompanionStageGround.mat`，并将 `Idle / Observe / Pet / Drink / Eat / Happy` 六个 clip 接入 Animator Controller。
- 新增 `Assets/Editor/CompanionCatAndroidExporter.cs`，用于在 Android Build Support 就绪后将 Unity Android Gradle Project 导出到 `unity-export/`。
- 当前机器的 `D:\unity\Editor\Data\PlaybackEngines` 仅包含 `windowsstandalonesupport/`，缺少 `AndroidPlayer/`，因此 P3 仍被 Android Build Support 缺失所阻塞，暂不能实际导出 `unityLibrary`。

### 2026-05-19 第三次执行

- 已执行 Android 导出入口验证：`Maomaomao.CompanionCat.EditorTools.CompanionCatAndroidExporter.ExportAndroidGradleProject`。
- 验证日志：`ulw/tuanjie-export-android-missing-module.log`。日志显示 C# 脚本编译通过，但导出阶段按预期失败：`Android Build Support is not installed for the current Tuanjie/Unity editor. Install the Android playback module before exporting to unity-export.`，进程返回码为 `1`。
- 该失败是机器环境阻塞，不是 Unity 项目脚本错误；当前导出器会在缺少 Android 模块时提前停止，避免生成不完整或无效的 `unity-export/`。
- 已确认本机 `D:\unity\Editor\Data\PlaybackEngines` 下仍无 `AndroidPlayer/`，所以 P3 `unityLibrary` 导出继续保持阻塞状态。
- 已检查团结 / Tuanjie 官方 Android 环境说明：Android 导出需要通过 Hub 为当前 Editor 安装 `Android Build Support`、`Android SDK & NDK Tools`、`OpenJDK`。官方说明还建议使用 Hub 随 Editor 提供的依赖版本；Unity 2022.3 对应 JDK 11 与 NDK r23b。
- 本机缓存的 Tuanjie Hub release metadata 未发现已安装版本 `2022.3.62t7` 的精确 Android Support 安装条目，只发现 `2022.3.61t11` 与 `2022.3.62t8` 的模块信息。因此不要把 `2022.3.62t8` 的 Android Support 模块手动混装到 `2022.3.62t7`，除非 Tuanjie Hub 官方 UI 明确允许。
- 下一步机器操作：打开 `D:\unity Hub\Tuanjie Hub\Tuanjie Hub.exe`，在 Installs / 安装中对 `2022.3.62t7` 执行 Add Modules / 添加模块，安装 `Android Build Support`、`Android SDK & NDK Tools`、`OpenJDK`。如果 Hub 只提供 `2022.3.62t8`，优先升级 Editor 到 `2022.3.62t8` 并让 Hub 安装同版本 Android 模块，再重新打开 / 升级 `unity/companion-cat/` 工程。
- Android 模块安装完成后，重新运行：`D:\unity\Editor\Tuanjie.exe -batchmode -quit -nographics -projectPath G:\maomaomao\unity\companion-cat -executeMethod Maomaomao.CompanionCat.EditorTools.CompanionCatAndroidExporter.ExportAndroidGradleProject -logFile G:\maomaomao\ulw\tuanjie-export-android.log`。预期产物为 `unity-export/unityLibrary` 及 Unity Android Gradle 导出工程。
- 已更新 `.gitignore`，忽略 Unity / Tuanjie 生成目录（`Library/`、`Temp/`、`Logs/`、`UserSettings/`、构建输出等）和 `unity-export/`，避免把本机缓存和导出产物误提交到仓库。
