package dev.lunasa.modules.commander.core

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandContext(val sender: CommandSender, val args: Array<String>) {
    val isPlayer: Boolean
        get() = sender is Player

    val player: Player
        get() = sender as Player

    fun reply(message: Component) {
        sender.sendMessage(message)
    }

    fun getArg(index: Int): String? {
        return args.getOrNull(index)
    }

    fun getArg(index: Int, default: String): String {
        return args.getOrNull(index) ?: default
    }

    fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}