using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Maomaomao.CompanionCat;
using UnityEditor;
using UnityEditor.Animations;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Maomaomao.CompanionCat.EditorTools
{
    public static class CompanionCatProjectBuilder
    {
        private static readonly string[] ActionNames =
        {
            "Idle",
            "Observe",
            "Pet",
            "Drink",
            "Eat",
            "Happy"
        };

        private const string SourceModelPath = @"G:\maomaomao\app\src\main\assets\models\mao-xiaohei-rigged.glb";
        private const string ModelAssetPath = "Assets/Models/mao-xiaohei-rigged.glb";
        private const string ControllerPath = "Assets/Animations/XiaoheiCompanion.controller";
        private const string ScenePath = "Assets/Scenes/CompanionCat.unity";
        private const string GroundMaterialPath = "Assets/Materials/CompanionStageGround.mat";

        public static void BuildAll()
        {
            try
            {
                EnsureProjectFolders();
                CopyGeneratedModel();
                AssetDatabase.Refresh(ImportAssetOptions.ForceSynchronousImport);
                ConfigureImportedModel();
                CreateAnimatorControllerAndScene();
                ConfigureBuildScene();
                AssetDatabase.SaveAssets();
                AssetDatabase.Refresh();
                Debug.Log("CompanionCatProjectBuilder completed successfully.");
            }
            catch (Exception exception)
            {
                Debug.LogException(exception);
                EditorApplication.Exit(1);
            }
        }

        private static void EnsureProjectFolders()
        {
            Directory.CreateDirectory(ToAbsolutePath("Assets/Animations"));
            Directory.CreateDirectory(ToAbsolutePath("Assets/Materials"));
            Directory.CreateDirectory(ToAbsolutePath("Assets/Models"));
            Directory.CreateDirectory(ToAbsolutePath("Assets/Scenes"));
            Directory.CreateDirectory(ToAbsolutePath("Assets/Scripts"));
        }

        private static void CopyGeneratedModel()
        {
            if (!File.Exists(SourceModelPath))
            {
                throw new FileNotFoundException("Generated Xiaohei GLB source asset was not found.", SourceModelPath);
            }

            string destinationPath = ToAbsolutePath(ModelAssetPath);
            Directory.CreateDirectory(Path.GetDirectoryName(destinationPath));
            File.Copy(SourceModelPath, destinationPath, true);
            Debug.Log($"Copied Xiaohei GLB into Unity project: {ModelAssetPath}");
        }

        private static void ConfigureImportedModel()
        {
            AssetDatabase.ImportAsset(ModelAssetPath, ImportAssetOptions.ForceSynchronousImport);

            ModelImporter modelImporter = AssetImporter.GetAtPath(ModelAssetPath) as ModelImporter;
            if (modelImporter == null)
            {
                Debug.LogWarning("The GLB file was copied into Assets/Models, but this Unity installation did not expose it as a ModelImporter. Install/enable a GLB importer such as Unity glTFast if the prefab is missing.");
                return;
            }

            modelImporter.importAnimation = true;
            modelImporter.animationType = ModelImporterAnimationType.Generic;
            modelImporter.materialImportMode = ModelImporterMaterialImportMode.ImportStandard;
            modelImporter.SaveAndReimport();
            Debug.Log("Configured imported Xiaohei model as Generic animation model.");
        }

        private static void CreateAnimatorControllerAndScene()
        {
            AnimatorController controller = RecreateAnimatorController();
            Scene scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);

            GameObject stageRoot = new GameObject("CompanionCatStage");
            GameObject catObject = InstantiateCatObject();
            catObject.transform.SetParent(stageRoot.transform, true);
            catObject.transform.position = Vector3.zero;
            catObject.transform.rotation = Quaternion.Euler(0f, 180f, 0f);
            catObject.transform.localScale = Vector3.one;

            Animator animator = catObject.GetComponent<Animator>();
            if (animator == null)
            {
                animator = catObject.AddComponent<Animator>();
            }
            animator.runtimeAnimatorController = controller;

            CompanionCatController catController = catObject.GetComponent<CompanionCatController>();
            if (catController == null)
            {
                catObject.AddComponent<CompanionCatController>();
            }

            CreateStageGround(stageRoot.transform);
            CreateStageLighting(stageRoot.transform);
            CreateStageCamera(stageRoot.transform);

            EditorSceneManager.SaveScene(scene, ScenePath);
            Debug.Log($"Saved companion cat Unity scene: {ScenePath}");
        }

        private static AnimatorController RecreateAnimatorController()
        {
            if (AssetDatabase.LoadAssetAtPath<AnimatorController>(ControllerPath) != null)
            {
                AssetDatabase.DeleteAsset(ControllerPath);
            }

            AnimatorController controller = AnimatorController.CreateAnimatorControllerAtPath(ControllerPath);
            AnimatorStateMachine stateMachine = controller.layers[0].stateMachine;
            stateMachine.states = Array.Empty<ChildAnimatorState>();

            Vector3 statePosition = new Vector3(240f, 0f, 0f);
            foreach (string actionName in ActionNames)
            {
                AnimatorState state = stateMachine.AddState(actionName, statePosition);
                state.motion = GetOrCreateAnimationClip(actionName);
                state.writeDefaultValues = true;

                if (actionName == "Idle")
                {
                    stateMachine.defaultState = state;
                }

                statePosition.y += 60f;
            }

            Debug.Log($"Created Animator Controller with states: {string.Join(", ", ActionNames)}");
            return controller;
        }

        private static AnimationClip GetOrCreateAnimationClip(string actionName)
        {
            AnimationClip importedClip = FindImportedClip(actionName);
            if (importedClip != null)
            {
                return importedClip;
            }

            string fallbackClipPath = $"Assets/Animations/{actionName}.anim";
            AnimationClip existingClip = AssetDatabase.LoadAssetAtPath<AnimationClip>(fallbackClipPath);
            if (existingClip != null)
            {
                return existingClip;
            }

            AnimationClip fallbackClip = CreateFallbackClip(actionName);
            AssetDatabase.CreateAsset(fallbackClip, fallbackClipPath);
            Debug.LogWarning($"Created fallback animation clip for '{actionName}' because no imported GLB clip was available.");
            return fallbackClip;
        }

        private static AnimationClip FindImportedClip(string actionName)
        {
            UnityEngine.Object[] modelAssets = AssetDatabase.LoadAllAssetsAtPath(ModelAssetPath);
            return modelAssets
                .OfType<AnimationClip>()
                .FirstOrDefault(clip => string.Equals(clip.name, actionName, StringComparison.OrdinalIgnoreCase));
        }

        private static AnimationClip CreateFallbackClip(string actionName)
        {
            AnimationClip clip = new AnimationClip
            {
                name = actionName,
                frameRate = 30f,
                wrapMode = WrapMode.Loop
            };

            float rotationAmount = actionName == "Idle" ? 0f : 8f;
            AnimationCurve rotationCurve = AnimationCurve.EaseInOut(0f, -rotationAmount, 0.5f, rotationAmount);
            rotationCurve.AddKey(1f, -rotationAmount);
            AnimationUtility.SetEditorCurve(
                clip,
                EditorCurveBinding.FloatCurve(string.Empty, typeof(Transform), "localEulerAnglesRaw.y"),
                rotationCurve);

            return clip;
        }

        private static GameObject InstantiateCatObject()
        {
            GameObject importedModel = AssetDatabase.LoadAssetAtPath<GameObject>(ModelAssetPath);
            if (importedModel != null)
            {
                GameObject instance = PrefabUtility.InstantiatePrefab(importedModel) as GameObject;
                if (instance != null)
                {
                    instance.name = "XiaoheiRiggedCat";
                    return instance;
                }
            }

            GameObject placeholder = GameObject.CreatePrimitive(PrimitiveType.Capsule);
            placeholder.name = "XiaoheiRiggedCat_GlbImportPlaceholder";
            placeholder.transform.localScale = new Vector3(0.7f, 0.45f, 0.7f);
            Debug.LogWarning("Created placeholder cat object because the GLB was not importable as a GameObject in this Unity installation.");
            return placeholder;
        }

        private static void CreateStageGround(Transform parent)
        {
            Material groundMaterial = AssetDatabase.LoadAssetAtPath<Material>(GroundMaterialPath);
            if (groundMaterial == null)
            {
                groundMaterial = new Material(Shader.Find("Standard"))
                {
                    name = "CompanionStageGround"
                };
                groundMaterial.color = new Color(0.22f, 0.25f, 0.22f, 1f);
                AssetDatabase.CreateAsset(groundMaterial, GroundMaterialPath);
            }

            GameObject ground = GameObject.CreatePrimitive(PrimitiveType.Plane);
            ground.name = "CompanionStageGround";
            ground.transform.SetParent(parent, false);
            ground.transform.localScale = new Vector3(1.8f, 1f, 1.8f);
            Renderer renderer = ground.GetComponent<Renderer>();
            if (renderer != null)
            {
                renderer.sharedMaterial = groundMaterial;
            }
        }

        private static void CreateStageLighting(Transform parent)
        {
            GameObject keyLightObject = new GameObject("CompanionKeyLight");
            keyLightObject.transform.SetParent(parent, false);
            keyLightObject.transform.rotation = Quaternion.Euler(45f, -35f, 0f);
            Light keyLight = keyLightObject.AddComponent<Light>();
            keyLight.type = LightType.Directional;
            keyLight.intensity = 1.15f;

            RenderSettings.ambientMode = UnityEngine.Rendering.AmbientMode.Flat;
            RenderSettings.ambientLight = new Color(0.35f, 0.38f, 0.42f, 1f);
        }

        private static void CreateStageCamera(Transform parent)
        {
            GameObject cameraObject = new GameObject("CompanionCamera");
            cameraObject.transform.SetParent(parent, false);
            cameraObject.transform.position = new Vector3(0f, 1.1f, -3.2f);
            cameraObject.transform.rotation = Quaternion.Euler(13f, 0f, 0f);
            Camera camera = cameraObject.AddComponent<Camera>();
            camera.fieldOfView = 38f;
            camera.clearFlags = CameraClearFlags.SolidColor;
            camera.backgroundColor = new Color(0.09f, 0.11f, 0.14f, 1f);
            camera.nearClipPlane = 0.05f;
            camera.farClipPlane = 30f;
            cameraObject.AddComponent<AudioListener>();
        }

        private static void ConfigureBuildScene()
        {
            EditorBuildSettings.scenes = new[]
            {
                new EditorBuildSettingsScene(ScenePath, true)
            };

            PlayerSettings.companyName = "Maomaomao";
            PlayerSettings.productName = "Companion Cat";
            PlayerSettings.SetApplicationIdentifier(BuildTargetGroup.Android, "com.example.myapplication.unitycompanion");
        }

        private static string ToAbsolutePath(string unityRelativePath)
        {
            return Path.Combine(Directory.GetParent(Application.dataPath).FullName, unityRelativePath.Replace('/', Path.DirectorySeparatorChar));
        }
    }
}
