package dev.lunasa.modules.commander.api

import org.bukkit.command.CommandSender

fun interface CommandExecutor {
    fun execute(sender: CommandSender, args: Array<String>): Boolean
}