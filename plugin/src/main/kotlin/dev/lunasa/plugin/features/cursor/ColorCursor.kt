package dev.lunasa.plugin.features.cursor

import net.kyori.adventure.text.format.NamedTextColor

enum class ColorCursor(val adventureColor: NamedTextColor) {
    // players
    GREEN(NamedTextColor.GREEN),
    ORANGE(NamedTextColor.GOLD),

    // monsters
    // TODO: Implement monsters in [ColorCursorService]
    YELLOW(NamedTextColor.YELLOW),
    LIGHT_PINK(NamedTextColor.LIGHT_PURPLE),
    RED(NamedTextColor.RED),
    PURPLE(NamedTextColor.DARK_PURPLE),
    DARK_CRIMSON(NamedTextColor.DARK_RED),
    BLACK(NamedTextColor.BLACK);

    companion object {
        fun fromReputation(rep: Int): ColorCursor =
            if (rep >= 1) ORANGE else GREEN

        fun forMonster(delta: Int): ColorCursor = when {
            delta >= 10 -> LIGHT_PINK
            delta >= -5 -> RED
            delta >= -15 -> PURPLE
            delta >= -25 -> DARK_CRIMSON
            else -> BLACK
        }
    }
}