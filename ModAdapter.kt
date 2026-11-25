package com.solauncher.bedrock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solauncher.bedrock.databinding.ItemModBinding

class ModAdapter(
    private val mods: MutableList<SoMod>,
    private val onToggle: (SoMod, Boolean) -> Unit,
    private val onDelete: (SoMod) -> Unit
) : RecyclerView.Adapter<ModAdapter.ModViewHolder>() {

    inner class ModViewHolder(val binding: ItemModBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModViewHolder {
        val binding = ItemModBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModViewHolder, position: Int) {
        val mod = mods[position]
        holder.binding.tvModName.text = mod.name
        holder.binding.tvModArch.text = if (mod.isArm32Elf) {
            "armeabi-v7a (32-bit ARM)"
        } else {
            "Внимание: не 32-bit ARM ELF"
        }
        holder.binding.switchMod.setOnCheckedChangeListener(null)
        holder.binding.switchMod.isChecked = mod.enabled
        holder.binding.switchMod.setOnCheckedChangeListener { _, isChecked ->
            onToggle(mod, isChecked)
        }
        holder.binding.btnDeleteMod.setOnClickListener {
            onDelete(mod)
        }
    }

    override fun getItemCount(): Int = mods.size
}
