package com.example.fireapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.fireapp.databinding.ActivityStatusBinding
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private val client = OkHttpClient()

    private var lastAlertState = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 AUTO FETCH EVERY 3 SECONDS
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                fetchFromEsp()
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)

        binding.btnViewSensorData.setOnClickListener {
            startActivity(Intent(this, SensorDataActivity::class.java))
        }
    }

    // ================= FETCH FROM ESP32 =================
    private fun fetchFromEsp() {

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val espIp = prefs.getString("esp_ip", null)

        if (espIp.isNullOrEmpty()) {
            binding.statusValue.text = "ESP IP NOT SET"
            return
        }

        val url = "http://$espIp/sensor"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.statusValue.text = "ESP OFFLINE"
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                if (body != null) {
                    val json = JSONObject(body)

                    val temp = json.optString("temperature", "--")
                    val humidity = json.optString("humidity", "--")
                    val gas = json.optString("gas", "--")
                    val smoke = json.optString("smoke", "--")
                    val water = json.optString("water_level", "--")

                    runOnUiThread {

                        // ===== UI UPDATE =====
                        binding.tempValue.text = "Temperature: $temp°C"
                        binding.humidityValue.text = "Humidity: $humidity%"
                        binding.gasValue.text = "Gas Level: $gas"
                        binding.smokeValue.text = "Smoke Level: $smoke"
                        binding.watervalue.text = "Water Level: $water"

                        // ===== ALERT LOGIC =====
                        val tempF = temp.toFloatOrNull() ?: 0f
                        val gasI = gas.toIntOrNull() ?: 0
                        val smokeI = smoke.toIntOrNull() ?: 0

                        val state = when {
                            gasI > 700 || smokeI > 700 || tempF > 40 -> "HIGH"
                            gasI > 500 || smokeI > 500 || tempF > 35 -> "MODERATE"
                            else -> "SAFE"
                        }

                        updateSystemStatus(state)
                        updateRecommendation(state)

                        if (state != lastAlertState) {
                            if (state == "HIGH") {
                                showAlert("HIGH FIRE ALERT!")
                            } else if (state == "MODERATE") {
                                showAlert("WARNING!")
                            }
                            lastAlertState = state
                        }
                    }
                }
            }
        })
    }

    // ================= STATUS UI =================
    private fun updateSystemStatus(state: String) {
        when (state) {
            "SAFE" -> {
                binding.statusValue.text = "SAFE"
                binding.systemStatusCard.setBackgroundColor(getColor(R.color.safeGreen))
            }
            "MODERATE" -> {
                binding.statusValue.text = "WARNING"
                binding.systemStatusCard.setBackgroundColor(getColor(R.color.warningAmber))
            }
            "HIGH" -> {
                binding.statusValue.text = "ALERT"
                binding.systemStatusCard.setBackgroundColor(getColor(R.color.primaryRed))
            }
        }
    }

    private fun updateRecommendation(state: String) {
        val msg = when (state) {
            "SAFE" -> "System operating normally."
            "MODERATE" -> "Monitor environment closely."
            "HIGH" -> "Evacuate immediately!"
            else -> "Monitoring..."
        }

        binding.tvSeverityBottom.text = "Status: $state"
        binding.tvRecommendationBottom.text = msg
    }

    private fun showAlert(message: String) {
        Snackbar.make(binding.mainContent, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.primaryRed))
            .setTextColor(getColor(R.color.textWhite))
            .show()
    }
}