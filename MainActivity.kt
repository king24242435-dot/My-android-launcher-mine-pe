package com.solauncher.bedrock

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.solauncher.bedrock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modAdapter: ModAdapter
    private val mods = mutableListOf<SoMod>()
    private var selectedMc: DetectedMinecraft? = null

    private val pickSoLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val imported = ModManager.importModFromUri(this, uri)
            if (imported != null) {
                refreshMods()
                Toast.makeText(this, "Добавлен мод: ${imported.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось импортировать .so файл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        refreshMods()
        detectMinecraft()

        binding.btnAddMod.setOnClickListener {
            pickSoLauncher.launch(arrayOf("*/*"))
        }

        binding.btnRefreshMc.setOnClickListener {
            detectMinecraft()
        }

        binding.btnLaunch.setOnClickListener {
            val mc = selectedMc
            if (mc == null) {
                Toast.makeText(this, "Minecraft Bedrock не найден на устройстве", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, MinecraftHostActivity::class.java).apply {
                putExtra("mc_package", mc.packageName)
                putExtra("mc_apk", mc.apkPath)
                putExtra("mc_lib_dir", mc.nativeLibDir)
            }
            startActivity(intent)
        }
    }

    private fun setupRecycler() {
        modAdapter = ModAdapter(
            mods = mods,
            onToggle = { mod, enabled ->
                mod.enabled = enabled
                ModManager.saveMods(this, mods)
            },
            onDelete = { mod ->
                java.io.File(mod.filePath).delete()
                mods.remove(mod)
                ModManager.saveMods(this, mods)
                modAdapter.notifyDataSetChanged()
            }
        )
        binding.rvMods.layoutManager = LinearLayoutManager(this)
        binding.rvMods.adapter = modAdapter
    }

    private fun refreshMods() {
        mods.clear()
        mods.addAll(ModManager.loadMods(this))
        modAdapter.notifyDataSetChanged()
    }

    private fun detectMinecraft() {
        val found = MinecraftDetector.findInstalledMinecraft(this)
        selectedMc = found.firstOrNull()
        val mc = selectedMc
        if (mc != null) {
            binding.tvMcStatus.text = "Обнаружен: ${mc.appName} (v${mc.versionName})"
            binding.tvMcDetails.text = "Пакет: ${mc.packageName} • " +
                if (mc.hasArmeabiV7a) "32-bit armeabi-v7a готов" else "Нет armeabi-v7a библиотек"
            binding.btnLaunch.isEnabled = mc.hasArmeabiV7a
        } else {
            binding.tvMcStatus.text = "Minecraft Bedrock не найден"
            binding.tvMcDetails.text = "Установите 32-битную версию Minecraft и нажмите обновить"
            binding.btnLaunch.isEnabled = false
        }
    }
}
