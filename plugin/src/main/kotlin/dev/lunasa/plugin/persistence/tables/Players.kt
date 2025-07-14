package dev.lunasa.plugin.persistence.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object Players : UUIDTable("players", columnName = "uuid") {
    val reputation = integer("reputation").default(0)
}
