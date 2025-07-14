package dev.lunasa.modules.infuse.service

@Retention(AnnotationRetention.RUNTIME)
annotation class Service(
    val name: String = "",
    val priority: Int = 1
)