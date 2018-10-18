package com.criations.bulldog

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.criations.bulldog.UserModel.role
import com.criations.bulldog.UserSettings2.*
import com.criations.bulldog_runtime.bullDogCtx

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bullDogCtx = applicationContext

        UserSettings().apply {
            id = 2
            email = "abc@gmail.com"
            role = Roles.ADMIN
        }

        Log.d("PREFS", UserSettings().toString())

    }

}
