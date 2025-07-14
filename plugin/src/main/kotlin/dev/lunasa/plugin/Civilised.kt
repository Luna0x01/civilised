package dev.lunasa.plugin

import dev.lunasa.modules.infuse.Infuse
import org.bukkit.plugin.java.JavaPlugin

class Civilised : JavaPlugin() {
    val infuse = Infuse.create<Civilised>()

    override fun onEnable() {
        infuse.bind<Civilised>().to(this)
        infuse.bind<JavaPlugin>().to(this)

        infuse.startup()
    }

    override fun onDisable() {
        infuse.close()
    }
}
