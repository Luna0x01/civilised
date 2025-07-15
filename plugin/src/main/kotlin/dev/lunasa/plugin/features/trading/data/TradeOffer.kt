package dev.lunasa.plugin.features.trading.data

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class TradeOffer(
    val player: Player,
    val items: MutableList<ItemStack> = mutableListOf(),
    var isReady: Boolean = false,
    var isConfirmed: Boolean = false
) {
    val isEmpty get() = items.isEmpty()

    fun add(item: ItemStack) {
        items.add(item.clone())
    }

    fun remove(index: Int): ItemStack? {
        return if (index in items.indices) {
            items.removeAt(index)
        } else null
    }
}