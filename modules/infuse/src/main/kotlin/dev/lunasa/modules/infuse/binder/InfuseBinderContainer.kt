package dev.lunasa.modules.infuse.binder

import dev.lunasa.modules.infuse.InfuseBinder

abstract class InfuseBinderContainer {
    internal val binders = mutableListOf<InfuseBinder<*>>()

    abstract fun populate()

    fun bind(obj: Any) = InfuseBinderMultiType(this, obj)
}
