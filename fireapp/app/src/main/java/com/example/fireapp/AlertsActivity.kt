package com.example.fireapp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AlertsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var tvAlertMessage: TextView
    private lateinit var tvSmoke: TextView
    private lateinit var tvTemp: TextView
    private lateinit var btnSoundAlarm: LinearLayout
    private lateinit var btnSendAlert: LinearLayout
    private lateinit var btnSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("AlertData")

        // Bind views
        tvAlertMessage = findViewById(R.id.tvAlertMessage)
        tvSmoke = findViewById(R.id.tvSmoke)
        tvTemp = findViewById(R.id.tvTemp)
        btnSoundAlarm = findViewById(R.id.btnSoundAlarm)
        btnSendAlert = findViewById(R.id.btnSendAlert)

        // Real-time Firebase Listener
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fireDetected = snapshot.child("fireDetected").getValue(Boolean::class.java) ?: false
                val room = snapshot.child("room").getValue(String::class.java) ?: "Unknown"
                val smoke = snapshot.child("smokeLevel").getValue(Int::class.java) ?: 0
                val temp = snapshot.child("temperature").getValue(Int::class.java) ?: 0

                if (fireDetected) {
                    tvAlertMessage.text = "Fire Detected in $room!"
                    tvSmoke.text = "Smoke Level: ${smoke}%"
                    tvTemp.text = "Temperature: ${temp}°C"
                } else {
                    tvAlertMessage.text = "No Fire Detected"
                    tvSmoke.text = "Smoke Level: Safe"
                    tvTemp.text = "Temperature: Normal"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AlertsActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Buttons
        btnSoundAlarm.setOnClickListener {
            database.child("soundAlarm").setValue(true)
            Toast.makeText(this, "Sound Alarm Triggered 🔔", Toast.LENGTH_SHORT).show()
        }

        btnSendAlert.setOnClickListener {
            database.child("sendAlert").setValue(true)
            Toast.makeText(this, "Alert Sent 🚨", Toast.LENGTH_SHORT).show()
        }
    }
}


