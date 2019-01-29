package com.criations.bulldog

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.criations.bulldog_annotations.Bulldog
import com.criations.bulldog_annotations.Enum
import com.criations.bulldog_runtime.bindEnumPreference
import com.criations.bulldog_runtime.bindPreference
import com.criations.bulldog_runtime.bullDogCtx


/**
 * @author Sérgio Serra on 30/09/2018.
 * sergioserra99@gmail.com
 */
@Bulldog(name = "UserSettings")
object UserModel {
    const val id: Int = 20
    const val email: String = "sergioserra@gmail.com"
    const val likes: Long = 20L
    const val isPremium: Boolean = false
    const val minutesLeft: Float = 24.5F
    @Enum(value = Roles.user)
    val role: Roles = Roles.USER
}

@Bulldog
object NetworkSettings {
    const val id: Int = 22
    const val email: String = "sergio@gmail.com"
}

enum class Roles {
    USER, ADMIN;

    companion object {
        const val user = "USER"
        const val admin = "ADMIN"
    }
}
