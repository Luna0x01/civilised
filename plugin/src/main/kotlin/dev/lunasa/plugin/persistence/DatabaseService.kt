package dev.lunasa.plugin.persistence

import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.service.Close
import dev.lunasa.modules.infuse.service.Configure
import dev.lunasa.modules.infuse.service.Service
import dev.lunasa.plugin.Civilised
import dev.lunasa.plugin.persistence.tables.Players
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils
import java.sql.Connection
import java.util.logging.Level

@Service(priority = 1000)
class DatabaseService {
    @Inject
    lateinit var plugin: Civilised

    val configuration by lazy { DatabaseConfiguration(plugin) }
    lateinit var database: Database

    @Configure
    fun configure() {
        val config = configuration
        val jdbcUrl = config.jdbcUrl
        val username = config.credentials.username
        val password = config.credentials.password

        try {
            plugin.logger.info("Connecting to ${config.type.uppercase()} database...")

            database = when (DatabaseConfiguration.DatabaseType.fromString(config.type)) {
                DatabaseConfiguration.DatabaseType.SQLITE -> Database.connect(jdbcUrl, driver = "org.sqlite.JDBC")
                DatabaseConfiguration.DatabaseType.POSTGRESQL -> Database.connect(jdbcUrl, driver = "org.postgresql.Driver", user = username, password = password)
                DatabaseConfiguration.DatabaseType.MYSQL,
                DatabaseConfiguration.DatabaseType.MARIADB -> Database.connect(jdbcUrl, driver = "com.mysql.cj.jdbc.Driver", user = username, password = password)
            }

            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

            plugin.logger.info("Connected to database!")

            val statements = transaction { MigrationUtils.statementsRequiredForDatabaseMigration(Players) }

            if (statements.isNotEmpty()) {
                plugin.logger.info("Applying pending database migrations...")

                transaction {
                    execInBatch(statements)
                }

                plugin.logger.info("Migrations applied successfully.")
            }

        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Database initialization failed: ${e.message}", e)
        }
    }

    @Close
    fun close() {
        try {
            TransactionManager.closeAndUnregister(database)
            plugin.logger.info("Database connection closed.")
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to close database: ${e.message}", e)
        }
    }
}
