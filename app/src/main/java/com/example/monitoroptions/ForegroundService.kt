package com.example.monitoroptions

import android.app.*
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean



class ForegroundService : Service() {

    private var worker: Thread = Thread()
    private var running: AtomicBoolean = AtomicBoolean(false)
    private val CHANNEL_ID = "ForegroundService Kotlin"
    private var ringPlayerFS: SoundManager? = null
    private var optionValue: NSEOptionData? = null



    companion object {
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        const val ACTION_PLAY_SOUND = "ACTION_PLAY_SOUND"
        const val ACTION_STOP_SOUND = "ACTION_STOP_SOUND"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent != null) {
            val action = intent.action;
            when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    if (ringPlayerFS == null) {
                        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ringPlayerFS = SoundManager(RingtoneManager.getRingtone(applicationContext, notification));
                    }
                    Toast.makeText(
                        applicationContext,
                        "Foreground service is started.",
                        Toast.LENGTH_LONG
                    ).show()
                    optionValue = NSEOptionData()
                    var dbHelperObj = DBHelper(this, null);
                    optionValue = intent.getSerializableExtra("optionData") as NSEOptionData
                    if ( optionValue != null) {
                        dbHelperObj.addUpdateOptionData(optionValue!!)
                        startForegroundServices()
                    }
                }
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    ringPlayerFS?.stopRingtone();
                    Toast.makeText(
                        applicationContext,
                        "Foreground service is stopped.",
                        Toast.LENGTH_LONG
                    ).show()
                    stopForegroundServices()
                }
                ACTION_PLAY_SOUND -> {
                    ringPlayerFS?.playRingtone();
                    Toast.makeText(
                        applicationContext,
                        "Playing ringtone",
                        Toast.LENGTH_LONG
                    ).show()
                }
                ACTION_STOP_SOUND -> {
                    ringPlayerFS?.stopRingtone();
                    Toast.makeText(
                        applicationContext,
                        "Ringtone stopped",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)

    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun  startForegroundServices() {
        println("Service started")
        var optionDataResp = CEAndPE()
        var input = ""
        writeToLog("Started...")
        var i=0
        if(!running.get()) {
            running.set(true)
            worker = Thread {
                while (running.get()) {
                    println("Service is running...")
                    i++
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        input = "Last Run ${
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))
                        }"
                    }
                    else
                    {
                        input = "Last run count $i"
                    }
                    writeToLog(input)
                    val mNotificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.notify(1, getMyActivityNotification(optionDataResp))
                    try {
                        optionDataResp = pollNSEOptionChain(optionDataResp)
                        Thread.sleep(10000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
            worker.start()

            createNotificationChannel()

            startForeground(1, getMyActivityNotification(optionDataResp))
        }




    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pollNSEOptionChain(prevData: CEAndPE) : CEAndPE {
        val optionData = prevData

        try {
            val url = URL("https://www.nseindia.com/api/option-chain-indices?symbol=NIFTY")
            val response = StringBuffer()
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                writeToLog("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()

                }
            }
            if (response != null) {
                val EntryPriceCE = optionValue!!.ce_price.toDouble()
                val EntryPricePE = optionValue!!.pe_price.toDouble()
                val ExistingPorL = optionValue!!.previous_profit.toDouble()
                val optCEFilter = "OPTIDXNIFTY" + optionValue!!.expiry + "CE" + optionValue!!.ce_strike + ".00"
                val optPEFilter = "OPTIDXNIFTY" + optionValue!!.expiry + "PE" + optionValue!!.pe_strike + ".00"
                var stringResp = response.toString()
                optionData.lastPriceCE = GetLastPrice(stringResp, optCEFilter)
                optionData.lastPricePE = GetLastPrice(stringResp, optPEFilter)
                var currentPorL = ((EntryPricePE - optionData.lastPricePE) + ( EntryPriceCE - optionData.lastPriceCE)) * 50
                optionData.PorL = currentPorL + ExistingPorL
                writeToLog("Data lastPricePE:  ${optionData.lastPricePE}" +
                        "lastPriceCE ${optionData.lastPriceCE}" +
                        "currentPorL ${currentPorL}" +
                        "optionData.PorL ${optionData.PorL}")
                if (optionValue!!.alert.toDoubleOrNull() != null &&
                    optionValue!!.alert.toDouble() > 0 &&
                    optionData.PorL > optionValue!!.alert.toDouble()) {
                    ringPlayerFS?.playRingtone();
                }
            }


            /* Sample for accessing full record
            if (response != null) {
                var stringResp = response.toString()
                var temp = stringResp.indexOf("filtered")
                var simpleResponse =  "{\""
                simpleResponse += stringResp.substring(temp)
                var obj = JSONObject(simpleResponse)
                writeToLog("Success")
            }*/

        }
        catch (e: IOException) {
            writeToLog("Error : ${e.toString()}")
        }
        return optionData

    }



    private fun GetLastPrice(stringResp: String, filter: String ) : Double {
        var lastPrice : String = "0"
        try {
            var startIndex = stringResp.indexOf(filter)
            var lastPriceStartIndex = stringResp.indexOf("lastPrice", startIndex)
            var lastPriceEndIndex = stringResp.indexOf(",", lastPriceStartIndex)
            var lastPriceString = stringResp.substring(lastPriceStartIndex, lastPriceEndIndex)
            lastPrice = lastPriceString.split(":")[1]

        }  catch (e: IOException) {
            writeToLog("Error : ${e.toString()}")
        }
        return  lastPrice.toDouble()
    }






    private fun writeToLog(data: String) {
        try {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val FileName =
                     "Log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")) + ".txt"
                 var myExternalFile = File(getExternalFilesDir("Logs"), FileName)
                 myExternalFile.appendText(data)
             }
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }




    private fun getMyActivityNotification(notificationData: CEAndPE): Notification? {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val contentView = RemoteViews(packageName, R.layout.notification_layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           var temp  = "Last Run ${
                LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))
            }"
            contentView.setTextViewText(R.id.tvTitle, "Current P And L : " + notificationData.PorL)
            contentView.setTextViewText(R.id.tvDesc, "CE : ${notificationData.lastPriceCE} PE : ${notificationData.lastPricePE} $temp")
        }


        return  NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service Kotlin Example")
            .setCustomContentView(contentView)
            .setContent(contentView)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun stopForegroundServices() {
        ringPlayerFS?.stopRingtone();
        running.set(false)
        worker.interrupt()
        println("stopForegroundService")
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()

    }



    override fun onTaskRemoved(rootIntent: Intent?) {

        println("onTaskRemoved")
    }

    override fun onDestroy() {
        stopForegroundServices()
        println("onDestroy")
    }
}