package com.example.wifiattendance

import android.content.*
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val officeSSIDs = listOf("Allsystem_office") // wifi office
    private val homeSSIDs = listOf("wonghan_2.4GHZ", "wonghan_5GHZ", "Wonghan_2.4GHZ_ext", "Wonghan_5GHZ_ext") // wifi home
    private var lastSSID: String? = null

    private val webhookUrl = "https://script.google.com/macros/s/AKfycbzSxxu3yVtgabyN-OMJVwsva4I3oJAXkdBAlnVt65d2MbKmfIUvNeGY7yc5AWhUc6es/exec" // <= ใส่ URL ของคุณ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Wi-Fi Attendance Logger กำลังทำงาน.."
        })

        registerReceiver(wifiReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiReceiver)
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkWifiAndSend()
        }
    }

    private fun checkWifiAndSend() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentSSID = wifiManager.connectionInfo?.ssid?.replace("\"", "") ?: return

        if (currentSSID == lastSSID) return // ยังอยู่ที่เดิม ไม่ต้องส่งซ้ำ

        val location = when (currentSSID) {
            in officeSSIDs -> "ออฟฟิศ"
            in homeSSIDs -> "บ้าน"
            else -> null
        }

        val status = when {
            lastSSID in officeSSIDs || lastSSID in homeSSIDs -> "ออก"
            location != null -> "เข้า"
            else -> null
        }

        if (location != null || status == "ออก") {
            sendToGoogleSheets(location ?: "ไม่ทราบ", status ?: "ไม่ทราบ", currentSSID)
        }

        lastSSID = currentSSID
    }

    private fun sendToGoogleSheets(location: String, status: String, ssid: String) {
        val json = JSONObject().apply {
            put("location", location)
            put("status", status)
            put("ssid", ssid)
        }

        val request = Request.Builder()
            .url(webhookUrl)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        OkHttpClient().newCall(request).enqueue(responseCallback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WifiLogger", "ส่งข้อมูลล้มเหลว", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                Log.i("WifiLogger", "ส่งข้อมูลสำเร็จ: $bodyString")
            }
        })
    }
}
