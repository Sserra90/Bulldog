@file:Suppress("PackageName")

package com.criations.bulldog_runtime

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

lateinit var prefs: SharedPreferences
@SuppressLint("StaticFieldLeak") // Use application context
lateinit var bullDogCtx: Context

inline fun <reified V : Any> Any.bindPreference(prefs: SharedPreferences, default: V, key: String? = null): ReadWriteProperty<Any, V> {
    return PreferencesVar(prefs, IdentityAdapter(V::class.java), key) { default }
}

inline fun <reified E : Enum<E>> Any.bindEnumPreference(prefs: SharedPreferences, default: E, key: String? = null): ReadWriteProperty<Any, E> {
    return PreferencesVar(prefs, EnumAdapter(E::class.java), key) { default }
}

inline fun <reified E : Enum<E>> Any.bindEnumPreference(prefs: SharedPreferences, noinline default: () -> E, key: String? = null): ReadWriteProperty<Any, E> {
    return PreferencesVar(prefs, EnumAdapter(E::class.java), key, default)
}

interface Adapter<V, P> {
    fun type(): Class<P>
    fun fromPreference(preference: P): V
    fun toPreference(value: V): P
}

class IdentityAdapter<T>(val clazz: Class<T>) : Adapter<T, T> {
    override fun type(): Class<T> = clazz
    override fun fromPreference(preference: T): T = preference
    override fun toPreference(value: T): T = value
}

class EnumAdapter<E : Enum<E>>(val clazz: Class<E>) : Adapter<E, String> {
    override fun type(): Class<String> = String::class.java
    override fun fromPreference(preference: String): E = java.lang.Enum.valueOf(clazz, preference)
    override fun toPreference(value: E): String = value.name
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
class PreferencesVar<T : Any, V : Any, P : Any>(
        private val prefs: SharedPreferences,
        private val adapter: Adapter<V, P>,
        private val key: String?,
        private val default: () -> V
) : ReadWriteProperty<T, V> {

    private val preference = onGetPropertyFromClass(adapter.type())

    override operator fun getValue(thisRef: T, property: KProperty<*>): V {
        val name = key ?: property.name

        if (!prefs.contains(name)) {
            setValue(thisRef, property, default())
        }

        return adapter.fromPreference(preference[prefs, name] as P)
    }

    override operator fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        prefs.edit().apply {
            preference[this, key ?: property.name] = adapter.toPreference(value)
            apply()
        }
    }
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
private fun onGetPropertyFromClass(clazz: Class<*>): Preference<Any> {
    return when (clazz) {
        kotlin.Boolean::class.java -> BooleanPreference
        kotlin.Float::class.java -> FloatPreference
        kotlin.Int::class.java -> IntPreference
        kotlin.Long::class.java -> LongPreference
        kotlin.String::class.java -> StringPreference

        java.lang.Boolean::class.java -> BooleanPreference
        java.lang.Float::class.java -> FloatPreference
        java.lang.Integer::class.java -> IntPreference
        java.lang.Long::class.java -> LongPreference
        java.lang.String::class.java -> StringPreference

        else -> throw UnsupportedOperationException("Unsupported preference type \"${clazz.canonicalName}\"")
    } as Preference<Any>
}

private interface Preference<T> {
    operator fun set(editor: SharedPreferences.Editor, name: String, value: T)
    operator fun get(preferences: SharedPreferences, name: String): T
}

private object BooleanPreference : Preference<Boolean> {
    override fun set(editor: SharedPreferences.Editor, name: String, value: Boolean) {
        editor.putBoolean(name, value)
    }

    override fun get(preferences: SharedPreferences, name: String): Boolean {
        return preferences.getBoolean(name, false)
    }
}

private object FloatPreference : Preference<Float> {
    override fun set(editor: SharedPreferences.Editor, name: String, value: Float) {
        editor.putFloat(name, value)
    }

    override fun get(preferences: SharedPreferences, name: String): Float {
        return preferences.getFloat(name, 0.0f)
    }
}

private object IntPreference : Preference<Int> {
    override fun set(editor: SharedPreferences.Editor, name: String, value: Int) {
        editor.putInt(name, value)
    }

    override fun get(preferences: SharedPreferences, name: String): Int {
        return preferences.getInt(name, 0)
    }
}

private object LongPreference : Preference<Long> {
    override fun set(editor: SharedPreferences.Editor, name: String, value: Long) {
        editor.putLong(name, value)
    }

    override fun get(preferences: SharedPreferences, name: String): Long {
        return preferences.getLong(name, 0L)
    }
}

private object StringPreference : Preference<String> {
    override fun set(editor: SharedPreferences.Editor, name: String, value: String) {
        editor.putString(name, value)
    }

    override fun get(preferences: SharedPreferences, name: String): String {
        return preferences.getString(name, "")
    }
}
