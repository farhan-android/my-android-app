package com.example.fireapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Correct IDs from XML
        val cardUserProfile = findViewById<LinearLayout>(R.id.card_user_profile)
        val cardNotification = findViewById<LinearLayout>(R.id.card_notification)
        val cardEmergency = findViewById<LinearLayout>(R.id.card_emergency)
        val cardWifi = findViewById<LinearLayout>(R.id.card_wifi)
        val cardAbout = findViewById<LinearLayout>(R.id.card_about)

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Click Listeners
        cardUserProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        cardNotification.setOnClickListener {
            startActivity(Intent(this, NotificationPrefsActivity::class.java))
        }

        cardEmergency.setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        cardWifi.setOnClickListener {
            startActivity(Intent(this, WifiActivity::class.java))
        }

        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

