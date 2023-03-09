
package com.android.monitoroptions

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import java.io.Serializable;
import android.widget.EditText
import android.widget.Toast
import java.util.*


class MainActivity : AppCompatActivity(),  View.OnClickListener, Serializable  {

    // declaring objects of Button class
    private var startService: Button? = null
    private var stopService: Button? = null
    private var stopSound: Button? = null
    private var startSound: Button? = null
    private var resetOptions: Button? = null
    private var optionData : NSEOptionData? = null;
    private var dbHelperObj: DBHelper? = null;


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
        resetOptions = findViewById<View>(R.id.resetOptions) as Button

        // declaring listeners for the
        // buttons to make them respond
        // correctly according to the process
        startService!!.setOnClickListener(this)
        stopService!!.setOnClickListener(this)
        stopSound!!.setOnClickListener(this)
        startSound!!.setOnClickListener(this)
        resetOptions!!.setOnClickListener(this)

        if (!ForegroundService.ISRUNNING) {
            enableDisableEditText(true)
        }


        dbHelperObj  = DBHelper(this, null);
        optionData = dbHelperObj!!.readOptionData();
        if (optionData != null) {
            findViewById<EditText>(R.id.cePrice).setText(optionData!!.ce_price)
            findViewById<EditText>(R.id.pePrice).setText(optionData!!.pe_price)
            findViewById<EditText>(R.id.ceStrike).setText(optionData!!.ce_strike)
            findViewById<EditText>(R.id.peStrike).setText(optionData!!.pe_strike)
            findViewById<EditText>(R.id.expiry).setText(optionData!!.expiry)
            findViewById<EditText>(R.id.alert).setText(optionData!!.alert)
            findViewById<EditText>(R.id.previousProfit).setText(optionData!!.previous_profit)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View) {

        // process to be performed
        // if start button is clicked
        val intent = Intent(this@MainActivity, ForegroundService::class.java)

        when {
            view === startService -> {
                intent.action = ForegroundService.ACTION_START_FOREGROUND_SERVICE


                optionData = NSEOptionData()
                optionData!!.ce_price = findViewById<EditText>(R.id.cePrice).text.toString()
                optionData!!.pe_price = findViewById<EditText>(R.id.pePrice).text.toString()
                optionData!!.ce_strike = findViewById<EditText>(R.id.ceStrike).text.toString()
                optionData!!.pe_strike = findViewById<EditText>(R.id.peStrike).text.toString()
                optionData!!.expiry = findViewById<EditText>(R.id.expiry).text.toString()
                optionData!!.alert = findViewById<EditText>(R.id.alert).text.toString()
                optionData!!.previous_profit = findViewById<EditText>(R.id.previousProfit).text.toString()

                if (
                    !isValidInput(optionData!!.ce_price) ||
                    !isParseDouble(optionData!!.ce_price) ||
                    !isValidInput(optionData!!.pe_price) ||
                    !isParseDouble(optionData!!.pe_price) ||
                    !isValidInput(optionData!!.ce_strike) ||
                    !isValidInput(optionData!!.pe_strike) ||
                    !isValidInput(optionData!!.expiry) ||
                    !isValidInput(optionData!!.alert) ||
                    !isValidInput(optionData!!.previous_profit) ||
                    !isParseDouble(optionData!!.previous_profit)
                )
                {
                    Toast.makeText(
                        applicationContext,
                        "Invalid Input",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    enableDisableEditText(false)
                    intent.putExtra("optionData", optionData)
                    // starting the service
                    startService(intent)
                }
            }

            // process to be performed
            // if stop button is clicked
            view === stopService -> {

                enableDisableEditText(true)

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
            view === resetOptions -> {
                dbHelperObj!!.deleteAll()
            }
        }
    }

    fun enableDisableEditText(flag: Boolean) {
        findViewById<EditText>(R.id.cePrice).isEnabled = flag
        findViewById<EditText>(R.id.pePrice).isEnabled = flag
        findViewById<EditText>(R.id.ceStrike).isEnabled = flag
        findViewById<EditText>(R.id.peStrike).isEnabled = flag
        findViewById<EditText>(R.id.expiry).isEnabled = flag
        findViewById<EditText>(R.id.alert).isEnabled = flag
        findViewById<EditText>(R.id.previousProfit).isEnabled = flag
    }

    fun isParseDouble(input : String?) : Boolean{
        if (input != null && input != "") {
            val parsedInt = input.toDoubleOrNull()
            if (parsedInt != null)
                return true;
        }
        return false;
    }

    fun isValidInput(input : String?) : Boolean{
        if (input != null && input != "") {
            return true
        }
        return false;
    }
}