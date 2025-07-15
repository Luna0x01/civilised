package dev.lunasa.modules.ui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@Suppress("unused")
abstract class UIMenu(
    title: Component,
    val rows: Int
) {
    val inventory: Inventory = Bukkit.createInventory(null, rows * 9, title)
    private val buttons = mutableMapOf<Int, UIButton>()

    // Don't call this from the ctor
    open fun setupMenu() {}

    fun setButton(slot: Int, button: UIButton) {
        buttons[slot] = button
        inventory.setItem(slot, button.item)
    }

    fun getButton(slot: Int): UIButton? {
        return buttons[slot]
    }

    fun handleClick(player: Player, slot: Int, clickType: ClickType) {
        val button = buttons[slot] ?: return
        button.onClick(player, clickType)
    }

    open fun handleClose(player: Player): Boolean {
        return true
    }

    fun open(player: Player) {
        this.setupMenu()
        player.openInventory(inventory)
        UIService.registerGui(player, this)
    }

    fun fillEmptySlots(item: ItemStack) {
        for (i in 0 until inventory.size) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item)
            }
        }
    }

    fun createBorder(item: ItemStack) {
        val size = inventory.size
        val rowSize = 9

        for (i in 0 until rowSize) {
            inventory.setItem(i, item)
            inventory.setItem(size - rowSize + i, item)
        }

        for (row in 1 until rows - 1) {
            inventory.setItem(row * rowSize, item)
            inventory.setItem(row * rowSize + rowSize - 1, item)
        }
    }
}