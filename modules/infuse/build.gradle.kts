plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.reflections)
    compileOnly(libs.paper.api)
}