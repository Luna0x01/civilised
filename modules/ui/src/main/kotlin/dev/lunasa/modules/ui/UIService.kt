package dev.lunasa.modules.ui

import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
@Service
object UIService : Listener {
    private val activeGuis = ConcurrentHashMap<UUID, UIMenu>()

    @Inject
    private lateinit var plugin: JavaPlugin

    @Configure
    fun configure() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun registerGui(player: Player, menu: UIMenu) {
        activeGuis[player.uniqueId] = menu
    }

    fun unregisterGui(player: Player) {
        activeGuis.remove(player.uniqueId)
    }

    fun get(player: Player): UIMenu? {
        return activeGuis[player.uniqueId]
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val gui = activeGuis[player.uniqueId] ?: return

        if (event.inventory != gui.inventory) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot >= 0 && slot < gui.inventory.size) {
            gui.handleClick(player, slot, event.click)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val gui = activeGuis[player.uniqueId] ?: return

        if (event.inventory != gui.inventory) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val gui = activeGuis[player.uniqueId] ?: return

        if (event.inventory != gui.inventory) return

        if (gui.handleClose(player)) {
            unregisterGui(player)
        } else {
            Bukkit.getScheduler().runTask(plugin) { task ->
                gui.setupMenu()
                player.openInventory(gui.inventory)
            }
        }
    }
}