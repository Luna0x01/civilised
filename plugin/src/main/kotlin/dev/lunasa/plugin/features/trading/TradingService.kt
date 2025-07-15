package dev.lunasa.plugin.features.trading

import dev.lunasa.modules.commander.CommandCategoryKey
import dev.lunasa.modules.commander.Commander
import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.modules.ui.UIService
import dev.lunasa.plugin.features.trading.data.*
import dev.lunasa.plugin.features.trading.ui.TradeMenu
import dev.lunasa.modules.ui.utils.Text
import dev.lunasa.plugin.Civilised
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
@Service
object TradingService {
    @Inject
    private lateinit var plugin: Civilised

    private val activeTrades = ConcurrentHashMap<UUID, TradeSession>()
    private val pendingRequests = ConcurrentHashMap<UUID, TradeRequest>()
    private val playerTrades = ConcurrentHashMap<UUID, UUID>()

    private val KEY = CommandCategoryKey("Trading", "Commands related to player-to-player trading.")

    @Configure
    fun configure() {
        registerCommands()
    }

    fun send(from: Player, to: Player): Boolean {
        if (isPlayerInTrade(from) || isPlayerInTrade(to)) {
            from.sendMessage(Component.text("One of you is already in a trade!").color(NamedTextColor.RED))
            return false
        }

        if (hasPendingRequest(to)) {
            from.sendMessage(Component.text("${to.name} already has a pending trade request!").color(NamedTextColor.RED))
            return false
        }

        val request = TradeRequest(from, to)
        pendingRequests[to.uniqueId] = request

        from.sendMessage(Text.createGradient("✉ Trade request sent to ${to.name}!", "#4CAF50", "#8BC34A"))

        val acceptMessage = Component.text()
            .append(Text.BAR)
            .append(Text.createGradient("📋 ${from.name} wants to trade with you! ", "#FF6B35", "#F7931E"))
            .append(Component.newline())
            .append(Component.text("[ACCEPT]").color(NamedTextColor.GREEN).clickEvent(
                ClickEvent.runCommand("/trade accept ${from.name}")
            ).hoverEvent(HoverEvent.showText(Component.text("Accept this trade.").color(NamedTextColor.GRAY))))
            .append(Component.text(" "))
            .append(Component.text("[DECLINE]").color(NamedTextColor.RED).clickEvent(
                ClickEvent.runCommand("/trade decline ${from.name}")
            )
            ).hoverEvent(HoverEvent.showText(Component.text("Decline this trade.").color(NamedTextColor.GRAY)))
            .append(Text.BAR)
            .build()

        to.sendMessage(acceptMessage)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            expire(to)
        }, 600L) // 30s

        return true
    }

    fun accept(player: Player, fromPlayerName: String): Boolean {
        val request = pendingRequests[player.uniqueId] ?: return false

        if (request.from.name != fromPlayerName) {
            player.sendMessage(Component.text("No trade request from $fromPlayerName found!").color(NamedTextColor.RED))
            return false
        }

        if (request.hasExpired()) {
            pendingRequests.remove(player.uniqueId)
            player.sendMessage(Component.text("Trade request has expired!").color(NamedTextColor.RED))
            return false
        }

        pendingRequests.remove(player.uniqueId)
        return initiate(request.from, player)
    }

    fun decline(player: Player, fromPlayerName: String): Boolean {
        val request = pendingRequests[player.uniqueId] ?: return false

        if (request.from.name != fromPlayerName) {
            player.sendMessage(Component.text("No trade request from $fromPlayerName found!").color(NamedTextColor.RED))
            return false
        }

        pendingRequests.remove(player.uniqueId)

        player.sendMessage(Text.createGradient("✗ Trade request declined", "#F44336", "#E91E63"))
        request.from.sendMessage(Text.createGradient("✗ ${player.name} declined your trade request", "#F44336", "#E91E63"))

        return true
    }

    private fun initiate(initiator: Player, target: Player): Boolean {
        if (isPlayerInTrade(initiator) || isPlayerInTrade(target)) {
            return false
        }

        val session = TradeSession(
            initiator = initiator,
            target = target,
            status = TradeStatus.ACTIVE
        )

        activeTrades[session.id] = session
        playerTrades[initiator.uniqueId] = session.id
        playerTrades[target.uniqueId] = session.id

        openTradeMenu(initiator, session)
        openTradeMenu(target, session)

        initiator.sendMessage(Text.createGradient("🎉 Trade started with ${target.name}!", "#4CAF50", "#8BC34A"))
        target.sendMessage(Text.createGradient("🎉 Trade started with ${initiator.name}!", "#4CAF50", "#8BC34A"))

        return true
    }

    fun addItem(player: Player, item: ItemStack): Boolean {
        val session = getPlayerTrade(player) ?: return false
        val offer = session.getPlayerOffer(player)

        if (offer.items.size >= 27) {
            player.sendMessage(Component.text("Your trade offer is full!").color(NamedTextColor.RED))
            return false
        }

        offer.add(item)
        offer.isReady = false
        session.getOtherPlayerOffer(player).isReady = false

        update(session)

        return true
    }

    fun removeItem(player: Player, index: Int): Boolean {
        val session = getPlayerTrade(player) ?: return false
        val offer = session.getPlayerOffer(player)

        val removedItem = offer.remove(index) ?: return false

        player.inventory.addItem(removedItem)

        offer.isReady = false
        session.getOtherPlayerOffer(player).isReady = false

        update(session)

        return true
    }

    fun toggleReady(player: Player): Boolean {
        val session = getPlayerTrade(player) ?: return false
        val offer = session.getPlayerOffer(player)

        if (offer.isEmpty) {
            player.sendMessage(Component.text("You must add items to your offer first!").color(NamedTextColor.RED))
            return false
        }

        offer.isReady = !offer.isReady

        if (session.isReady) {
            session.status = TradeStatus.READY
        } else {
            session.status = TradeStatus.ACTIVE
        }

        update(session)

        return true
    }

    fun confirm(player: Player): Boolean {
        val session = getPlayerTrade(player) ?: return false

        val offer = session.getPlayerOffer(player)
        offer.isConfirmed = true

        if (session.isConfirmed) {
            return execute(session)
        } else {
            session.status = TradeStatus.CONFIRMING
            update(session)
        }

        return true
    }

    private fun execute(session: TradeSession): Boolean {
        val initiator = session.initiator
        val target = session.target

        if (!validate(session)) {
            cancel(session, "Inventory validation failed for this trade! Ensure both players have enough space in their inventories.")
            return false
        }

        val initiatorItems = session.initiatorOffer.items.toList()
        val targetItems = session.targetOffer.items.toList()

        targetItems.forEach { initiator.inventory.addItem(it) }
        initiatorItems.forEach { target.inventory.addItem(it) }

        session.status = TradeStatus.COMPLETED
        end(session)

        initiator.sendMessage(Text.createGradient("✓ Trade completed.", "#4CAF50", "#8BC34A"))
        target.sendMessage(Text.createGradient("✓ Trade completed.", "#4CAF50", "#8BC34A"))

        return true
    }

    fun cancel(player: Player): Boolean {
        val session = getPlayerTrade(player) ?: return false
        return cancel(session, "${player.name} cancelled the trade")
    }

    private fun cancel(session: TradeSession, reason: String): Boolean {
        session.status = TradeStatus.CANCELLED

        revert(session.initiator, session.initiatorOffer.items)
        revert(session.target, session.targetOffer.items)

        session.initiator.sendMessage(Text.createGradient("✗ $reason", "#F44336", "#E91E63"))
        session.target.sendMessage(Text.createGradient("✗ $reason", "#F44336", "#E91E63"))

        end(session)
        return true
    }

    private fun end(session: TradeSession) {
        session.initiator.closeInventory()
        session.target.closeInventory()

        activeTrades.remove(session.id)

        playerTrades.remove(session.initiator.uniqueId)
        playerTrades.remove(session.target.uniqueId)
    }

    private fun validate(session: TradeSession): Boolean {
        val initiator = session.initiator
        val target = session.target

        val initiatorSpaceNeeded = session.targetOffer.items.size
        val targetSpaceNeeded = session.initiatorOffer.items.size

        val initiatorFreeSlots = initiator.inventory.storageContents.count { it == null }
        val targetFreeSlots = target.inventory.storageContents.count { it == null }

        return initiatorFreeSlots >= initiatorSpaceNeeded && targetFreeSlots >= targetSpaceNeeded
    }

    private fun revert(player: Player, items: List<ItemStack>) {
        items.forEach { item ->
            val remaining = player.inventory.addItem(item)
            remaining.values.forEach { leftover ->
                player.world.dropItemNaturally(player.location, leftover)
            }
        }
    }

    private fun openTradeMenu(player: Player, session: TradeSession) {
        TradeMenu(player, session).open(player)
    }

    private fun update(session: TradeSession) {
        val initiatorMenu = UIService.get(session.initiator) as? TradeMenu
        val targetMenu = UIService.get(session.target) as? TradeMenu

        initiatorMenu?.refresh()
        targetMenu?.refresh()
    }

    private fun expire(player: Player) {
        val request = pendingRequests.remove(player.uniqueId) ?: return

        if (!request.hasExpired()) {
            request.isExpired = true

            player.sendMessage(Component.text("⌛ Trade request expired!").color(NamedTextColor.GRAY))
            request.from.sendMessage(Component.text("⌛ Trade request to ${player.name} expired!").color(NamedTextColor.GRAY))
        }
    }

    fun isPlayerInTrade(player: Player) = playerTrades.containsKey(player.uniqueId)
    fun hasPendingRequest(player: Player) = pendingRequests.containsKey(player.uniqueId)

    fun getPlayerTrade(player: Player): TradeSession? {
        val sessionId = playerTrades[player.uniqueId] ?: return null
        return activeTrades[sessionId]
    }

    // TODO: Move this somewhere else?
    private fun registerCommands() {
        Commander.register("trade", KEY) {
            description("Trade items with other players")
            usage("/trade <player> | /trade <accept|decline|cancel> [player]")
            playerOnly()

            executes {
                if (args.isEmpty()) {
                    reply(Component.text("Usage: /trade <player>").color(NamedTextColor.RED))
                    return@executes true
                }

                val targetName = args[0]
                val target = Bukkit.getPlayer(targetName)

                if (target == null) {
                    reply(Component.text("Player '$targetName' not found!").color(NamedTextColor.RED))
                    return@executes true
                }

                if (target == player) {
                    reply(Component.text("You cannot trade with yourself!").color(NamedTextColor.RED))
                    return@executes true
                }

                send(player, target)
                true
            }

            subCommand("accept") {
                description("Accept a trade request")
                usage("/trade accept <player>")
                playerOnly()

                executes {
                    if (args.isEmpty()) {
                        reply(Component.text("Usage: /trade accept <player>").color(NamedTextColor.RED))
                        return@executes true
                    }

                    val fromPlayerName = args[0]
                    accept(player, fromPlayerName)
                    true
                }

                tabComplete { sender, args ->
                    if (args.size == 1 && sender is Player) {
                        val request = pendingRequests[sender.uniqueId]
                        if (request != null) {
                            listOf(request.from.name)
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            subCommand("decline") {
                description("Decline a trade request")
                usage("/trade decline <player>")
                playerOnly()

                executes {
                    if (args.isEmpty()) {
                        reply(Component.text("Usage: /trade decline <player>").color(NamedTextColor.RED))
                        return@executes true
                    }

                    val fromPlayerName = args[0]
                    decline(player, fromPlayerName)
                    true
                }

                tabComplete { sender, args ->
                    if (args.size == 1 && sender is Player) {
                        val request = pendingRequests[sender.uniqueId]
                        if (request != null) {
                            listOf(request.from.name)
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            subCommand("cancel") {
                description("Cancel your current trade")
                usage("/trade cancel")
                playerOnly()

                executes {
                    if (!isPlayerInTrade(player)) {
                        reply(Component.text("You are not currently in a trade!").color(NamedTextColor.RED))
                        return@executes true
                    }

                    cancel(player)
                    true
                }
            }

            tabComplete { sender, args ->
                if (args.size == 1) {
                    val online = Bukkit.getOnlinePlayers()
                        .filter { it != sender }
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[0].lowercase()) }

                    val subCommands = listOf("accept", "decline", "cancel")
                        .filter { it.lowercase().startsWith(args[0].lowercase()) }

                    online + subCommands
                } else {
                    emptyList()
                }
            }
        }
    }
}