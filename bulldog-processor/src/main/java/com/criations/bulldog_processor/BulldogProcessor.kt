package com.criations.bulldog_processor

import com.criations.bulldog_annotations.Bulldog
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
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

    private lateinit var mMessager: Messager
    private lateinit var mFiler: Filer
    private lateinit var mTypeUtils: Types
    private lateinit var mElements: Elements

    private fun error(msg: String, vararg args: Any) {
        mMessager.printMessage(ERROR, String.format(msg, *args))
    }

    private fun warning(msg: String, vararg args: Any) {
        mMessager.printMessage(WARNING, String.format(msg, *args))
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

        makeItHappen(bindings)

        return true
    }

    private fun parseBulldogAnnotation(type: TypeElement, bindings: MutableList<BulldogElement>) {
        warning("Parse type: %s", type)
        bindings.add(BulldogElement(type))
    }

    private fun makeItHappen(bindings: List<BulldogElement>) {

        if (bindings.isEmpty()) {
            return
        }

        warning("Make it happen")

        val file = FileSpec.builder(getPackage(bindings.first().element as TypeElement), KAPT_FILENAME)

        bindings.forEach {
            warning("Add type: %s", it.className)

            val funcSpecs = mutableListOf<FunSpec>()
            val funClearAllSpec = FunSpec.builder(CLEAR_ALL).addCode("return $PREFS.edit().apply{")

            val classSpec = TypeSpec.classBuilder(it.className)
            val prefsSpec = PropertySpec.builder(PREFS, prefsType)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("%T.getSharedPreferences(javaClass.simpleName, %T)", bulldogType, modeType)
                    .build()
            classSpec.addProperty(prefsSpec)

            it.fields.forEach { field ->
                val propSpec = PropertySpec.builder(field.fieldName, getType(field))
                        .addModifiers(KModifier.PUBLIC)
                        .delegate(getCodeFormat(field), bindPrefType, field.value, field.fieldName)
                        .build()
                classSpec.addProperty(propSpec)

                funClearAllSpec.addStatement("remove(%S)", field.fieldName)

                funcSpecs.add(
                        FunSpec.builder("clear" + field.fieldName.capitalize())
                                .addStatement("return $PREFS.edit().remove(%S).apply()", field.name)
                                .build()
                )
            }

            funcSpecs.add(funClearAllSpec.addStatement("}.apply()").build())

            classSpec.addFunctions(funcSpecs)
            file.addType(classSpec.build())
        }

        val outfile = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME], KAPT_DIR)
        if (!outfile.exists()) {
            outfile.mkdirs()
        }

        file.build().writeTo(outfile)
    }

    private fun getType(field: FieldElement): TypeName {
        return when (field.fieldType.toString()) {
            "java.lang.String" -> ClassName("kotlin", "String")
            else -> field.fieldType.asTypeName()
        }
    }

    private fun getCodeFormat(field: FieldElement): String {
        if (mTypeUtils.isAssignable(field.fieldType, mElements.getTypeElement("java.lang.String").asType())) {
            return "%T($PREFS, %S, %S)"
        }
        return "%T($PREFS, %L, %S)"
    }

    private fun getPackage(element: TypeElement): String =
            element.qualifiedName.toString().substring(0, element.qualifiedName.toString().lastIndexOf("."))


    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val KAPT_DIR = "Bulldog"
        const val KAPT_FILENAME = "BulldogSettings"
        const val PREFS = "prefs"
        const val CLEAR_ALL = "clearAll"

        val prefsType = ClassName("android.content", "SharedPreferences")
        val modeType = ClassName("android.content.Context", "MODE_PRIVATE")
        val bulldogType = ClassName("com.criations.bulldog_runtime", "bullDogCtx")
        val bindPrefType = ClassName("com.criations.bulldog_runtime", "bindPreference")
    }

}
