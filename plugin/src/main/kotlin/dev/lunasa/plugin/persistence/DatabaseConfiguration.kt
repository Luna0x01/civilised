package dev.lunasa.plugin.persistence

import dev.lunasa.modules.configuration.OptionStorage
import org.bukkit.plugin.java.JavaPlugin

class DatabaseConfiguration(plugin: JavaPlugin) : OptionStorage(plugin, "database") {
    val type by property("type", DatabaseType.SQLITE.name.lowercase())

    val host by property("host", "localhost")
    val port by property("port", 3306)
    val database by property("database", "civilised")
    val file by property("file", "storage/database.db")

    inner class Credentials : Section("credentials") {
        val username by property("username", "root")
        val password by property("password", "")
    }

    val credentials = Credentials()

    inner class Pooling : Section("pool") {
        val enabled by property("enabled", true)
        val maxConnections by property("max-connections", 10)
        val minConnections by property("min-connections", 1)
        val connectionTimeout by property("timeout", 30000L) // in millis
    }

    val pool = Pooling()

    enum class DatabaseType {
        SQLITE,
        MYSQL,
        MARIADB,
        POSTGRESQL;

        companion object {
            fun fromString(type: String): DatabaseType = entries.find {
                it.name.equals(type, ignoreCase = true)
            } ?: SQLITE
        }
    }

    val jdbcUrl get() = when (DatabaseType.fromString(type)) {
        DatabaseType.SQLITE -> "jdbc:sqlite:$file"
        DatabaseType.MYSQL -> "jdbc:mysql://$host:$port/$database"
        DatabaseType.MARIADB -> "jdbc:mariadb://$host:$port/$database"
        DatabaseType.POSTGRESQL -> "jdbc:postgresql://$host:$port/$database"
    }
}
