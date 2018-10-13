package com.criations.bulldog_processor

import com.criations.bulldog_annotations.Bulldog
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class BulldogElement(element: Element) : AnnotatedElement(element) {

    companion object {
        const val PREFIX = "Bulldog"
    }

    val fields: List<FieldElement> = element
            .enclosedElements
            .asSequence()
            .filter { it.kind == ElementKind.FIELD && it.simpleName.toString() != "INSTANCE" }
            .map { FieldElement(it) }
            .toList()

    val className: String
        get() {
            return if (element.getAnnotation(Bulldog::class.java).name.isNotBlank()) {
                element.getAnnotation(Bulldog::class.java).name
            } else {
                PREFIX + name.toString()
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

    val value: Any?
        get() = (element as VariableElement).constantValue

    val fieldType: TypeMirror
        get() = (element as VariableElement).asType()

    val fieldName: String
        get() = element.simpleName.toString()

    override fun toString(): String {
        return "FieldElement{" +
                " fieldType=" + fieldType +
                " fieldName=" + fieldName +
                " fieldValue=" + value +
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
