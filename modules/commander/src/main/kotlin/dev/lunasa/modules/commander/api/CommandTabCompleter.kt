package dev.lunasa.modules.commander.api

import org.bukkit.command.CommandSender

fun interface CommandTabCompleter {
    fun complete(sender: CommandSender, args: Array<String>): List<String>
}