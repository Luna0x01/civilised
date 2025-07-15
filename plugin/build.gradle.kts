plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.reflections)
    implementation(libs.hikari)
    implementation(libs.sqlite.jdbc)
    implementation(libs.mysql.connector)

    implementation(libs.bundles.exposed)
    implementation(libs.bundles.kotlinx.coroutines)

    implementation(project(":modules:infuse"))
    implementation(project(":modules:configuration"))
    implementation(project(":modules:commander"))

    compileOnly(libs.paper.api)
}

kotlin {
    jvmToolchain(21)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
    }

    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to version)

        inputs.properties(props)

        filteringCharset = "UTF-8"

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}