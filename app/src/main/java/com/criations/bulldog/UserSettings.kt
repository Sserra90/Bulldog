package com.criations.bulldog

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.criations.bulldog_annotations.Bulldog
import com.criations.bulldog_runtime.bindPreference
import com.criations.bulldog_runtime.bullDogCtx


/**
 * @author Sérgio Serra on 30/09/2018.
 * sergioserra99@gmail.com
 */
@Bulldog(name = "UserSettings")
interface UserModel {
    val id: Int
    val email: String
}

class UserSettings : UserModel {

    private val prefs: SharedPreferences = bullDogCtx.getSharedPreferences(javaClass.simpleName, MODE_PRIVATE)

    override val id: Int by bindPreference(prefs, 10, "id")
    override val email: String by bindPreference(prefs, "sergio@gmail.com", "email")

    fun clearId() = prefs.edit().remove("id").apply()
    fun clearEmail() = prefs.edit().remove("email").apply()
    fun clearAll() {
        prefs.edit().apply {
            remove("id")
            remove("email")
        }.apply()
    }
}