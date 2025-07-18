package com.example.wifiattendance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(WifiReceiver(), filter)

        val file = File(getExternalFilesDir(null), "attendance_log.txt")
        if (file.exists()) {
            textView.text = file.readText()
        }
    }

    inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid.replace("", "")
            val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val log = if (ssid == "YourOfficeWiFi") "เข้าออฟฟิศ: $time" else "ออกออฟฟิศ: $time"
            val file = File(getExternalFilesDir(null), "attendance_log.txt")
            file.appendText("$log\n")
            textView.text = file.readText()
        }
    }
}