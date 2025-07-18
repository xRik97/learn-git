package com.example.wifiattendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // เช็คว่า permission ได้รับแล้วหรือยัง
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // ได้ permission แล้ว
            getCurrentSSID()
        } else {
            // ขอ permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    private fun getCurrentSSID() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid
        Log.d("WiFi", "Connected to SSID: $ssid")
    }

    // รับผล permission ที่ผู้ใช้ตอบ
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentSSID()
        }
    }
}


        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(WifiReceiver(), filter)

        val file = File(getExternalFilesDir(null), "attendance_log.txt")
        if (file.exists()) {
            textView.text = file.readText()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    != PackageManager.PERMISSION_GRANTED) {

    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        123)
}

    }

    inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val currentSSID = wifiInfo.ssid.replace("\"", "")
    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    val officeSSIDs = listOf("OfficeA", "OfficeB")
    val homeSSIDs = listOf("Home1", "Home2", "Home3", "Home4")

    val location = when {
        officeSSIDs.contains(currentSSID) -> "เข้าออฟฟิศ"
        homeSSIDs.contains(currentSSID) -> "ถึงบ้าน"
        else -> "ออกจาก Wi-Fi ที่รู้จัก"
    }

    // เรียกใช้ฟังก์ชันส่งไป Google Sheet
    sendToGoogleSheet(location, currentSSID)

    // Optional: บันทึกใน log file ในเครื่องด้วย
    val file = File(getExternalFilesDir(null), "attendance_log.txt")
    file.appendText("$location: $time ($currentSSID)\n")
    textView.text = file.readText()
}

    }
    fun sendToGoogleSheet(location: String, ssid: String) {
    val url = "https://script.google.com/macros/s/AKfycbzSxxu3yVtgabyN-OMJVwsva4I3oJAXkdBAlnVt65d2MbKmfIUvNeGY7yc5AWhUc6es/exec"
    val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    val json = """
        {
            "time": "$time",
            "location": "$location",
            "ssid": "$ssid"
        }
    """.trimIndent()

    Thread {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(json.toByteArray())
            conn.inputStream.bufferedReader().readText() // รับ response
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}

}