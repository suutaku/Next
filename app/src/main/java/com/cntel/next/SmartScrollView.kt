package com.cntel.next

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import kotlinx.android.synthetic.main.search_result.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class SmartScrollView: AppCompatActivity(){
    private val TAG = "SmartScrollView"
    private val host = "http://192.168.0.180:26657"
    private lateinit var mRandom: Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable
    private var  mFilterValue: JSONObject? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_result)
        var swip = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        // Initialize a new Random instance
        mRandom = Random()

        // Initialize the handler instance
        mHandler = Handler()
        mFilterValue = JSONObject(intent.getStringExtra("FilterValue"))

        var searchKey = constructSearchKeys(mFilterValue!!)
        var ret = search(searchKey)
        text_view.text = ret.toString()
        // Set an on refresh listener for swipe refresh layout
        swip.setOnRefreshListener {
            // Initialize a new Runnable
            mRunnable = Runnable {
                // Update the text view text with a random number
                text_view.text = "Refresh and get random number ${mRandom.nextInt(500)}"

                // Change the text view text color with a random color
                text_view.setTextColor(randomHSVColor())

                // Hide swipe to refresh icon animation
                swip.isRefreshing = false
            }

            // Execute the task after specified time
            mHandler.postDelayed(
                mRunnable,
                (randomInRange(1,5)*1000).toLong() // Delay 1 to 5 seconds
            )
        }
    }



    // Custom method to generate random HSV color
    fun randomHSVColor(): Int {
        // Generate a random hue value between 0 to 360
        val hue = mRandom.nextInt(361)
        // We make the color depth full
        val saturation = 1.0f
        // We make a full bright color
        val value = 1.0f
        // We avoid color transparency
        val alpha = 255
        // Finally, generate the color
        // Return the color
        return Color.HSVToColor(alpha, floatArrayOf(hue.toFloat(), saturation, value))
    }


    // Custom method to get a random number from the provided range
    private fun randomInRange(min:Int, max:Int):Int{
        // Define a new Random class
        val r = Random()

        // Get the next random number within range
        // Including both minimum and maximum number
        return r.nextInt((max - min) + 1) + min;
    }



    //{"Channel":"全部","Province":"全部","YearStart":"全部","YearEnd":"全部","ActiveType":"全部","MainKey":"用户名"}
    private var minHeight = 1
    private var maxHeight = 50
    private var pageIndex = 1
    private fun constructSearchKeys(input: JSONObject): String{
        var searchKey: String? = null
        if(input.getString("MainKey") == "全部"){
            searchKey = "/blockchain?minHeight=${minHeight}&maxHeight=${maxHeight}"
            minHeight = maxHeight+1
            maxHeight = minHeight+50
        }else if(input.getString("MainKey") == "用户名"){
            searchKey = "/tx_search?\"tx.user='${input.getString("MainValue")}'\"&page=$pageIndex&per_page=50"
            pageIndex++
        }else{
            searchKey = "/block_by_hash?hash=${input.getString("MainValue")}"
        }
        return searchKey
    }

    private fun filtering(input: JSONObject,filter:JSONObject){
        if(input == null){
            return
        }
        if(input.getString("MainKey") == "全部"){

        }else if(input.getString("MainKey") == "用户名"){

        }else{

        }
    }

    private fun search(searchKey: String) : JSONObject? {

        val serverURL: String =  host+searchKey
        Log.i(TAG,serverURL)
        val url = URL(serverURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 300000
        connection.connectTimeout = 300000
        connection.doOutput = true
        var jObj: JSONObject? = null
        if (connection.responseCode != HttpURLConnection.HTTP_OK && connection.responseCode != HttpURLConnection.HTTP_CREATED) {
            try {
                val reader = BufferedReader(InputStreamReader(connection.errorStream))
                val output: String = reader.readText()
                println("There was error while connecting the chat $output")
            } catch (exception: Exception) {
                throw Exception("Exception while push the notification  $exception.message")
            }
        }else{
            val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            var tmp = reader.readText()
           jObj = JSONObject(tmp)
        }
        return jObj
    }
}