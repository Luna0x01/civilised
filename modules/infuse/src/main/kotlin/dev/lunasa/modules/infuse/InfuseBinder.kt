package dev.lunasa.modules.infuse

import kotlin.properties.Delegates
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class InfuseBinder<T : Any>(
    val kClass: KClass<out T>
) {
    val annotationChecks = mutableMapOf<KClass<out Annotation>, (Annotation) -> Boolean>()
    var instance by Delegates.notNull<Any>()

    infix fun to(any: Any): InfuseBinder<T> {
        instance = any
        return this
    }

    inline fun <reified A : Annotation> annotated(
        noinline lambda: (A) -> Boolean
    ): InfuseBinder<T> {
        annotationChecks[A::class] = lambda as (Annotation) -> Boolean
        return this
    }
}