package com.yumiapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

class MonitoringService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        
        startMonitoringLoop()
    }

    private fun startMonitoringLoop() {
        scope.launch {
            while (isActive) {
                // Contoh hardcode untuk simulasi
                checkUrl("https://google.com", "Google", "Yuee")
                
                // Interval 60 detik
                delay(60_000)
            }
        }
    }

    private suspend fun checkUrl(url: String, name: String, owner: String) {
        val startTime = System.currentTimeMillis()
        var status = "DOWN"
        var sslStatus = "UNKNOWN"
        var responseTime = 0L

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            responseTime = System.currentTimeMillis() - startTime
            status = if (response.isSuccessful) "ONLINE" else "ERROR"
            
            // Cek SSL
            val certs = response.handshake?.peerCertificates
            if (!certs.isNullOrEmpty()) {
                val cert = certs[0] as X509Certificate
                val daysLeft = (cert.notAfter.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
                sslStatus = if (daysLeft > 0) "VALID ($daysLeft hari)" else "EXPIRED"
            }
            
            response.close()
        } catch (e: IOException) {
            status = "DOWN"
        }

        // Kirim Notifikasi Telegram
        sendTelegramNotification(status, name, url, responseTime, sslStatus, owner)
    }

    private fun sendTelegramNotification(status: String, name: String, url: String, responseTime: Long, sslStatus: String, owner: String) {
        // GANTI DENGAN TOKEN DAN CHAT ID ANDA
        val token = "YOUR_BOT_TOKEN"
        val chatId = "YOUR_CHAT_ID"
        
        if (token == "YOUR_BOT_TOKEN") return // Skip jika belum diatur

        val emoji = when(status) {
            "ONLINE" -> "🟢"
            "DOWN" -> "🟤"
            "ERROR" -> "🔴"
            else -> "🔵"
        }

        var message = "<b>🧾 YUMI MONITORING INVOICE</b>\n" +
                "━━━━━━━━━━━━━━━━━━\n" +
                "<b>🏷️ Target:</b> $name\n" +
                "<b>🔗 URL:</b> $url\n" +
                "<b>📡 Status:</b> $emoji $status\n" +
                "<b>⏱️ Ping:</b> ${responseTime}ms\n" +
                "<b>🔒 SSL:</b> $sslStatus\n" +
                "━━━━━━━━━━━━━━━━━━"

        if (status == "DOWN" || status == "ERROR") {
            message += "\n\n⚠️ <b>ATTENTION $owner!</b>\nSistem mendeteksi anomali pada URL Anda. Segera lakukan pengecekan server!"
        } else {
            message += "\n\n✅ Sistem berjalan normal."
        }

        message += "\n\n<i>⚡ Powered by YumeePanels</i>"

        // Format JSON untuk dikirim ke API Telegram
        val json = """{"chat_id": "$chatId", "text": "$message", "parse_mode": "HTML"}"""
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$token/sendMessage")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "YUMI_CHANNEL",
                "YUMI Background Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "YUMI_CHANNEL")
            .setContentTitle("YUMI APP")
            .setContentText("Monitoring is running in the background (24/7)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
