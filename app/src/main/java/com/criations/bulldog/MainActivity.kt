package com.criations.bulldog

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.criations.bulldog_runtime.bullDogCtx

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bullDogCtx = applicationContext

        UserSettings().apply {
            Log.d("PREFS", "Id $id")
            Log.d("PREFS", "Id $email")
        }

    }
}
