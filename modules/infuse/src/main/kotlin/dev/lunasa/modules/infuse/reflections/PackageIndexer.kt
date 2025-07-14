package dev.lunasa.modules.infuse.reflections

import dev.lunasa.modules.infuse.InfuseOptions
import org.reflections.Reflections
import org.reflections.Store
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.Scanners
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.QueryFunction
import java.lang.reflect.Method
import kotlin.reflect.KClass

class PackageIndexer(
    private val clazz: KClass<*>,
    options: InfuseOptions
) {
    val reflections =
        Reflections(
            ConfigurationBuilder()
                .forPackage(
                    options.pkg ?: this.clazz.java.`package`.name,
                    this.clazz.java.classLoader
                )
                .addScanners(
                    MethodAnnotationsScanner(),
                    TypeAnnotationsScanner(),
                    SubTypesScanner()
                )
        )

    inline fun <reified T> getSubTypes(): List<Class<*>> {
        return reflections[subTypes<T>()]
            .toList()
    }

    inline fun <reified T : Annotation> getMethodsAnnotatedWith(): List<Method> {
        return reflections
            .get(annotated<T>())
            .toList()
    }

    inline fun <reified T : Annotation> getTypesAnnotatedWith(): List<Class<*>> {
        return reflections
            .getTypesAnnotatedWith(T::class.java)
            .toList()
    }

    inline fun <reified T> annotated(): QueryFunction<Store, Method> {
        return Scanners.MethodsAnnotated
            .with(T::class.java)
            .`as`(Method::class.java)
    }

    inline fun <reified T> subTypes(): QueryFunction<Store, Class<*>> {
        return Scanners.SubTypes
            .with(T::class.java)
            .`as`(Class::class.java)
    }
}