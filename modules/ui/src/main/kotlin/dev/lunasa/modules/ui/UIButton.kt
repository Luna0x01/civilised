package dev.lunasa.modules.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

open class UIButton(
    val item: ItemStack,
    private val clickHandler: ((Player, ClickType) -> Unit)? = null
) {

    fun onClick(player: Player, clickType: ClickType) {
        clickHandler?.invoke(player, clickType)
    }

    companion object {
        fun create(
            item: ItemStack,
            onClick: ((Player, ClickType) -> Unit)? = null
        ): UIButton {
            return UIButton(item, onClick)
        }

        fun empty(): UIButton {
            return UIButton(ItemStack(org.bukkit.Material.AIR))
        }
    }
}
