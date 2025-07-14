package dev.lunasa.plugin.features

import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.plugin.Civilised
import dev.lunasa.plugin.features.cursor.ColorCursor
import dev.lunasa.plugin.persistence.tables.Players
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Animals
import org.bukkit.entity.Bee
import org.bukkit.entity.Boss
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.NPC
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

/**
 * There is definitely a better way to do this, but I cannot be arsed
 * to figure it out right now (It's 1 AM bro I need to sleep)
 */
@Suppress("unused")
@Service
class ColorCursorService : Listener {
    @Inject
    lateinit var plugin: Civilised

    private val playerRep = mutableMapOf<UUID, Int>()
    private val lastHit = mutableMapOf<UUID, UUID>()

    private fun loadPlayer(uuid: UUID): Int =
        transaction {
            Players.insertIgnore { it[Players.id] = uuid }
            Players.selectAll().where { Players.id eq uuid }
                .single()[Players.reputation]
        }.also { playerRep[uuid] = it }

    private fun savePlayer(uuid: UUID) {
        val rep = playerRep[uuid] ?: 0
        transaction {
            Players.update({ Players.id eq uuid }) {
                it[reputation] = rep
            }
        }
    }

    @Configure
    fun configure() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) = syncPlayerCursor(e.player)

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) = savePlayer(e.player.uniqueId)

    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        val damager = e.damager as? Player ?: return
        val victim = e.entity as? LivingEntity ?: return

        if (victim is Player && getPlayerColor(victim) == ColorCursor.GREEN) {
            playerRep[damager.uniqueId] = (playerRep[damager.uniqueId] ?: 0) + 1
            syncPlayerCursor(damager)
        }
        lastHit[victim.uniqueId] = damager.uniqueId
    }

    @EventHandler
    fun onKill(e: EntityDeathEvent) {
        val killerId = lastHit[e.entity.uniqueId] ?: return
        val killer = Bukkit.getPlayer(killerId) ?: return

        if (e.entity is Player) {
            playerRep[killerId] = (playerRep[killerId] ?: 0) + 3
            syncPlayerCursor(killer)
        }
    }

    private fun getPlayerColor(p: Player): ColorCursor =
        ColorCursor.fromReputation(playerRep[p.uniqueId] ?: 0)

    private fun syncPlayerCursor(player: Player) {
        val color = getPlayerColor(player)
        val teamName = "cc_${player.uniqueId}"

        val board = Bukkit.getScoreboardManager().mainScoreboard
        var team = board.getTeam(teamName)

        if (team == null) team = board.registerNewTeam(teamName)

        team.prefix(Component.text("✦ ").color(color.adventureColor))
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
        team.setCanSeeFriendlyInvisibles(false)

        if (!team.hasEntry(player.name)) team.addEntry(player.name)
    }
}