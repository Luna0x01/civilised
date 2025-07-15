package dev.lunasa.modules.ui.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

object Text {
    fun createGradient(text: String, startColor: String, endColor: String): Component {
        return MiniMessage.miniMessage().deserialize("<gradient:$startColor:$endColor>$text</gradient>")
    }

    fun createColor(text: String, color: String): Component {
        return MiniMessage.miniMessage().deserialize("<$color>$text")
    }

    fun serialize(component: Component) = MiniMessage.miniMessage().serialize(component)

    fun serialize(block: Component.() -> Unit) = Component.empty().apply {
        this.block()
        MiniMessage.miniMessage().serialize(this)
    }

    fun buildComponent(block: TextComponent.Builder.() -> Unit) = Component.text { it.block() }

    fun Component.append(text: String) {
        this.append(Component.text(text))
    }
    fun TextComponent.Builder.append(text: String) {
        this.append(Component.text(text))
    }

    val BAR = buildComponent {
        append("───────────────────────────────────")
        color(NamedTextColor.GRAY)
        decorate(TextDecoration.STRIKETHROUGH)
    }

    object Trading {
        fun title() = createGradient("Trade", "#FF6B35", "#F7931E")
        fun confirm() = createColor("✓ Confirm", "#4CAF50")
        fun cancel() = createColor("✗ Cancel", "#F44336")
        fun ready() = createColor("✓ Ready", "#4CAF50")
        fun notReady() = createColor("⏳ Not Ready", "#FF9800")
        fun yourItems() = createColor("📦 Your Offer", "#9C27B0")
        fun theirItems(name: Component) = createColor("📦 ${serialize(name)}'s Offer", "#3F51B5")
        fun addItems() = createColor("➕ Add Items", "#00BCD4")
    }
}
