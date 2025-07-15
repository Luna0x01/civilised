package dev.lunasa.plugin.features.trading.data

import org.bukkit.entity.Player

data class TradeRequest(
    val from: Player,
    val to: Player,
    val timestamp: Long = System.currentTimeMillis(),
    var isExpired: Boolean = false
) {
    fun hasExpired(): Boolean {
        return isExpired || (System.currentTimeMillis() - timestamp) > EXPIRY_TIME
    }

    companion object {
        const val EXPIRY_TIME = 30_000L
    }
}
