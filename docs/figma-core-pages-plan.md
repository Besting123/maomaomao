# Figma Core Pages Restoration Plan

Target Figma file: https://www.figma.com/design/c8rD5pNdV1ldumfdOxSq1p/Untitled

## Current Status

This workspace can analyze the Android Compose project and extract the page structure, visual tokens, components, and image assets. The current Codex session does not expose a Figma write tool such as `use_figma` or `search_design_system`, so the Figma file cannot be modified directly from this session yet.

Once Figma MCP/write access is available, restore the app as mobile frames using the plan below.

## Source Scope

Core source files:

- `app/src/main/java/com/example/myapplication/ui/screens/MainScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/CampusScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/CompanionScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/CatProfileScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/EducationScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/TaskScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/ForumScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/screens/ProfileScreen.kt`
- `app/src/main/java/com/example/myapplication/ui/theme/Color.kt`
- `app/src/main/java/com/example/myapplication/ui/navigation/BottomNavItem.kt`

## Recommended Figma Frames

Use mobile portrait frames at `390 x 844` or `393 x 852`, with Android status area and the custom bottom navigation where applicable.

Primary tab pages:

1. `01 Home / 首页`
   - Fixed translucent top bar with avatar, greeting, token pill, notification icon.
   - Image hero with dark brown vertical gradient overlay.
   - Safety alert pill.
   - Daily mission two-card row.
   - Core function bento: campus map, companion, education, cat profile.
   - Activity cards and followed cat avatars.
   - Bottom navigation selected on Home.

2. `02 Campus Map / 校园地图`
   - Full-screen map placeholder or imported static map reference.
   - Top app bar: title, subtitle, location icon.
   - Time segmented selector: 清晨 / 午后 / 傍晚 / 夜间.
   - Map marker pins for 大橘, 云朵, 奶油.
   - Bottom cat marker card state with habit, approach advice, safety chip, profile action.
   - Bottom navigation selected on Campus.

3. `03 Companion / 云陪伴`
   - Soft layered background using background, surface, primary-container, tertiary-container.
   - Top bar with selected cat pill and token pill.
   - Large rounded hero card with 3D cat preview placeholder.
   - Action dock: 观察, 补水, 安抚, 添粮.
   - State meters: 饱食, 心情, 健康, EXP.
   - Long-term companion records.
   - Bottom navigation selected on Companion.

4. `04 Forum / 论坛`
   - Top bar with avatar, title 共护, search and notification buttons.
   - Recognition tabs: 本校 / 周边学校 / 社区内容流.
   - Category chip row: 目击记录, 组队活动, 知识分享, 求助信息, 经验分享, 猫咪日记, 片区记录, 全部.
   - Published post cards.
   - Team event card, knowledge share card, emergency card, sighting card.
   - Floating add button.
   - Bottom navigation selected on Forum.

5. `05 Profile / 我的`
   - Fixed top bar.
   - Profile hero with avatar, level badge, school chip, observer chip.
   - Token balance card with exchange action.
   - Long-term companion stats bento.
   - Knowledge badge row.
   - Followed cat cards.
   - Timeline and settings list.
   - Bottom navigation selected on Profile.

Secondary core pages:

6. `06 Cat Profile / 猫咪档案`
   - Back top bar with share and more icons.
   - Tall image hero with tags, cat name, code line, health pill, follow action.
   - Mood indicator.
   - Interaction boundary.
   - Personality, memory polaroid, location/time chart, health advice, timeline, companion records, personality theater.

7. `07 Education / 新手学堂`
   - Standard top app bar with back.
   - Progress tracker card.
   - Safety principles card.
   - Horizontal knowledge card gallery.
   - Daily quiz card with answer states.

8. `08 Tasks / 任务中心`
   - Standard top app bar with back.
   - Sign-in streak card with seven-day row.
   - Token and task progress stat cards.
   - Task categories: 安全陪伴任务, 学习任务, 特殊成就.
   - Completed and claimable task rows.

