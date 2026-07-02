package com.example.fireapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SensorDataActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var txtTemp: TextView
    private lateinit var txtSmoke: TextView
    private lateinit var txtGas: TextView
    private lateinit var txtHumidity: TextView

    private lateinit var btnSettings: Button

    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_data)

        // ================= WEBVIEW =================
        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.webViewClient = WebViewClient()

        txtTemp = findViewById(R.id.txtTemp)
        txtSmoke = findViewById(R.id.txtSmoke)
        txtGas = findViewById(R.id.txtGas)
        txtHumidity = findViewById(R.id.txtHumidity)

        btnSettings = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // ================= LOAD IP =================
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val espIp = prefs.getString("esp_ip", "") ?: ""

        val cameraIp = prefs.getString("camera_ip", "") ?: ""

        if (cameraIp.isNotEmpty()) {
            webView.loadUrl("http://$cameraIp:5000/video")
        }

        // ================= LIVE SENSOR FETCH =================
        startLiveFetch(espIp)
    }

    // ================= FETCH FROM ESP =================
    private fun startLiveFetch(ip: String) {

        if (ip.isEmpty()) return

        val runnable = object : Runnable {
            override fun run() {
                fetchSensorData(ip)
                handler.postDelayed(this, 3000) // every 3 sec
            }
        }

        handler.post(runnable)
    }

    private fun fetchSensorData(ip: String) {

        val url = "http://$ip/sensor"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    txtTemp.text = "ESP OFFLINE"
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                if (body != null) {
                    val json = JSONObject(body)

                    val temp = json.optString("temperature", "--")
                    val smoke = json.optString("smoke", "--")
                    val gas = json.optString("gas", "--")
                    val humidity = json.optString("humidity", "--")

                    runOnUiThread {

                        txtTemp.text = "Temperature: $temp°C"
                        txtSmoke.text = "Smoke: $smoke%"
                        txtGas.text = "Gas: $gas%"
                        txtHumidity.text = "Humidity: $humidity%"
                    }
                }
            }
        })
    }

    // ================= BACK PRESS =================
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val cameraIp = prefs.getString("camera_ip", "") ?: ""

        if (cameraIp.isNotEmpty()) {
            webView.loadUrl("http://$cameraIp:5000/video")
        }
    }
}