package com.cntel.next

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.search_result.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import android.util.Base64
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import com.github.promeg.pinyinhelper.Pinyin
import org.json.JSONArray
import java.text.SimpleDateFormat


class SmartScrollView: AppCompatActivity(){
    private val TAG = "SmartScrollView"
    private var host: String? = null
    private var fileServer: String? = null
    private lateinit var mRandom: Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable
    private var  mFilterValue: JSONObject? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_result)
        host = getString(R.string.test_chain_server)
        fileServer = getString(R.string.storage_node)
        var swip = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        // Initialize a new Random instance
        mRandom = Random()

        // Initialize the handler instance
        mHandler = Handler()
        mFilterValue = JSONObject(intent.getStringExtra("FilterValue"))

        var names = ArrayList<String>()
        var dataArray: ArrayList<JSONObject>? = null
        var indexArray: ArrayList<JSONObject>? = null
        fun updateIndex(): ArrayList<JSONObject>?{

            var searchKey: String? = null
            Log.d(TAG,"updateData")
            searchKey = constructSearchKeys(mFilterValue!!)
            var ret = search(searchKey!!)
            var tmp = ret?.let { filtering(it, mFilterValue!!) }
            Log.d(TAG,tmp.toString())
            if (tmp != null) {
                var index = 1
                tmp!!.forEach {
                    var longStr = it.getString("Hash")
                    var shortStr = longStr.substring(0,10)+ "*****" + longStr.substring(longStr.length - 10)
                    names.add("$index     $shortStr")
                    index++

                }
            }
            return tmp
        }
        fun updateData(options: JSONObject?): ArrayList<JSONObject>?{
            var searchKey: String? = null
            Log.d(TAG,"updateData")
            searchKey = options?.let { constructSearchKeys(it) }
            var ret = search(searchKey!!)
            var tmp = ret?.let { filtering(it, mFilterValue!!) }
            return tmp
        }

        indexArray = updateIndex()

        var  arrayAdapter = ArrayAdapter<String>(this,R.layout.list_view_layout,names)
        listView.adapter = arrayAdapter
        listView.setOnItemClickListener{ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            var index = i
            if ( indexArray?.get(i)?.getString("Type") == "INDEX"){
                var opts = mFilterValue
                opts!!.put("MainKey","Hash")
                opts!!.put("MainValue",indexArray?.get(i)?.getString("Hash"))
                dataArray = updateData(opts)
                if(dataArray?.size == 0){
                    return@setOnItemClickListener
                }
                // fix index
                index = 0
            }else{
                dataArray = indexArray
            }

            if(!dataArray?.get(index)?.has("Value")!!){
                return@setOnItemClickListener
            }

            var resultDialog = AlertDialog.Builder(this)
            resultDialog.setTitle("详细信息")
            resultDialog.setCancelable(false)
            resultDialog.setView(R.layout.result_detail)
            resultDialog.setPositiveButton("确定",
                DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    return@OnClickListener
                })
            var value = dataArray?.get(index)?.getString("Value")

            var Obj = JSONObject(value)
            // fill infomations