9. `09 Forum Detail / 论坛详情与评论`
   - Back top bar.
   - Original post content panel.
   - Comment list.
   - Bottom comment input and send button.

## Visual Tokens

Colors extracted from `Color.kt`:

- `primary`: `#8B5928`
- `onPrimary`: `#FFFFFF`
- `primaryContainer`: `#FDBC82`
- `onPrimaryContainer`: `#623808`
- `secondary`: `#4D6C51`
- `onSecondary`: `#FFFFFF`
- `secondaryContainer`: `#C9EBCA`
- `onSecondaryContainer`: `#3A593F`
- `tertiary`: `#416A76`
- `onTertiary`: `#FFFFFF`
- `tertiaryContainer`: `#C4EFFD`
- `onTertiaryContainer`: `#315B66`
- `error`: `#AE4025`
- `onError`: `#FFFFFF`
- `background`: `#FFFCF7`
- `onBackground`: `#383833`
- `surface`: `#FFFCF7`
- `onSurface`: `#383833`
- `onSurfaceVariant`: `#65655E`
- `surfaceContainerLowest`: `#FFFFFF`
- `surfaceContainerLow`: `#FCF9F3`
- `surfaceContainer`: `#F6F3ED`
- `surfaceContainerHigh`: `#F0EEE7`
- `surfaceContainerHighest`: `#EAE8E0`
- `surfaceVariant`: `#EAE8E0`

Radii:

- Pills and chips: `999`
- Small controls: `8`
- Regular cards: `12`, `14`, `16`
- Large cards: `20`, `24`, `26`, `28`
- Companion hero: `36`
- Bottom nav top radius: `32`

Spacing:

- Screen horizontal padding: `20` or `24`
- Dense card padding: `12`, `14`, `16`
- Primary card padding: `18`, `20`, `22`, `24`
- Section spacing: `16`, `20`, `24`, `32`
- Bottom nav reserved space: about `96-120`

Typography:

- App bar title: `18`, bold
- Section title: `18-20`, bold or extra bold
- Large hero cat name: `48`, extra bold
- Home hero title: `22`, extra bold, line height `28`
- Card title: `14-18`, bold
- Body text: `12-14`, line height `18-22`
- Tiny chip labels: `10-11`, bold or semi-bold

## Shared Components To Build In Figma

- App top bar, with variants for home/profile/forum/back pages.
- Bottom navigation, selected state for five tabs.
- Pill / chip, selected and default states.
- Icon circle button.
- Primary rounded button.
- Text button.
- Standard card.
- Image hero card with gradient overlay.
- Stat card.
- Progress meter.
- Safety notice row.
- List row with icon avatar.
- Cat avatar chip/card.
- Timeline row.
- Dialog surface.
- Text input/comment composer.

## Asset Notes

Use images from:

- `app/src/main/res/drawable/img_net_*.png`
- `app/src/main/res/drawable/img_*.png`
- `app/src/main/assets/models/kitty.glb` and `cat.glb` for the companion hero reference if Figma supports imported preview imagery; otherwise use a static cat placeholder.

Map implementation uses osmdroid/Amap. In Figma, represent it as a static campus map placeholder with soft map tiles and marker pins unless an exact screenshot is available.

## Figma Execution Order

1. Inspect target Figma file pages and existing styles/components.
2. Create or update a `Core Pages` page.
3. Create local variables for the color tokens above if the file has no compatible library tokens.
4. Create shared components first: top bars, bottom nav, buttons, chips, cards, meters, list rows.
5. Build the five primary tab frames.
6. Build secondary frames for cat profile, education, tasks, and forum detail.
7. Add prototype links matching navigation:
   - Home bento to Campus, Companion, Education, Cat Profile.
   - Bottom nav among five primary pages.
   - Campus marker card to Cat Profile.
   - Forum post to Forum Detail.
   - Home daily mission to Tasks.
8. Review screenshots for clipped text, overlapping text, image cropping, and bottom-nav spacing.

