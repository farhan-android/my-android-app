package com.example.fireapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fireapp.databinding.ActivityWifiBinding

class WifiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWifiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // 🔄 LOAD SAVED DATA
        binding.etWifi.setText(prefs.getString("wifi", ""))
        binding.etPass.setText(prefs.getString("pass", ""))
        binding.etIp.setText(prefs.getString("camera_ip", ""))
        binding.etEspIp.setText(prefs.getString("esp_ip", ""))

        // 💾 SAVE BUTTON
        binding.btnSave.setOnClickListener {

            val wifi = binding.etWifi.text.toString()
            val pass = binding.etPass.text.toString()
            val cameraIp = binding.etIp.text.toString()
            val espIp = binding.etEspIp.text.toString()

            prefs.edit()
                .putString("wifi", wifi)
                .putString("pass", pass)
                .putString("camera_ip", cameraIp)
                .putString("esp_ip", espIp)
                .apply()

            Toast.makeText(this, "Settings Saved Successfully", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}