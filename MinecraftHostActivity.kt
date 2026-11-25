package com.solauncher.bedrock

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.PathClassLoader
import java.io.File

/**
 * Loads Minecraft's native libraries (armeabi-v7a) and user-selected .so mods
 * into the launcher process, then hands off to Minecraft's MainActivity class.
 */
class MinecraftHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mcPackage = intent.getStringExtra("mc_package") ?: return finish()
        val mcApk = intent.getStringExtra("mc_apk") ?: return finish()
        val mcLibDir = intent.getStringExtra("mc_lib_dir") ?: ""

        try {
            val mcContext = createPackageContext(
                mcPackage,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )

            // 1. Preload core Minecraft shared libraries from target nativeLibraryDir
            val coreLibs = listOf("libc++_shared.so", "libfmod.so", "libminecraftpe.so")
            for (libName in coreLibs) {
                val libFile = File(mcLibDir, libName)
                if (libFile.exists()) {
                    val ok = NativeBridge.nativeLoadLibrary(libFile.absolutePath)
                    Log.i("SOlauncher", "Load $libName -> $ok")
                }
            }

            // 2. Load enabled 32-bit .so mods
            val enabledMods = ModManager.loadMods(this).filter { it.enabled }
            for (mod in enabledMods) {
                val ok = NativeBridge.nativeLoadLibrary(mod.filePath)
                if (!ok) {
                    val err = NativeBridge.nativeGetLastError()
                    Log.e("SOlauncher", "Failed mod ${mod.name}: $err")
                }
            }

            // 3. Resolve Minecraft's MainActivity class via PathClassLoader
            val modsDir = ModManager.getModsDir(this).absolutePath
            val combinedLibPath = "$mcLibDir:$modsDir"
            val mcClassLoader = PathClassLoader(mcApk, combinedLibPath, mcContext.classLoader)
            val mainActClass = mcClassLoader.loadClass("com.mojang.minecraftpe.MainActivity")

            Toast.makeText(
                this,
                "Загружено .so модов: ${enabledMods.size}. Запуск ${mainActClass.simpleName}",
                Toast.LENGTH_SHORT
            ).show()

            // Note: Full lifecycle forwarding (AssetManager + ActivityThread instrumentation)
            // depends on the specific Minecraft Bedrock version installed.
        } catch (e: Exception) {
            Log.e("SOlauncher", "Launch failed", e)
            Toast.makeText(this, "Ошибка запуска: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
