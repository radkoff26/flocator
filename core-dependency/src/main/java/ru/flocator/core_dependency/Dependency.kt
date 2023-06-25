package ru.flocator.core_dependency

import androidx.fragment.app.Fragment

typealias DependenciesMap = Map<Class<*>, Any>

interface DependenciesContainer {
    var dependenciesMap: DependenciesMap
}

inline fun <reified D> Fragment.findDependencies(): D {
    return findDependenciesByClass(D::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <D> Fragment.findDependenciesByClass(clazz: Class<D>): D {
    return parents.firstNotNullOfOrNull {
        it.dependenciesMap[clazz] as D?
    } ?: throw IllegalStateException("There is no such dependency!")
}

private val Fragment.parents: Iterable<DependenciesContainer>
    get() = allParents.filterIsInstance<DependenciesContainer>()

private val Fragment.allParents: Iterable<Any>
    get() = object : Iterable<Any> {
        override fun iterator(): Iterator<Any> {
            return object : Iterator<Any> {
                private var currentParentFragment = parentFragment
                private var currentParentActivity = activity
                private var currentParentApplication = currentParentActivity?.application

                override fun hasNext(): Boolean =
                    !(currentParentFragment == null && currentParentActivity == null && currentParentApplication == null)

                override fun next(): Any {
                    currentParentFragment?.let {
                        currentParentFragment = it.parentFragment
                        return it
                    }
                    currentParentActivity?.let {
                        currentParentActivity = null
                        return it
                    }
                    currentParentApplication?.let {
                        currentParentApplication = null
                        return it
                    }
                    throw NoSuchElementException()
                }
            }
        }

    }