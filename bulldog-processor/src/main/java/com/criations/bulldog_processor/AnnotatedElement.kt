package com.criations.bulldog_processor

import com.criations.bulldog_annotations.Bulldog
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class BulldogElement(element: Element) : AnnotatedElement(element) {

    val fields: List<FieldElement> = element
            .enclosedElements
            .asSequence()
            .filter { it.kind == ElementKind.METHOD }
            .map { FieldElement(it) }
            .toList()

    val className: String
        get() {
            return if (element.getAnnotation(Bulldog::class.java).name.isNotBlank()) {
                element.getAnnotation(Bulldog::class.java).name
            } else {
                name.toString()
            }
        }

    override fun toString(): String {
        return "BulldogElement{" +
                " className=$className" +
                " fields=$fields" +
                "}"
    }

}

class FieldElement(element: Element) : AnnotatedElement(element) {

    val fieldType: TypeMirror
        get() = (element as ExecutableElement).returnType

    val fieldName: String
        get() = element.simpleName.toString().substring(3, element.simpleName.length).toLowerCase()

    override fun toString(): String {
        return "FieldElement{" +
                " fieldType=" + fieldType +
                " fieldName=" + fieldName +
                '}'
    }

}

abstract class AnnotatedElement(val element: Element) {

    val name: Name
        get() = element.simpleName

    val enclosingElement: TypeElement
        get() = element.enclosingElement as TypeElement

    override fun toString(): String {
        return "AnnotatedElement{" +
                "element=" + element +
                '}'
    }
}
