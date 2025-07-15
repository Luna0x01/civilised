package dev.lunasa.modules.commander.core

import dev.lunasa.modules.commander.CommandCategoryKey
import dev.lunasa.modules.commander.api.CommandExecutor
import dev.lunasa.modules.commander.api.CommandTabCompleter

class CommandBuilder(private val name: String, private val key: CommandCategoryKey) {
    private var description: String = ""
    private var usage: String = "/$name"
    private var aliases: List<String> = emptyList()
    private var permission: String? = null
    private var playerOnly: Boolean = false
    private val subCommands = mutableMapOf<String, CommanderBukkitImpl>()
    private var executor: CommandExecutor? = null
    private var tabCompleter: CommandTabCompleter? = null

    fun description(desc: String) {
        description = desc
    }

    fun usage(usage: String) {
        this.usage = usage
    }

    fun aliases(vararg aliases: String) {
        this.aliases = aliases.toList()
    }

    fun permission(permission: String) {
        this.permission = permission
    }

    fun playerOnly() {
        playerOnly = true
    }

    fun subCommand(name: String, block: CommandBuilder.() -> Unit) {
        val builder = CommandBuilder(name, key)
        builder.apply(block)
        subCommands[name.lowercase()] = builder.build()
    }

    fun executes(executor: CommandExecutor) {
        this.executor = executor
    }

    fun tabComplete(completer: CommandTabCompleter) {
        this.tabCompleter = completer
    }

    fun executes(block: CommandContext.() -> Boolean) {
        executor = CommandExecutor { sender, args ->
            val context = CommandContext(sender, args)
            block(context)
        }
    }

    internal fun build(): CommanderBukkitImpl {
        return CommanderBukkitImpl(
            name,
            description,
            usage,
            aliases,
            key,
            permission,
            playerOnly,
            subCommands,
            executor,
            tabCompleter
        )
    }
}