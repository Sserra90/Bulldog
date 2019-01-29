package com.criations.bulldog_processor

import com.criations.bulldog_annotations.Bulldog
import com.criations.bulldog_annotations.Enum
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.sun.org.apache.xerces.internal.util.DOMUtil.getAnnotation
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.*

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BulldogProcessor : AbstractProcessor() {

    private lateinit var mMessager: Messager
    private lateinit var mFiler: Filer
    private lateinit var mTypeUtils: Types
    private lateinit var mElements: Elements
    private val allowedTypes: MutableList<TypeMirror> = mutableListOf()

    private fun error(msg: String, vararg args: Any) {
        mMessager.printMessage(ERROR, String.format(msg, *args))
    }

    private fun warning(msg: String, vararg args: Any) {
        mMessager.printMessage(NOTE, String.format(msg, *args))
    }

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        mMessager = env.messager
        mFiler = env.filer
        mTypeUtils = env.typeUtils
        mElements = env.elementUtils

        allowedTypes.add("java.lang.Long".asType(mElements))
        allowedTypes.add("java.lang.Integer".asType(mElements))
        allowedTypes.add("java.lang.Float".asType(mElements))
        allowedTypes.add("java.lang.String".asType(mElements))
        allowedTypes.add("java.lang.Boolean".asType(mElements))
        allowedTypes.add("java.lang.Enum".asType(mElements))
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
        val bulldogElement = BulldogElement(type)
        if (validate(bulldogElement)) {
            bindings.add(bulldogElement)
        }
    }

    private fun validate(element: BulldogElement): Boolean {
        element.fields.forEach { field ->

            if (!allowedTypes.contains(mapToJavaType(field))) {
                error("Type %s is not allowed, field: %s", field.fieldType.asTypeName(), field)
                return false
            }

            if (isEnum(field)) {
                if (!hasEnumAnnotation(field.element)) {
                    error("Field %s is an Enum, please annotated the field with @Enum", field)
                    return false
                }

                val value = field.element.getAnnotation(Enum::class.java).value
                if (value.isEmpty()) {
                    error("Value for field %s cannot be null or empty", field)
                    return false
                }
            }

        }
        return true
    }

    private fun mapToJavaType(field: FieldElement): TypeMirror? {

        return when (field.fieldType.asTypeName().toString()) {
            "kotlin.Long" -> "java.lang.Long".asType(mElements)
            "kotlin.String" -> "java.lang.String".asType(mElements)
            "kotlin.Float" -> "java.lang.Float".asType(mElements)
            "kotlin.Int" -> "java.lang.Integer".asType(mElements)
            "kotlin.Boolean" -> "java.lang.Boolean".asType(mElements)

            "java.lang.String" -> "java.lang.String".asType(mElements)
            else -> {
                if (isEnum(field)) "java.lang.Enum".asType(mElements) else null
            }
        }
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
            val funClearAllSpec = FunSpec.builder(CLEAR_ALL).addCode("$PREFS.edit().apply{")

            val classSpec = TypeSpec.classBuilder(it.className)
            val prefsSpec = PropertySpec.builder(PREFS, prefsType)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("%T.getSharedPreferences(javaClass.simpleName, %T)", bulldogType, modeType)
                    .build()
            classSpec.addProperty(prefsSpec)

            it.fields.forEach { field ->
                val propSpec = PropertySpec.builder(field.fieldName, getType(field))
                        .addModifiers(KModifier.PUBLIC)
                        .mutable(true)
                        .delegate(getCodeFormat(field), getPrefType(field), getFieldValue(field), field.fieldName)
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

            funcSpecs.add(generateToString(it))

            classSpec.addFunctions(funcSpecs)
            file.addType(classSpec.build())
        }

        val outfile = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME])
        if (!outfile.exists()) {
            outfile.mkdirs()
        }

        file.build().writeTo(outfile)
    }

    private fun getFieldValue(field: FieldElement): Any? {
        if (!isEnum(field)) {
            return field.value
        }

        return field.fieldType.toString() + "." + field.element.getAnnotation(Enum::class.java).value
    }

    private fun generateToString(element: BulldogElement): FunSpec {
        var toStringBody = ""
        element.fields.forEach { field ->
            toStringBody += " ${field.fieldName}=$${field.fieldName},"
        }
        return FunSpec.builder("toString")
                .addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addCode("return \"%L $toStringBody\"", element.className)
                .build()
    }

    private fun getType(field: FieldElement): TypeName {
        return when (field.fieldType.toString()) {
            "java.lang.String" -> ClassName("kotlin", "String")
            else -> field.fieldType.asTypeName()
        }
    }

    private fun getCodeFormat(field: FieldElement): String {
        return when (field.fieldType.asTypeName().toString()) {
            "java.lang.String" -> "%T($PREFS, %S, %S)"
            "kotlin.Float" -> "%T($PREFS, %LF, %S)"
            else -> "%T($PREFS, %L, %S)"
        }
    }

    private fun getPrefType(field: FieldElement): ClassName {
        if (isEnum(field)) {
            return bindEnumPrefType
        }
        return bindPrefType
    }

    private fun isEnum(field: FieldElement): Boolean {
        val fieldType: TypeMirror = field.fieldType
        if (fieldType.kind == TypeKind.DECLARED) {
            val type = mElements.getTypeElement(fieldType.toString())
            if (type.kind == ElementKind.ENUM) {
                return true
            }
        }
        return false
    }

    private fun hasEnumAnnotation(element: Element): Boolean {
        return element.getAnnotation(Enum::class.java) != null
    }

    private fun getPackage(element: TypeElement): String =
            element.qualifiedName.toString().substring(0, element.qualifiedName.toString().lastIndexOf("."))

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val KAPT_FILENAME = "BulldogSettings"
        const val PREFS = "prefs"
        const val CLEAR_ALL = "clearAll"

        val prefsType = ClassName("android.content", "SharedPreferences")
        val modeType = ClassName("android.content.Context", "MODE_PRIVATE")
        val bulldogType = ClassName("com.criations.bulldog_runtime", "bullDogCtx")
        val bindPrefType = ClassName("com.criations.bulldog_runtime", "bindPreference")
        val bindEnumPrefType = ClassName("com.criations.bulldog_runtime", "bindEnumPreference")

    }

}

fun String.asType(elements: Elements): TypeMirror = elements.getTypeElement(this).asType()