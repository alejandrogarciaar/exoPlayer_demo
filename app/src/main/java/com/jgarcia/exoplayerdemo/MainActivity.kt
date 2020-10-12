package com.jgarcia.exoplayerdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jgarcia.exoplayerdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setListeners()
    }

    private fun setListeners() {
        activityMainBinding.btnGoToDefaultStreaming.setOnClickListener {
            startActivity(Intent(this, LiveStreamingExoPlayerActivity::class.java))
        }
        activityMainBinding.btnGoToCustomStreamingView.setOnClickListener {
            //startActivity(Intent(this, ))
        }
    }
}