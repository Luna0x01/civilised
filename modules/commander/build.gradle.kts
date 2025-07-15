plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":modules:infuse"))
}