package com.leonardo.drinkslab.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.ui.login.Login
import java.util.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val intent = Intent(this@SplashScreen, Login::class.java)
                startActivity(intent)
                finish()
            }
        }, 800L)
    }
}
