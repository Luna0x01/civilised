package dev.lunasa.modules.infuse.binder

import dev.lunasa.modules.infuse.InfuseBinder
import kotlin.reflect.KClass

class InfuseBinderMultiType(
    private val container: InfuseBinderContainer,
    private val instance: Any
) {
    val types = mutableListOf<KClass<*>>()
    var binderInternalPopulator = { _: InfuseBinder<*> -> }

    inline fun <reified T> to(): InfuseBinderMultiType {
        types += T::class
        return this
    }

    fun to(kClass: KClass<*>): InfuseBinderMultiType {
        types += kClass
        return this
    }

    fun populate(populator: InfuseBinder<*>.() -> Unit): InfuseBinderMultiType {
        binderInternalPopulator = populator
        return this
    }

    fun bind() {
        for (type in types) {
            container.binders += InfuseBinder(type)
                .apply(binderInternalPopulator)
                .to(instance)
        }
    }
}