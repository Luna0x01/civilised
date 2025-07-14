package dev.lunasa.modules.infuse

import java.util.logging.Logger

data class InfuseOptions @JvmOverloads constructor(
    val logger: Logger = Logger.getAnonymousLogger(),
    val pkg: String? = null
)
