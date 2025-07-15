package dev.lunasa.plugin.features.common

import dev.lunasa.modules.configuration.OptionStorage
import org.bukkit.plugin.java.JavaPlugin

class CommonConfiguration(plugin: JavaPlugin) : OptionStorage(plugin, "config") {
    val disableDeathMessages by property("disable-death-messages", true)
}