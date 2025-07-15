package dev.lunasa.modules.ui

import dev.lunasa.modules.ui.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object UIItems {
    fun filler(): ItemStack {
        return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
            .displayName(Component.text(""))
            .build()
    }

    fun border(): ItemStack {
        return ItemBuilder.create(Material.BLACK_STAINED_GLASS_PANE)
            .displayName(Component.text(""))
            .build()
    }

    fun back(): ItemStack {
        return ItemBuilder.create(Material.ARROW)
            .displayName(Component.text("← Back"))
            .build()
    }

    fun confirm(): ItemStack {
        return ItemBuilder.create(Material.LIME_CONCRETE)
            .displayName(Component.text("✓ Confirm"))
            .build()
    }

    fun cancel(): ItemStack {
        return ItemBuilder.create(Material.RED_CONCRETE)
            .displayName(Component.text("✗ Cancel"))
            .build()
    }
}
