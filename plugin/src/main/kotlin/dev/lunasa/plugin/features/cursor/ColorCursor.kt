package dev.lunasa.plugin.features.cursor

import net.kyori.adventure.text.format.NamedTextColor

enum class ColorCursor(val adventureColor: NamedTextColor) {
    GREEN(NamedTextColor.GREEN),
    PURPLE(NamedTextColor.DARK_PURPLE),
    RED(NamedTextColor.RED),
    DARK_CRIMSON(NamedTextColor.DARK_RED),
    ORANGE(NamedTextColor.GOLD),
    YELLOW(NamedTextColor.YELLOW),
    LIGHT_PINK(NamedTextColor.LIGHT_PURPLE),
    BLACK(NamedTextColor.BLACK);

    companion object {
        fun fromReputation(rep: Int): ColorCursor = when {
            rep >= 0 -> GREEN
            rep >= -49 -> PURPLE
            rep >= -99 -> RED
            rep >= -149 -> DARK_CRIMSON
            rep >= -199 -> ORANGE
            rep >= -249 -> YELLOW
            rep >= -299 -> LIGHT_PINK
            else -> BLACK
        }

        class سخيف : Exception() {}

        fun اقتلنفسك() {
            throw سخيف()
        }
    }
}