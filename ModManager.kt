package com.solauncher.bedrock

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

data class SoMod(
    val id: String,
    val name: String,
    val filePath: String,
    var enabled: Boolean,
    val isArm32Elf: Boolean
)

object ModManager {
    private const val PREFS_NAME = "solauncher_mods"
    private const val KEY_MODS = "mods_json"

    fun getModsDir(context: Context): File {
        return File(context.filesDir, "so_mods").apply { mkdirs() }
    }

    fun loadMods(context: Context): MutableList<SoMod> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MODS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<SoMod>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val path = obj.getString("filePath")
            if (File(path).exists()) {
                list.add(
                    SoMod(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        filePath = path,
                        enabled = obj.getBoolean("enabled"),
                        isArm32Elf = obj.optBoolean("isArm32Elf", true)
                    )
                )
            }
        }
        return list
    }

    fun saveMods(context: Context, mods: List<SoMod>) {
        val arr = JSONArray()
        for (mod in mods) {
            arr.put(
                JSONObject().apply {
                    put("id", mod.id)
                    put("name", mod.name)
                    put("filePath", mod.filePath)
                    put("enabled", mod.enabled)
                    put("isArm32Elf", mod.isArm32Elf)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODS, arr.toString())
            .apply()
    }

    fun importModFromUri(context: Context, uri: Uri): SoMod? {
        val fileName = resolveFileName(context, uri) ?: "mod_${System.currentTimeMillis()}.so"
        val safeName = if (fileName.endsWith(".so", ignoreCase = true)) fileName else "$fileName.so"
        val destFile = File(getModsDir(context), safeName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null

        val isArm32 = isArm32ElfFile(destFile)
        val mods = loadMods(context)
        val existingIndex = mods.indexOfFirst { it.filePath == destFile.absolutePath }
        val mod = SoMod(
            id = destFile.nameWithoutExtension + "_" + System.currentTimeMillis(),
            name = safeName,
            filePath = destFile.absolutePath,
            enabled = isArm32,
            isArm32Elf = isArm32
        )
        if (existingIndex >= 0) {
            mods[existingIndex] = mod
        } else {
            mods.add(mod)
        }
        saveMods(context, mods)
        return mod
    }

    private fun resolveFileName(context: Context, uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                return cursor.getString(idx)
            }
        }
        return uri.lastPathSegment
    }

    /**
     * Checks ELF header: 0x7F 'E' 'L' 'F', class = 1 (32-bit), e_machine = 40 (EM_ARM).
     */
    private fun isArm32ElfFile(file: File): Boolean {
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(20)
                if (fis.read(header) < 20) return false
                val isElf = header[0] == 0x7F.toByte() &&
                    header[1] == 'E'.code.toByte() &&
                    header[2] == 'L'.code.toByte() &&
                    header[3] == 'F'.code.toByte()
                val is32Bit = header[4] == 1.toByte()
                val machine = (header[18].toInt() and 0xFF) or ((header[19].toInt() and 0xFF) shl 8)
                isElf && is32Bit && machine == 40
            }
        } catch (_: Exception) {
            false
        }
    }
}
