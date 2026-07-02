package com.example.fireapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 2 seconds then go to Login
        window.decorView.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}
