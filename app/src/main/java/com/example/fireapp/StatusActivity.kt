package com.example.fireapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
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
    private var lastSmsTime = 0L   // 🔥 spam control

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
            runOnUiThread {
                binding.statusValue.text = "ESP IP NOT SET"
            }
            return
        }

        val url = "http://$espIp/sensor"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.statusValue.text = "ESP OFFLINE"
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                if (body != null) {
                    try {
                        val json = JSONObject(body)

                        val temp = json.optString("temperature", "0")
                        val humidity = json.optString("humidity", "0")
                        val gas = json.optString("gas", "0")
                        val smoke = json.optString("smoke", "0")
                        val water = json.optString("water_level", "0")

                        runOnUiThread {

                            // UI UPDATE
                            binding.tempValue.text = "Temperature: $temp°C"
                            binding.humidityValue.text = "Humidity: $humidity%"
                            binding.gasValue.text = "Gas Level: $gas"
                            binding.smokeValue.text = "Smoke Level: $smoke"
                            binding.watervalue.text = "Water Level: $water"

                            val tempF = temp.toFloatOrNull() ?: 0f
                            val gasI = gas.toIntOrNull() ?: 0
                            val smokeI = smoke.toIntOrNull() ?: 0

                            val state = when {
                                gasI > 6000 || smokeI > 6000 || tempF > 40 -> "HIGH"
                                gasI > 5500 || smokeI > 5500 || tempF > 35 -> "MODERATE"
                                else -> "SAFE"
                            }

                            updateSystemStatus(state)
                            updateRecommendation(state)

                            // 🔥 ALERT + SMS
                            if (state != lastAlertState) {

                                when (state) {
                                    "HIGH" -> {
                                        showAlert("HIGH FIRE ALERT!")
                                        sendEmergencySms("Severe")
                                    }
                                    "MODERATE" -> {
                                        showAlert("WARNING!")
                                        sendEmergencySms("High")
                                    }
                                }

                                lastAlertState = state
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("JSON_ERROR", e.message ?: "JSON parse error")
                    }
                }
            }
        })
    }

    // ================= UI =================
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

    // ================= TWILIO SMS =================
    private fun sendEmergencySms(risk: String) {

        // 🔴 replace these
        val accountSid = "YOUR_ACCOUNT_SID"
        val authToken = "YOUR_AUTH_TOKEN"
        val from = "+19123014460"
        val to = "+923174117194"

        // 🔥 spam protection (1 min)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSmsTime < 60000) {
            Log.d("TWILIO", "SMS blocked (spam control)")
            return
        }
        lastSmsTime = currentTime

        val messageBody = if (risk == "Severe") {
            "CRITICAL FIRE ALERT! Evacuate immediately!"
        } else {
            "WARNING! Fire risk detected."
        }

        val url = "https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json"

        val formBody = FormBody.Builder()
            .add("To", to)
            .add("From", from)
            .add("Body", messageBody)
            .build()

        val credential = "$accountSid:$authToken"
        val authHeader = "Basic " + Base64.encodeToString(
            credential.toByteArray(),
            Base64.NO_WRAP
        )

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .addHeader("Authorization", authHeader)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("TWILIO_ERROR", e.message ?: "Request failed")
            }

            override fun onResponse(call: Call, response: Response) {

                val responseData = response.body?.string()

                if (response.isSuccessful) {
                    Log.d("TWILIO_SUCCESS", "SMS Sent")
                    Log.d("TWILIO_RESPONSE", responseData ?: "")
                } else {
                    Log.e("TWILIO_FAIL", "Code: ${response.code}")
                    Log.e("TWILIO_RESPONSE", responseData ?: "")
                }
            }
        })
    }
}