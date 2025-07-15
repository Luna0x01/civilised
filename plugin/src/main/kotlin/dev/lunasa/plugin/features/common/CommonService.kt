package dev.lunasa.plugin.features.common

import dev.lunasa.modules.commander.CommandCategoryKey
import dev.lunasa.modules.commander.Commander
import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.modules.ui.utils.Text
import dev.lunasa.modules.ui.utils.Text.buildComponent
import dev.lunasa.plugin.Civilised
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

@Suppress("unused")
@Service
object CommonService {
    @Inject
    private lateinit var plugin: Civilised

    val configuration by lazy {
        CommonConfiguration(plugin)
    }

    @Configure
    fun configure() {
        configuration.load()

        this.registerCommands()
    }

    private fun registerCommands() {
        Commander.register(
            "civilised",
            CommandCategoryKey("Common", "Commands related to the plugins's common features"),
        ) {
            subCommand("help") {
                description("Show a help menu.")
                usage("/help")

                executes {
                    reply(buildHelpMenu())

                    true
                }
            }
        }
    }

    private fun buildHelpMenu() = buildComponent {
        append(Text.BAR)
        append(Component.newline())
        append(Text.createColor("Civilised | Help", "aqua"))
        append(Component.newline())
        append(Component.newline())

        val categorized = Commander.commands.values.groupBy { it.key }

        for ((category, commandList) in categorized) {
            append(Text.createColor("• ${category.name}", "aqua"))
            append(Component.text(" - ${category.description}").color(NamedTextColor.GRAY))
            append(Component.newline())

            for (command in commandList) {
                append(Component.text("  ⮞ /${command.name}").color(NamedTextColor.YELLOW))
                if (command.description.isNotBlank()) {
                    append(Component.text(" - ${command.description}").color(NamedTextColor.GRAY))
                }
                append(Component.newline())

                // sub-commands
                if (command.subCommands.isNotEmpty()) {
                    for ((subName, subCommand) in command.subCommands) {
                        append(
                            Component.text("     ↳ /${command.name} $subName")
                                .color(NamedTextColor.GOLD)
                                .append(
                                    Component.text(" - ${subCommand.description}")
                                        .color(NamedTextColor.GRAY)
                                )
                        )
                        append(Component.newline())
                        if (subCommand.usage.isNotBlank()) {
                            append(
                                Component.text("        Usage: ")
                                    .color(NamedTextColor.DARK_GRAY)
                                    .append(Component.text(subCommand.usage).color(NamedTextColor.GRAY))
                            )
                            append(Component.newline())
                        }
                    }
                }

                if (command.usage.isNotBlank()) {
                    append(
                        Component.text("     Usage: ")
                            .color(NamedTextColor.DARK_GRAY)
                            .append(Component.text(command.usage).color(NamedTextColor.GRAY))
                    )
                    append(Component.newline())
                }
            }

            append(Component.newline())
        }

        append(Text.BAR)
    }
}