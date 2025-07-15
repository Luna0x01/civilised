package dev.lunasa.modules.configuration

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.reflect.KProperty

/**
 * The [OptionStorage] class provides a base for managing configuration options
 * in a YAML file for a Bukkit plugin.
 *
 * @author Lunasa
 */
abstract class OptionStorage(val plugin: JavaPlugin, val name: String) {
    private val configFile: File by lazy {
        Bukkit.getPluginsFolder().resolve(plugin.name).mkdirs()
        val file = Bukkit.getPluginsFolder().resolve(plugin.name).resolve("$name.yml")
        if (!file.exists()) {
            file.createNewFile()
        }
        file
    }

    val fileConfiguration: FileConfiguration by lazy {
        YamlConfiguration.loadConfiguration(configFile)
    }

    init {
        load()
    }

    // We enforce that the property MUST have a default value
    protected fun <T> property(path: String, default: T): ConfigurationProperty<T> {
        load()
        return ConfigurationProperty(path, default)
    }

    abstract inner class Section(private val basePath: String = "") {
        protected fun fullPath(key: String): String {
            return if (basePath.isBlank()) key else "$basePath.$key"
        }

        protected fun <T> property(path: String, default: T): ConfigurationProperty<T> {
            return ConfigurationProperty(fullPath(path), default)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Section = this
    }

    inner class ConfigurationProperty<T>(
        private val path: String,
        private val default: T
    ) {
        init {
            if (path.isBlank()) {
                throw IllegalArgumentException("Path cannot be blank")
            }

            if (!fileConfiguration.contains(path))
                fileConfiguration.set(path, default)
        }

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val value: T? = when (default) {
                is String -> fileConfiguration.getString(path) as T?
                is Int -> fileConfiguration.getInt(path) as T?
                is Boolean -> fileConfiguration.getBoolean(path) as T?
                is Double -> fileConfiguration.getDouble(path) as T?
                is Long -> fileConfiguration.getLong(path) as T?
                is List<*> -> fileConfiguration.getList(path) as T?
                else -> fileConfiguration.get(path) as? T
            }

            if (value == null) {
                fileConfiguration.set(path, default)
                save()

                return default
            }

            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            fileConfiguration.set(path, value)
        }
    }

    fun load() {
        fileConfiguration.load(configFile)
    }

    fun save() {
        configFile.writeText(fileConfiguration.saveToString())
    }

    open fun reload() {
        this.load()
    }
}