//            var viewTmp = resultDialog as AlertDialog
            var viewTmp = resultDialog.show()
            var preview =  viewTmp.findViewById<ImageView>(R.id.preveiw)
            var url = URL(fileServer+"/api/v0/cat?arg="+Obj.getString("FilePreviewHash"))
            var image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            preview?.setImageBitmap(image)
            var userName = viewTmp.findViewById<TextView>(R.id.user_detai2)
            userName?.text = Obj.getString("User")
            var channel = viewTmp.findViewById<TextView>(R.id.data_channel_detai2)
            channel?.text = Obj.getString("Channel")
            var location = viewTmp.findViewById<TextView>(R.id.location_detail2)
            var lat = Obj.getJSONObject("Location").getDouble("Latitude")
            var lng = Obj.getJSONObject("Location").getDouble("Longitude")
            location?.text = lng.toString()+","+lat
            var address = viewTmp.findViewById<TextView>(R.id.adress2)
            var geoCoder = GeoCoder.newInstance()
            var op = ReverseGeoCodeOption()
            op.location(LatLng(lat,lng))
            val listener: OnGetGeoCoderResultListener = object : OnGetGeoCoderResultListener {
                // 反地理编码查询结果回调函数
                override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult) {
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) { // 没有检测到结果
                        address?.text = "暂时无法获取地址"
                    }else{
                        address?.text = result.address
                    }

                }

                // 地理编码查询结果回调函数
                override fun onGetGeoCodeResult(result: GeoCodeResult) {
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) { // 没有检测到结果
                    }
                }
            }
            geoCoder.setOnGetGeoCodeResultListener(listener)
            geoCoder.reverseGeoCode(op)
            var baiduMap = viewTmp.findViewById<MapView>(R.id.bMapView2)?.map
            var mMapStatus = MapStatus.Builder()
                .target(LatLng(lat,lng))
                .zoom(18.0f)
                .build()
            var mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus)
            baiduMap?.animateMapStatus(mMapStatusUpdate)
            var opt = MarkerOptions().position(LatLng(lat,lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
            baiduMap?.addOverlay(opt)
            Log.d(TAG,"$lat           $lng")
        }
        // Set an on refresh listener for swipe refresh layout
        swip.setOnRefreshListener {
            // Initialize a new Runnable
            mRunnable = Runnable {
                // Update the text view text with a random number
//                text_view.text = "Refresh and get random number ${mRandom.nextInt(500)}"
//
//                // Change the text view text color with a random color
//                text_view.setTextColor(randomHSVColor())
                indexArray = updateIndex()
                // Hide swipe to refresh icon animation
                swip.isRefreshing = false
            }

            // Execute the task after specified time
            mHandler.postDelayed(
                mRunnable,
                (1000).toLong() // Delay 1 to 5 seconds
            )
        }
    }

    //{"Channel":"全部","Province":"全部","YearStart":"全部","YearEnd":"全部","ActiveType":"全部","MainKey":"用户名"}
    private var minHeight = 1
    private var maxHeight = 50
    private var pageIndex = 1
    private fun constructSearchKeys(input: JSONObject): String? {
        var searchKey: String? = null
        Log.d(TAG,input.toString())
        if(input.getString("MainKey") == "全部"){
            var activeKey = input.getString("ActiveType")
            if(activeKey != "全部"){
                if(activeKey == "党建"){
                    searchKey = "/tx_search?query=\"tx.transactionTag='dj'\"&page=$pageIndex&per_page=50"
                    pageIndex++
                }else if(activeKey == "打卡"){
                    searchKey = "/tx_search?query=\"tx.transactionTag='dk'\"&page=$pageIndex&per_page=50"
                    pageIndex++
                }else{

                }
                return searchKey
            }

            var channel = input.getString("Channel")
            if(channel != "全部"){
                if(channel == "mobile"){
                    searchKey = "/tx_search?query=\"tx.channel='mobile'\"&page=$pageIndex&per_page=50"
                    pageIndex++
                }else if(channel == "api"){
                    searchKey = "/tx_search?query=\"tx.channel='api'\"&page=$pageIndex&per_page=50"
                    pageIndex++
                }else{

                }
                return searchKey
            }
            var province = input.getString("Province")
            if(province != "全部" ){
                searchKey = "/tx_search?query=\"tx.province='$province'\"&page=$pageIndex&per_page=50"
                pageIndex++
                return searchKey
            }

            var city = input.getString("City")
            if(city != "全部" ){
                searchKey = "/tx_search?query=\"tx.province='$city'\"&page=$pageIndex&per_page=50"
                pageIndex++
                return searchKey
            }

            var area = input.getString("Area")
            if(area != "全部" ){

                searchKey = "/tx_search?query=\"tx.province='$area'\"&page=$pageIndex&per_page=50"
                pageIndex++
                return searchKey
            }

            // no filter option: seach all blockcian
            searchKey = "/blockchain?minHeight=${minHeight}&maxHeight=${maxHeight}"
            minHeight = maxHeight+1
            maxHeight = minHeight+50
            // has filter option: search with index key

        }else if(input.getString("MainKey") == "用户名"){
            searchKey = "/tx_search?query=\"tx.user='${input.getString("MainValue")}'\"&page=$pageIndex&per_page=50"
            pageIndex++
        }else{
            searchKey = "/block_by_hash?hash=0x${input.getString("MainValue")}"
        }
        return searchKey
    }

    private fun matchFilter(value:JSONObject,filter:JSONObject): Boolean{
        Log.d(TAG,value.toString())
        var chanel = filter.getString("Channel")
        var province = filter.getString("Province")
        var city = filter.getString("City")
        var area = filter.getString("Area")
        var yearStart = filter.getString("YearStart")
        var yearEnd = filter.getString("YearEnd")
        var monthStart = filter.getString("MonthStart")
        var monthEnd = filter.getString("MonthEnd")
        var dayStart = filter.getString("DayStart")
        var dayEnd = filter.getString("DayEnd")
        var activeType = filter.getString("ActiveType")
        if(chanel != "全部"){
            if(chanel != value.getString("Channel")){
                Log.d(TAG,"Dismatch channel: excepted $chanel, got ${value.getString("Channel")}")
                return false
            }
        }
        if(province != "全部"){
            if(province != value.getJSONObject("Location").getString("Province")){
                Log.d(TAG,"Dismatch Province: excepted $province, got ${value.getJSONObject("Location").getString("Province")}")
                return false
            }
        }
        if(city != "全部"){
            if(city != value.getJSONObject("Location").getString("City")){
                Log.d(TAG,"Dismatch City: excepted $city, got ${value.getJSONObject("Location").getString("City")}")
                return false
            }
        }
        if(area != "全部"){
            if(area != value.getJSONObject("Location").getString("Area")){
                Log.d(TAG,"Dismatch Area: excepted $area, got ${value.getJSONObject("Location").getString("Area")}")
                return false
            }
        }
        if(activeType != "全部"){
            if(activeType != value.getString("TransactionTag")){
                Log.d(TAG,"Dismatch ActiveType: excepted $activeType, got ${value.getString("TransactionTag")}")
                return false
            }
        }
        if(yearStart != "全部"){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val current = simpleDateFormat.parse(value.getString("Date"))
            //make date string
            if(monthStart == "全部"){
                monthStart = "01"
            }
            if(dayStart == "全部"){
                dayStart = "01"
            }

            var dateStr = "$yearStart-$monthStart-$dayStart 00:00:00"
            var start =  simpleDateFormat.parse(dateStr)
            if(current < start){
                Log.d(TAG,"Dismatch Date after: excepted $dateStr, got ${value.getString("Date")}")
                return false
            }
        }
        if(yearEnd != "全部"){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val current = simpleDateFormat.parse(value.getString("Date"))
            if(monthEnd == "全部"){
                monthEnd = "12"
            }
            if(dayEnd == "全部"){
                dayEnd = "31"
            }
            var dateStr = "$yearEnd-$monthEnd-$dayEnd 24:59:59"
            var end =  simpleDateFormat.parse(dateStr)
            if(current > end){
                Log.d(TAG,"Dismatch Date befor: excepted $dateStr, got ${value.getString("Date")}")
                return false
            }
        }

        return true
    }

    private fun filtering(input: JSONObject,filter:JSONObject): ArrayList<JSONObject>? {
        if(input == null){
            return null
        }

        var arry = ArrayList<JSONObject>()
        if(filter.getString("MainKey") == "全部"){
            if(input.has("txs")){
                var tmp = input.getJSONArray("txs")
                for(i in 0 until tmp.length()){
                    var hash = tmp.getJSONObject(i).getString("hash")
                    var item = JSONObject()
                    item.put("Type","INFO")
                    item.put("Hash",hash)
                    var value = tmp.getJSONObject(i).getString("tx")
                    var test = Base64.decode(value,Base64.DEFAULT).toString(Charsets.UTF_8)
                    test = Base64.decode(test,Base64.DEFAULT).toString(Charsets.UTF_8)
                    if(matchFilter(JSONObject(test),filter)){
                        item.put("Value",test)
                        arry.add(item)
                    }
                }
            }else{
                var tmp = input.getJSONArray("block_metas")
                for(i in 0 until tmp.length()){
                    var hash = tmp.getJSONObject(i).getJSONObject("block_id").getString("hash")
                    var item = JSONObject()
                    item.put("Type","INDEX")
                    item.put("Hash",hash)
                    arry.add(item)
                }
            }

        }else if(filter.getString("MainKey") == "用户名"){
            var tmp = input.getJSONArray("txs")
            for(i in 0 until tmp.length()){
                var hash = tmp.getJSONObject(i).getString("hash")
                var item = JSONObject()
                item.put("Type","INFO")
                item.put("Hash",hash)

                item.put("Attributes",tmp.getJSONObject(i).getJSONObject("tx_result").getJSONArray("events").getJSONObject(0).getJSONArray("attributes"))
                var value = tmp.getJSONObject(i).getString("tx")
                var test = Base64.decode(value,Base64.DEFAULT).toString(Charsets.UTF_8)
                test = Base64.decode(test,Base64.DEFAULT).toString(Charsets.UTF_8)

                if(matchFilter(JSONObject(test),filter)){
                    item.put("Value",test)
                    arry.add(item)
                }

            }
        }else{
            // no specifical filter option: search all block chain
            var tmp = input.getJSONObject("block")
            var hash = input.getJSONObject("block_id").getString("hash")

            if(tmp.getJSONObject("data").get("txs").toString() == "null"){
                return arry
            }
            Log.d(TAG,tmp.getJSONObject("data").get("txs").toString())
            var item = JSONObject()
            item.put("Type","INFO")
            item.put("Hash",hash)
            var value = tmp.getJSONObject("data").getJSONArray("txs").getString(0)
            var test = Base64.decode(value,Base64.DEFAULT).toString(Charsets.UTF_8)
            test = Base64.decode(test,Base64.DEFAULT).toString(Charsets.UTF_8)
            if(matchFilter(JSONObject(test),filter)){
                item.put("Value",test)
                arry.add(item)
            }

        }
        return arry
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
        if(jObj!!.has("result")){
            return jObj!!.getJSONObject("result")
        }else{
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"Destroy")
        minHeight = 1
        maxHeight = 50
        pageIndex = 1
    }
}

