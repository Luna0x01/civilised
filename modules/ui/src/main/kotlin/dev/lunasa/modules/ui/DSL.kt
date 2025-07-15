package dev.lunasa.modules.ui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

@DslMarker
annotation class MenuDsl

@MenuDsl
class MenuBuilder(private val title: Component, private val rows: Int) {
    private val buttons = mutableMapOf<Int, UIButton>()

    fun button(slot: Int, material: Material, block: ButtonBuilder.() -> Unit) {
        val builder = ButtonBuilder(material)
        builder.block()
        buttons[slot] = builder.build()
    }

    fun button(slot: Int, itemStack: ItemStack, block: ButtonBuilder.() -> Unit) {
        val builder = ButtonBuilder(itemStack)
        builder.block()
        buttons[slot] = builder.build()
    }

    fun fillBorder(material: Material) {
        val borderSlots = getBorderSlots(rows)
        borderSlots.forEach { slot ->
            if (!buttons.containsKey(slot)) {
                button(slot, material) { }
            }
        }
    }

    fun fillEmpty(material: Material) {
        for (i in 0 until rows * 9) {
            if (!buttons.containsKey(i)) {
                button(i, material) { }
            }
        }
    }

    private fun getBorderSlots(rows: Int): List<Int> {
        val slots = mutableListOf<Int>()
        val size = rows * 9

        // top and bottom rows
        for (i in 0..8) slots.add(i)
        for (i in (size - 9) until size) slots.add(i)

        // left and right columns (excluding corners already added)
        for (row in 1 until rows - 1) {
            slots.add(row * 9)
            slots.add(row * 9 + 8)
        }

        return slots
    }

    internal fun build(): UIMenu {
        return object : UIMenu(title, rows) {
            init {
                buttons.forEach { (slot, button) ->
                    setButton(slot, button)
                }
            }
        }
    }
}

@MenuDsl
class ButtonBuilder {
    private var itemStack: ItemStack
    private var clickHandler: ((Player, ClickType) -> Unit)? = null

    constructor(material: Material) {
        this.itemStack = ItemStack(material)
    }

    constructor(itemStack: ItemStack) {
        this.itemStack = itemStack.clone()
    }

    fun displayName(component: Component) {
        val meta = itemStack.itemMeta
        meta?.displayName(component)
        itemStack.itemMeta = meta
    }

    fun lore(vararg lines: Component) {
        val meta = itemStack.itemMeta
        meta?.lore(lines.toList())
        itemStack.itemMeta = meta
    }

    fun amount(amount: Int) {
        itemStack.amount = amount
    }

    fun onClick(handler: (Player, ClickType) -> Unit) {
        this.clickHandler = handler
    }

    internal fun build(): UIButton {
        return UIButton(itemStack, clickHandler)
    }
}

fun menu(title: Component, rows: Int, block: MenuBuilder.() -> Unit): UIMenu {
    val builder = MenuBuilder(title, rows)
    builder.block()
    return builder.build()
}