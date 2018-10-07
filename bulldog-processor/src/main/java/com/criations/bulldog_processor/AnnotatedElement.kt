package com.criations.bulldog_processor

import javax.lang.model.element.Element
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement

abstract class AnnotatedElement(private val element: Element) {

    val name: Name
        get() = element.simpleName

    val enclosingElement: TypeElement
        get() = element.enclosingElement as TypeElement

    override fun toString(): String {
        return "AnnotatedElement{" +
                "element=" + element +
                '}'.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnnotatedElement

        if (element != other.element) return false

        return true
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

}
