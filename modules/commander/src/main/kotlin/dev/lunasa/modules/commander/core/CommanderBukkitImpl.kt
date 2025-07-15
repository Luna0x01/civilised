package dev.lunasa.modules.commander.core

import dev.lunasa.modules.commander.api.CommandExecutor
import dev.lunasa.modules.commander.api.CommandTabCompleter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommanderBukkitImpl(
    name: String,
    cmdDescription: String,
    cmdUsage: String,
    aliases: List<String>,
    private val cmdPermission: String?,
    private val playerOnly: Boolean,
    private val subCommands: Map<String, CommanderBukkitImpl>,
    private val executor: CommandExecutor?,
    private val tabCompleter: CommandTabCompleter?
) : Command(name) {
    init {
        description = cmdDescription
        usage = cmdUsage
        this.aliases = aliases
        permission = cmdPermission
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (playerOnly && sender !is Player) {
            sender.sendMessage(Component.text {
                it.append(
                    Component.text("This command can only be used by players.")
                        .color(NamedTextColor.RED),
                    Component.text("If you believe that this is a mistake, please contact one of our administrators.")
                        .color(NamedTextColor.GRAY)
                )
            })
            return true
        }

        if (cmdPermission != null && !sender.hasPermission(cmdPermission)) {
            sender.sendMessage(Component.text {
                it.append(
                    Component.text("You do not have permissions to use this command.")
                        .color(NamedTextColor.RED),
                    Component.text("If you believe that this is a mistake, please contact one of our administrators.")
                        .color(NamedTextColor.GRAY)
                )
            })
            return true
        }

        if (args.isNotEmpty() && subCommands.containsKey(args[0].lowercase())) {
            val subCommand = subCommands[args[0].lowercase()]
            val subArgs = args.copyOfRange(1, args.size)
            return subCommand?.execute(sender, args[0], subArgs) ?: false
        }

        return executor?.execute(sender, args) ?: false
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            val prefix = args[0].lowercase()
            return subCommands.keys
                .filter { it.startsWith(prefix) }
                .toList()
        }

        if (args.size > 1 && subCommands.containsKey(args[0].lowercase())) {
            val subCommand = subCommands[args[0].lowercase()]
            val subArgs = args.copyOfRange(1, args.size)
            return subCommand?.tabComplete(sender, args[0], subArgs) ?: emptyList()
        }

        return tabCompleter?.complete(sender, args) ?: super.tabComplete(sender, alias, args)
    }
}