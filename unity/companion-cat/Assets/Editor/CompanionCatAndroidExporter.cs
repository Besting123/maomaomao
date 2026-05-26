using System;
using System.IO;
using System.Linq;
using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace Maomaomao.CompanionCat.EditorTools
{
    public static class CompanionCatAndroidExporter
    {
        private const string ExportRootPath = @"G:\maomaomao\unity-export";

        public static void ExportAndroidGradleProject()
        {
            if (!BuildPipeline.IsBuildTargetSupported(BuildTargetGroup.Android, BuildTarget.Android))
            {
                throw new InvalidOperationException(
                    "Android Build Support is not installed for the current Tuanjie/Unity editor. " +
                    "Install the Android playback module before exporting to unity-export.");
            }

            string[] enabledScenes = EditorBuildSettings.scenes
                .Where(scene => scene.enabled)
                .Select(scene => scene.path)
                .ToArray();

            if (enabledScenes.Length == 0)
            {
                throw new InvalidOperationException("No enabled Unity scenes were found in EditorBuildSettings.");
            }

            EnsureExportDirectoryReady();

            EditorUserBuildSettings.exportAsGoogleAndroidProject = true;
            EditorUserBuildSettings.androidBuildSystem = AndroidBuildSystem.Gradle;

            BuildPlayerOptions buildPlayerOptions = new BuildPlayerOptions
            {
                scenes = enabledScenes,
                locationPathName = ExportRootPath,
                target = BuildTarget.Android,
                options = BuildOptions.None
            };

            Debug.Log($"Starting Unity Android Gradle export to: {ExportRootPath}");
            BuildReport report = BuildPipeline.BuildPlayer(buildPlayerOptions);
            BuildSummary summary = report.summary;

            if (summary.result != BuildResult.Succeeded)
            {
                throw new InvalidOperationException(
                    $"Unity Android Gradle export failed. Result={summary.result}, Errors={summary.totalErrors}, Warnings={summary.totalWarnings}");
            }

            Debug.Log($"Unity Android Gradle export completed successfully: {ExportRootPath}");
        }

        private static void EnsureExportDirectoryReady()
        {
            if (!Directory.Exists(ExportRootPath))
            {
                Directory.CreateDirectory(ExportRootPath);
                return;
            }

            bool hasEntries = Directory.EnumerateFileSystemEntries(ExportRootPath).Any();
            if (hasEntries)
            {
                throw new InvalidOperationException(
                    $"Export directory already contains files: {ExportRootPath}. Clear it before running ExportAndroidGradleProject again.");
            }
        }
    }
}
