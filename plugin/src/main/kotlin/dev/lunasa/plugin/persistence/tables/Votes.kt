package dev.lunasa.plugin.persistence.tables

import org.jetbrains.exposed.v1.core.Table

object Votes : Table("votes") {
    val voter = uuid("voter")
    val target = uuid("target")
    val value = integer("value")

    override val primaryKey = PrimaryKey(voter, target)
}