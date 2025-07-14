package dev.lunasa.modules.infuse.mappings

import dev.lunasa.modules.infuse.inject.Extract
import dev.lunasa.modules.infuse.inject.Inject
import dev.lunasa.modules.infuse.inject.condition.Named
import dev.lunasa.modules.infuse.service.Close
import dev.lunasa.modules.infuse.service.Configure
import org.eclipse.sisu.PostConstruct
import org.eclipse.sisu.PreDestroy

object AnnotationMappings {
    private val mappings = mutableMapOf(
        AnnotationType.Inject to listOf(
            Inject::class.java
        ),
        AnnotationType.Named to listOf(
            Named::class.java
        ),
        AnnotationType.Extract to listOf(
            Extract::class.java
        ),
        AnnotationType.PostConstruct to listOf(
            Configure::class.java,
            PostConstruct::class.java,
        ),
        AnnotationType.PreDestroy to listOf(
            Close::class.java,
            PreDestroy::class.java,
        )
    )

    fun matchesAny(
        type: AnnotationType,
        annotations: Array<Annotation>
    ): Boolean {
        val mapping = this.mappings[type]!!
        return annotations.any {
            it.annotationClass.java in mapping }
    }
}