package dev.lunasa.modules.commander

import dev.lunasa.modules.commander.core.CommandBuilder
import dev.lunasa.modules.commander.core.CommanderBukkitImpl
import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import org.bukkit.command.CommandMap
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.properties.Delegates

@Service
object Commander {
    @Inject
    private lateinit var plugin: JavaPlugin

    private val commands = mutableMapOf<String, CommanderBukkitImpl>()
    private var commandMap: CommandMap by Delegates.notNull()

    @Configure
    fun configure() {
        val serverField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
        serverField.isAccessible = true
        commandMap = serverField.get(Bukkit.getServer()) as CommandMap
    }

    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val builder = CommandBuilder(name)
        builder.apply(block)
        val command = builder.build()

        commandMap.register(plugin.name.lowercase(), command)

        commands[name.lowercase()] = command
    }

    operator fun get(name: String): CommanderBukkitImpl? {
        return commands[name.lowercase()]
    }
}