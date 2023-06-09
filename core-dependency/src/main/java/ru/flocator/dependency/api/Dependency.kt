package ru.flocator.dependency.api

import androidx.fragment.app.Fragment

interface Dependencies

typealias DependenciesMap = Map<Class<out Dependencies>, Dependencies>

interface DependenciesContainer {
    val dependenciesMap: DependenciesMap
}

inline fun <reified D : Dependencies> Fragment.findDependencies(): D {
    return findDependenciesByClass(D::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <D : Dependencies> Fragment.findDependenciesByClass(clazz: Class<D>): D {
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