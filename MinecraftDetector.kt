package com.solauncher.bedrock

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.File
import java.util.zip.ZipFile

data class DetectedMinecraft(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val apkPath: String,
    val nativeLibDir: String,
    val hasArmeabiV7a: Boolean
)

object MinecraftDetector {

    fun findInstalledMinecraft(context: Context): List<DetectedMinecraft> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val results = mutableListOf<DetectedMinecraft>()

        for (pkg in packages) {
            if (pkg.packageName == context.packageName) continue
            val appInfo = pkg.applicationInfo ?: continue

            val isOfficial = pkg.packageName == "com.mojang.minecraftpe"
            val nameLooksLikeMc = pkg.packageName.contains("minecraft", ignoreCase = true)
            val libDir = appInfo.nativeLibraryDir ?: ""
            val hasLibInDir = File(libDir, "libminecraftpe.so").exists()

            if (!isOfficial && !nameLooksLikeMc && !hasLibInDir) continue

            val apkPath = appInfo.sourceDir ?: continue
            val hasArm32 = hasLibInDir || checkApkHasArm32Lib(apkPath)

            val label = pm.getApplicationLabel(appInfo).toString()
            val version = pkg.versionName ?: "unknown"

            results.add(
                DetectedMinecraft(
                    packageName = pkg.packageName,
                    appName = label,
                    versionName = version,
                    apkPath = apkPath,
                    nativeLibDir = libDir,
                    hasArmeabiV7a = hasArm32
                )
            )
        }

        return results.sortedByDescending { it.packageName == "com.mojang.minecraftpe" }
    }

    private fun checkApkHasArm32Lib(apkPath: String): Boolean {
        return try {
            ZipFile(apkPath).use { zip ->
                zip.getEntry("lib/armeabi-v7a/libminecraftpe.so") != null
            }
        } catch (_: Exception) {
            false
        }
    }
}
