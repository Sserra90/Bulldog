package com.criations.bulldog_processor

import com.criations.bulldog_annotations.Bulldog
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.WARNING

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BulldogProcessor : AbstractProcessor() {

    private var mMessager: Messager? = null
    private var mFiler: Filer? = null
    private var mTypeUtils: Types? = null
    private var mElements: Elements? = null

    private fun error(msg: String, vararg args: Any) {
        mMessager?.printMessage(ERROR, String.format(msg, *args))
    }

    private fun warning(msg: String, vararg args: Any) {
        mMessager?.printMessage(WARNING, String.format(msg, *args))
    }

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        mMessager = env.messager
        mFiler = env.filer
        mTypeUtils = env.typeUtils
        mElements = env.elementUtils
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(Bulldog::class.java.canonicalName)

    override fun process(set: Set<TypeElement>, env: RoundEnvironment): Boolean {

        val bindings: MutableList<BulldogElement> = mutableListOf()

        // Parse @Bulldog annotated elements
        ElementFilter.typesIn(env.getElementsAnnotatedWith(Bulldog::class.java)).forEach {
            parseBulldogAnnotation(it, bindings)
        }

        warning("Bindings: %s", bindings)

        makeItHappen()

        return true
    }

    private fun parseBulldogAnnotation(type: TypeElement, bindings: MutableList<BulldogElement>) {
        warning("Parse type: %s", type)
        bindings.add(BulldogElement(type))
    }

    private fun makeItHappen() {
        warning("Make it happen")

    }
}
