package com.yumiapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<LinearLayout>(R.id.urlContainer)
        
        // Menambahkan data dummy untuk menampilkan UI Neon Robotik
        addUrlCard(container, "Yumee Main", "https://yumee.id", "ONLINE", "Owner1")
        addUrlCard(container, "Payment Gateway", "https://pay.yumee.id", "ERROR", "Yuee")
        addUrlCard(container, "Database Server", "https://db.yumee.id", "DOWN", "Admin")
        addUrlCard(container, "Storage", "https://cdn.yumee.id", "NORMAL", "System")

        // Memulai Background Service
        val serviceIntent = Intent(this, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun addUrlCard(container: LinearLayout, name: String, url: String, status: String, owner: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_url, container, false)
        
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvUrl = view.findViewById<TextView>(R.id.tvUrl)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val rotatingBorder = view.findViewById<View>(R.id.rotating_border)
        
        tvName.text = name
        tvUrl.text = url
        tvStatus.text = status
        
        val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink)
        
        when (status) {
            "ONLINE" -> {
                rotatingBorder.setBackgroundResource(R.drawable.grad_green)
                rotatingBorder.startAnimation(rotateAnim)
                tvStatus.setTextColor(Color.parseColor("#39FF14")) // Hijau Neon
            }
            "ERROR" -> {
                rotatingBorder.setBackgroundResource(R.drawable.grad_red)
                rotatingBorder.startAnimation(rotateAnim)
                view.startAnimation(blinkAnim) // Kelap kelip
                tvStatus.setTextColor(Color.parseColor("#FF003C")) // Merah Neon
            }
            "DOWN" -> {
                rotatingBorder.setBackgroundResource(R.drawable.grad_orange)
                rotatingBorder.startAnimation(rotateAnim)
                tvStatus.setTextColor(Color.parseColor("#D2691E")) // Orange Coklat
            }
            "NORMAL" -> {
                rotatingBorder.setBackgroundResource(R.drawable.grad_blue)
                rotatingBorder.startAnimation(rotateAnim)
                tvStatus.setTextColor(Color.parseColor("#00E5FF")) // Biru Neon
            }
        }
        
        container.addView(view)
    }
}
