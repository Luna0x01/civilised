package dev.lunasa.plugin.features.trading.data

import org.bukkit.entity.Player
import java.util.UUID

data class TradeSession(
    val id: UUID = UUID.randomUUID(),
    val initiator: Player,
    val target: Player,
    val initiatorOffer: TradeOffer = TradeOffer(initiator),
    val targetOffer: TradeOffer = TradeOffer(target),
    var status: TradeStatus = TradeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isReady get() = initiatorOffer.isReady && targetOffer.isReady
    val isConfirmed get() = initiatorOffer.isConfirmed && targetOffer.isConfirmed

    fun getOtherPlayer(player: Player): Player {
        return if (player == initiator) target else initiator
    }

    fun getPlayerOffer(player: Player): TradeOffer {
        return if (player == initiator) initiatorOffer else targetOffer
    }

    fun getOtherPlayerOffer(player: Player): TradeOffer {
        return if (player == initiator) targetOffer else initiatorOffer
    }
}