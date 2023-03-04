
package com.example.monitoroptions

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import android.media.RingtoneManager
import java.io.Serializable;

import android.net.Uri
import java.util.*


class MainActivity : AppCompatActivity(),  View.OnClickListener, Serializable  {

    // declaring objects of Button class
    private var startService: Button? = null
    private var stopService: Button? = null
    private var stopSound: Button? = null
    private var startSound: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main)

        // assigning ID of startButton
        // to the object start
        startService = findViewById<View>(R.id.startService) as Button

        // assigning ID of stopButton
        // to the object stop
        stopService = findViewById<View>(R.id.stopService) as Button
        stopSound = findViewById<View>(R.id.stopSound) as Button
        startSound = findViewById<View>(R.id.startSound) as Button

        // declaring listeners for the
        // buttons to make them respond
        // correctly according to the process
        startService!!.setOnClickListener(this)
        stopService!!.setOnClickListener(this)
        stopSound!!.setOnClickListener(this)
        startSound!!.setOnClickListener(this)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View) {

        // process to be performed
        // if start button is clicked
        val intent = Intent(this@MainActivity, ForegroundService::class.java)

        when {
            view === startService -> {
                intent.action = ForegroundService.ACTION_START_FOREGROUND_SERVICE
                // starting the service
                startService(intent)

            }

            // process to be performed
            // if stop button is clicked
            view === stopService -> {

                intent.action = ForegroundService.ACTION_STOP_FOREGROUND_SERVICE
                // stopping the service
                stopService(Intent(applicationContext, ForegroundService::class.java))
            }
            view === stopSound -> {
                intent.action = ForegroundService.ACTION_STOP_SOUND
                // starting the service
                startService(intent)
            }
            view === startSound -> {
                intent.action = ForegroundService.ACTION_PLAY_SOUND
                // starting the service
                startService(intent)
            }
        }
    }
}