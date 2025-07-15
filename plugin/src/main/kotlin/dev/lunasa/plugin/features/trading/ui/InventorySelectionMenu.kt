package dev.lunasa.plugin.features.trading.ui

import dev.lunasa.modules.ui.*
import dev.lunasa.modules.ui.utils.ItemBuilder
import dev.lunasa.plugin.features.trading.TradingService
import dev.lunasa.plugin.features.trading.data.TradeSession
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player

class InventorySelectionMenu(
    private val player: Player,
    private val session: TradeSession
) : UIMenu(Component.text("Add Items to Trade").color(NamedTextColor.DARK_PURPLE), 6) {
    override fun setupMenu() {
        createBorder(UIItems.border())

        buildControlButtons()
        buildInventorySlots()
    }

    private fun buildControlButtons() {
        setButton(0, UIButton.create(UIItems.back()) { _, _ ->
            TradeMenu(player, session).open(player)
        })
    }

    private fun buildInventorySlots() {
        val inventory = player.inventory.storageContents
        var itemIndex = 0

        outer@ for (row in 1..4) {
            for (col in 1..7) {
                if (itemIndex >= inventory.size) break@outer

                val item = inventory[itemIndex++]
                if (item != null && item.type != Material.AIR) {
                    val slot = row * 9 + col // translate to slot index

                    setButton(slot, UIButton.create(
                        ItemBuilder.create(item.type)
                            .displayName(item.effectiveName())
                            .lore(
                                Component.text(""),
                                Component.text("Click to add to trade").color(NamedTextColor.GREEN)
                            )
                            .amount(item.amount)
                            .build()
                    ) { p, _ ->
                        if (TradingService.addItem(p, item)) {
                            item.amount = 0
                            p.sendMessage(Component.text("Item added to trade!").color(NamedTextColor.GREEN))
                            TradeMenu(p, session).open(p)
                        }
                    })
                }
            }
        }
    }
}
