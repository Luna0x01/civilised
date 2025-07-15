package dev.lunasa.plugin.features.cursor

import dev.lunasa.modules.commander.CommandCategoryKey
import dev.lunasa.modules.commander.Commander
import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.plugin.Civilised
import dev.lunasa.plugin.persistence.tables.Players
import dev.lunasa.plugin.persistence.tables.Votes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.Team
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import java.util.Locale.getDefault
import java.util.UUID

/**
 * There is definitely a better way to do this, but I cannot be arsed
 * to figure it out right now (It's 1 AM bro I need to sleep)
 */
@Suppress("unused")
@Service
class ColorCursorService : Listener {
    companion object {
        val KEY = CommandCategoryKey("Reputation", "Commands related to player reputation and voting.")
    }

    @Inject
    private lateinit var plugin: Civilised

    @Configure
    fun configure() {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        this.registerCommands()
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        transaction { Players.insertIgnore { it[Players.id] = e.player.uniqueId } }
        sync(e.player)
    }

    private fun getReputation(uuid: UUID) = transaction {
        Votes.selectAll().where { Votes.target eq uuid }
            .sumOf { it[Votes.value] }
    }

    private fun vote(voter: UUID, target: UUID, value: Int): Boolean {
        if (voter == target || value !in listOf(-1, 0, 1)) return false

        transaction {
            val existing = Votes.selectAll().where { (Votes.voter eq voter) and (Votes.target eq target) }.singleOrNull()

            if (existing == null && value != 0) {
                Votes.insert {
                    it[Votes.voter] = voter
                    it[Votes.target] = target
                    it[Votes.value] = value
                }
            } else if (existing != null) {
                if (value == 0) {
                    Votes.deleteWhere { (Votes.voter eq voter) and (Votes.target eq target) }
                } else {
                    Votes.update({ (Votes.voter eq voter) and (Votes.target eq target) }) {
                        it[Votes.value] = value
                    }
                }
            }
        }

        return true
    }

    private fun sync(player: Player) {
        val color = ColorCursor.fromReputation(
            getReputation(player.uniqueId)
        )
        val teamName = "cc_${player.uniqueId}"

        val board = Bukkit.getScoreboardManager().mainScoreboard
        var team = board.getTeam(teamName)

        if (team == null) team = board.registerNewTeam(teamName)

        team.prefix(Component.text("✦ ").color(color.adventureColor))
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
        team.setCanSeeFriendlyInvisibles(false)

        if (!team.hasEntry(player.name)) team.addEntry(player.name)
    }

    private fun registerCommands() {
        listOf("upvote" to 1, "downvote" to -1).forEach { (name, value) ->
            Commander.register(name, KEY) {
                playerOnly()
                description("${name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() 
                }} a player.")
                usage("/$name <player>")
                permission("civilised.$name")

                executes {
                    if (args.isEmpty()) {
                        reply(Component.text("Usage: /$name <player>").color(NamedTextColor.RED))
                        return@executes true
                    }

                    val targetName = args[0]
                    val target = Bukkit.getPlayer(targetName)

                    if (target == null /*|| target.uniqueId == player.uniqueId*/) {
                        reply(Component.text("Invalid player.").color(NamedTextColor.RED))
                        return@executes true
                    }

                    if (vote(player.uniqueId, target.uniqueId, value)) {
                        sync(target)

                        reply(Component.text("${name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                        }}d ${target.name}!").color(NamedTextColor.GREEN))
                    } else {
                        reply(Component.text("Failed to vote.").color(NamedTextColor.RED))
                    }

                    true
                }
            }
        }

        Commander.register("unvote", KEY) {
            playerOnly()
            description("Remove your vote on a player.")
            usage("/unvote <player>")
            permission("civilised.unvote")

            executes {
                if (args.isEmpty()) {
                    reply(Component.text("Usage: /unvote <player>").color(NamedTextColor.RED))
                    return@executes true
                }

                val targetName = args[0]
                val target = Bukkit.getPlayer(targetName)

                if (target == null || target.uniqueId == player.uniqueId) {
                    reply(Component.text("Invalid player.").color(NamedTextColor.RED))
                    return@executes true
                }

                if (vote(player.uniqueId, target.uniqueId, 0)) {
                    sync(target)
                    reply(Component.text("Removed your vote on ${target.name}.").color(NamedTextColor.GREEN))
                } else {
                    reply(Component.text("Failed to unvote.").color(NamedTextColor.RED))
                }

                true
            }
        }
    }
}