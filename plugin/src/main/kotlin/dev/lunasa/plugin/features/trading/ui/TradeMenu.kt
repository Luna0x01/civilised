package dev.lunasa.plugin.features.trading.ui

import dev.lunasa.modules.ui.*
import dev.lunasa.modules.ui.utils.Text
import dev.lunasa.modules.ui.utils.ItemBuilder
import dev.lunasa.plugin.features.trading.TradingService
import dev.lunasa.plugin.features.trading.data.TradeSession
import dev.lunasa.plugin.features.trading.data.TradeStatus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

// TODO: Move to DSL
class TradeMenu(
    private val viewer: Player,
    private val session: TradeSession
) : UIMenu(Text.Trading.title(), 6) {
    private val otherPlayer get() = session.getOtherPlayer(viewer)
    private val viewerOffer get() = session.getPlayerOffer(viewer)
    private val otherOffer get() = session.getOtherPlayerOffer(viewer)

    override fun setupMenu() {
        createBorder(UIItems.border())

        buildPlayerSection()
        buildControlButtons()
        buildStatus()
        buildOfferDisplay()
    }

    private fun buildPlayerSection() {
        setButton(10, UIButton.create(
            ItemBuilder.create(Material.ENDER_CHEST)
                .displayName(Text.Trading.yourItems())
                .glow()
                .build()
        ))

        setButton(16, UIButton.create(
            ItemBuilder.create(Material.CHEST)
                .displayName(Text.Trading.theirItems(otherPlayer.displayName()))
                .glow()
                .build()
        ))

        for (i in arrayOf(13, 22, 31)) {
            setButton(i, UIButton.create(UIItems.filler()))
        }
    }

    private fun buildControlButtons() {
        val readyButton = if (viewerOffer.isReady) {
            ItemBuilder.create(Material.LIME_CONCRETE)
                .displayName(Text.Trading.ready())
                .lore(
                    Component.text("Click to mark as not ready").color(NamedTextColor.GRAY),
                    Component.text(""),
                    if (otherOffer.isReady) {
                        Component.text("✓ ${otherPlayer.name} is ready").color(NamedTextColor.GREEN)
                    } else {
                        Component.text("⏳ ${otherPlayer.name} is not ready").color(NamedTextColor.YELLOW)
                    }
                )
                .glow()
        } else {
            ItemBuilder.create(Material.RED_CONCRETE)
                .displayName(Text.Trading.notReady())
                .lore(
                    Component.text("Click to mark as ready").color(NamedTextColor.GRAY),
                    Component.text(""),
                    if (otherOffer.isReady) {
                        Component.text("✓ ${otherPlayer.name} is ready").color(NamedTextColor.GREEN)
                    } else {
                        Component.text("⏳ ${otherPlayer.name} is not ready").color(NamedTextColor.YELLOW)
                    }
                )
        }

        setButton(31, UIButton.create(readyButton.build()) { player, _ ->
            TradingService.toggleReady(player)
        })

        if (session.status == TradeStatus.READY || session.status == TradeStatus.CONFIRMING) {
            val confirmButton = if (viewerOffer.isConfirmed) {
                ItemBuilder.create(Material.EMERALD_BLOCK)
                    .displayName(Text.createGradient("✓ Confirmed", "#4CAF50", "#8BC34A"))
                    .lore(
                        Component.text("Waiting for ${otherPlayer.name} to confirm...").color(NamedTextColor.GRAY)
                    )
                    .glow()
            } else {
                ItemBuilder.create(Material.EMERALD)
                    .displayName(Text.Trading.confirm())
                    .lore(
                        Component.text("Click to confirm this trade").color(NamedTextColor.GRAY),
                        Component.text("⚠ This action cannot be undone.").color(NamedTextColor.RED).decorate(TextDecoration.ITALIC)
                    )
                    .glow()
            }

            setButton(40, UIButton.create(confirmButton.build()) { player, _ ->
                if (!viewerOffer.isConfirmed) {
                    TradingService.confirm(player)
                }
            })
        } else {
            setButton(40, UIButton.create(UIItems.filler()))
        }

        setButton(43, UIButton.create(
            ItemBuilder.create(Material.BARRIER)
                .displayName(Text.Trading.cancel())
                .lore(
                    Component.text("Click to cancel this trade").color(NamedTextColor.GRAY),
                    Component.text("All items will be returned").color(NamedTextColor.GRAY)
                )
                .build()
        ) { player, _ ->
            TradingService.cancel(player)
        })

        setButton(37, UIButton.create(
            ItemBuilder.create(Material.HOPPER)
                .displayName(Text.Trading.addItems())
                .lore(
                    Component.text("Click to open your inventory").color(NamedTextColor.GRAY),
                    Component.text("and select items to trade").color(NamedTextColor.GRAY)
                )
                .build()
        ) { player, _ ->
            openInventorySelector(player)
        })
    }

    private fun buildStatus() {
        val statusItem = when (session.status) {
            TradeStatus.ACTIVE -> ItemBuilder.create(Material.CLOCK)
                .displayName(Text.createGradient("Waiting for Offers", "#2196F3", "#03DAC6"))
                .lore(
                    Component.text("Players are setting up their offers").color(NamedTextColor.GRAY)
                )

            TradeStatus.READY -> ItemBuilder.create(Material.BELL)
                .displayName(Text.createGradient("Ready to Trade", "#FF9800", "#FFC107"))
                .lore(
                    Component.text("Both players are ready!").color(NamedTextColor.GREEN),
                    Component.text("Confirm to complete the trade").color(NamedTextColor.GRAY)
                )
                .glow()

            TradeStatus.CONFIRMING -> ItemBuilder.create(Material.FIRE_CHARGE)
                .displayName(Text.createGradient("Waiting for Confirmation", "#F44336", "#E91E63"))
                .lore(
                    Component.text("Waiting for final confirmations...").color(NamedTextColor.YELLOW)
                )
                .glow()

            else -> ItemBuilder.create(Material.FIRE_CHARGE)
                .displayName(Text.createGradient(session.status.toString(), "#F44336", "#E91E63"))
        }

        setButton(4, UIButton.create(statusItem.build()))
    }

    private fun buildOfferDisplay() {
        clearOfferSlots()

        val viewerSlots = arrayOf(19, 20, 21, 28, 29, 30)
        viewerOffer.items.take(6).forEachIndexed { index, item ->
            setButton(viewerSlots[index], UIButton.create(
                ItemBuilder.create(item.type)
                    .displayName(item.displayName())
                    .lore(
                        Component.text(""),
                        Component.text("Right-click to remove").color(NamedTextColor.RED)
                    )
                    .amount(item.amount)
                    .build()
            ) { player, clickType ->
                if (clickType == ClickType.RIGHT) {
                    TradingService.removeItem(player, index)
                }
            })
        }

        val otherSlots = arrayOf(23, 24, 25, 32, 33, 34)
        otherOffer.items.take(6).forEachIndexed { index, item ->
            setButton(otherSlots[index], UIButton.create(
                ItemBuilder.create(item.type)
                    .displayName(item.displayName())
                    .lore(
                        Component.text(""),
                        Component.text("${otherPlayer.name}'s item").color(NamedTextColor.BLUE)
                    )
                    .amount(item.amount)
                    .build()
            ))
        }
    }

    private fun clearOfferSlots() {
        val allOfferSlots = listOf(19, 20, 21, 23, 24, 25, 28, 29, 30, 32, 33, 34)
        allOfferSlots.forEach { slot ->
            setButton(slot, UIButton.create(ItemStack(Material.AIR)))
        }
    }

    private fun openInventorySelector(player: Player) {
        InventorySelectionMenu(player, session).open(player)
    }

    fun refresh() {
        buildControlButtons()
        buildOfferDisplay()
        buildStatus()
    }

    override fun handleClose(player: Player): Boolean {
        return session.status != TradeStatus.CONFIRMING
    }
}
