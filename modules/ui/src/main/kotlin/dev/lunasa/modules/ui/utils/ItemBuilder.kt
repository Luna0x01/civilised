package dev.lunasa.modules.ui.utils

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemBuilder {
    companion object {
        fun create(material: Material): ItemBuilder = ItemBuilder(material)
    }

    private val itemStack: ItemStack
    private val meta: ItemMeta

    constructor(material: Material) {
        this.itemStack = ItemStack(material)
        this.meta = itemStack.itemMeta ?: throw IllegalStateException("ItemStack#getItemMeta returned null")
    }

    fun displayName(component: Component): ItemBuilder {
        meta.displayName(component)
        return this
    }

    fun lore(vararg lines: Component): ItemBuilder {
        meta.lore(lines.toList())
        return this
    }

    fun amount(amount: Int): ItemBuilder {
        itemStack.amount = amount
        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): ItemBuilder {
        meta.addEnchant(enchantment, level, true)
        return this
    }

    fun glow(): ItemBuilder {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        return this
    }

    fun hideFlags(): ItemBuilder {
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = meta
        return itemStack
    }
}
