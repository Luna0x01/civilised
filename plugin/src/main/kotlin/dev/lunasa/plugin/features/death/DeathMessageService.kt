package dev.lunasa.plugin.features.death

import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.plugin.Civilised
import dev.lunasa.plugin.features.common.CommonService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

@Suppress("unused")
@Service
class DeathMessageService : Listener {
    @Inject
    private lateinit var plugin: Civilised

    @Configure
    fun configure() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onDeathMessage(event: PlayerDeathEvent) {
        if (CommonService.configuration.disableDeathMessages) {
            event.deathMessage(null)
        }
    }
}