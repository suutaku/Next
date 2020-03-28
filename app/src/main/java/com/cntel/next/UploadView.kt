package com.cntel.next

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import com.github.promeg.pinyinhelper.Pinyin
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class UploadView: AppCompatActivity() {
    val TAG = "UploadView"
    private var cameraLib = CameraLib(this)
    private lateinit var mLocationClient: LocationClient
    var sp: SharedPreferences? = null
    companion object {
        val RESULT_LOAD_IMAGE = 3//选择图片
        val RESULT_TAKE_PHOTO = 4//拍照
        val MESSAGE_PROCESS_START = 1
        var MESSAGE_UPLOAD_START = 2
        var MESSAGE_BLOCK_SUBMIT = 3
        var MESSAGE_PROGRESS_END = 4
    }

    var uploadURL: String? = null
    var blockCommitURL: String? = null
    var clusterReportURL: String? = null
    //var attachmentFileName = file_name_with_ext;
    var crlf = "\r\n";
    var twoHyphens = "--";
    var boundary =  "*****";
    var imageBuffer : Bitmap? = null
    var feedbackBuffer: JSONObject? = null
    private var UIHandler : Handler? = null
    private var bundle :Bundle? = null
    private val myLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(p0: BDLocation?) {
            // text_map.setText("lat:$lat, long:$lng")
            setAddress(p0!!.latitude,p0!!.longitude) {}

        }
    }
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_view)
        uploadURL = getString(R.string.storage_node)+"/api/v0/add"
        clusterReportURL = getString(R.string.cluster_server)+"/pins/"
        blockCommitURL = getString(R.string.chain_server)+"/broadcast_tx_commit?tx="

        SDKInitializer.initialize(applicationContext)
        //定位初始化
        mLocationClient = LocationClient(applicationContext)
        mLocationClient.registerLocationListener(myLocationListener)
        val option = LocationClientOption()
        option.coorType = "bd09ll"
        option.scanSpan = 0
        option.openGps = true
        mLocationClient.locOption = option
        mLocationClient.start()

        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        var menuButton = findViewById<Button>(R.id.menuButton)
        menuButton.setOnClickListener {
            var popMenu = PopupMenu(this,it)
            popMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.user_information -> {
                        Log.d(TAG,"item 1")
                        true
                    }
                    R.id.logout -> {
                        Log.d(TAG,"item 2")
                        sp=getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                        var editor=sp?.edit();
                        editor?.putBoolean("autoLogin", false);
                        editor?.commit();
                        val intent= Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        Log.d(TAG,"user logout")
                        true
                    }
                    R.id.upload -> {
//                        val intent= Intent(this,UploadView::class.java)
//                        startActivity(intent)
                        true
                    }
                    R.id.search -> {
                        val intent= Intent(this,SearchView::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popMenu.inflate(R.layout.menu_main)
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception){
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popMenu.show()
            }
        }



        var takePicture = findViewById<Button>(R.id.takePicture)
        takePicture.setOnClickListener {
            cameraLib.openCamera()

        }

        var loadImage = findViewById<Button>(R.id.loadImage)
        loadImage.setOnClickListener {
            cameraLib.loadImage()

        }


        supportActionBar?.hide()
        Log.d(TAG,"created")

    }


    private fun setAddress(lat: Double,lng: Double, callback: () -> Unit){
        intent.putExtra("LatFromMapSelf",lat)
        intent.putExtra("LngFromMapSelf",lng)
        var geoCoder = GeoCoder.newInstance()
        var op = ReverseGeoCodeOption()
        op.location(LatLng(lat,lng))
        val listener: OnGetGeoCoderResultListener = object : OnGetGeoCoderResultListener {
            // 反地理编码查询结果回调函数
            override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) { // 没有检测到结果
                    Log.e(TAG,result.toString())
                }else{
                    intent.putExtra("AddressFromMapSelf",result.address)
                    intent.putExtra("AddressFromMapProvinceSelf",
                        Pinyin.toPinyin(result.addressDetail.province,"").toLowerCase())
                    intent.putExtra("AddressFromMapCitySelf",
                        Pinyin.toPinyin(result.addressDetail.city,"").toLowerCase())
                    intent.putExtra("AddressFromMapAreaSelf",
                        Pinyin.toPinyin(result.addressDetail.district,"").toLowerCase())
                    intent.putExtra("AddressFromMapStreetSelf",
                        Pinyin.toPinyin(result.addressDetail.street,"").toLowerCase())
                    intent.putExtra("AddressFromMapNumberSelf",
                        Pinyin.toPinyin(result.addressDetail.streetNumber,"").toLowerCase())
                    callback()
                    Log.d(TAG,result.address+" $lat + $lng")
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
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun constructConfirmInfo(act: AlertDialog){
        var confirmUserName = act.findViewById<TextView>(R.id.confirmUserName)
        var confirmAddress = act.findViewById<TextView>(R.id.confirmAddress)
        var confirmLatlng = act.findViewById<TextView>(R.id.confirmLatlng)
        var confirmDate = act.findViewById<TextView>(R.id.confirmDate)
        var confirmPermission = act.findViewById<TextView>(R.id.confirmPermission)
        fun makeSpinner(id: Int, layoutID: Int, arrayID: Int) : Spinner?{

            var tmp = act.findViewById<Spinner>(id)
            ArrayAdapter.createFromResource(
                this,
                arrayID,
                layoutID
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                tmp!!.adapter = adapter
            }
            return tmp
        }
        var confirmSpinner = makeSpinner(R.id.confirmSpinner,R.layout.spinner_item_2,R.array.active_array)
        confirmSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                var selectValue = parent.getItemAtPosition(pos).toString()
                if(selectValue != "全部"){
                    selectValue = Pinyin.toPinyin(selectValue,"").toLowerCase()
                }
                intent.putExtra("ActiveTypeSelect", Pinyin.toPinyin(selectValue,"").toLowerCase())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        confirmSpinner!!.setSelection( 0,true);

        confirmUserName?.text =  intent.getStringExtra("UserName")
        confirmAddress?.text =  intent.getStringExtra("AddressFromMapSelf")
        confirmLatlng?.text =  intent.getDoubleExtra("LatFromMapSelf",-1.0).toString() +" "+ intent.getDoubleExtra("LngFromMapSelf",-1.0)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val current = simpleDateFormat.format(Date())
        confirmDate?.text = current.toString()
        confirmPermission?.text = "管理员"

        var cancelButton =  act.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelButton?.setOnClickListener {
            act.dismiss()
        }
        var editButton = act.getButton(DialogInterface.BUTTON_NEUTRAL)

        editButton.setOnClickListener {
            var dLog = AlertDialog.Builder(this)
            var dIn : AlertDialog? = null
            dLog.setTitle("修改信息")
            dLog.setView(R.layout.edit_data)
            dLog.setCancelable(false)
            dLog.setPositiveButton("确认",
                DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->

                    var latStr = dIn?.findViewById<EditText>(R.id.fixLocation_2)?.text.toString()
                    var lngStr = dIn?.findViewById<EditText>(R.id.fixLocation_3)?.text.toString()
                    if(latStr != "" && lngStr != ""){
                        var lat = latStr.toDouble()
                        var lng = lngStr.toDouble()
                        Log.d(TAG,"Update location: $lat $lng")
                        setAddress(lat,lng) {
                            // makeConfirmDialog()
                            Log.d(TAG,"Update address in runnable")
                            constructConfirmInfo(act)
                            dialog.dismiss()
                        }
                    }


                    var dateStr =  dIn?.findViewById<EditText>(R.id.fixDate_2)?.text.toString()
                    if(dateStr != null){
                        intent.putExtra("DateSelect",dateStr)
                    }
                    return@OnClickListener
                })

            dLog.setNegativeButton("取消",
                DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    return@OnClickListener
                })

            dIn = dLog.show()
        }

        var sumitButton =  act.getButton(DialogInterface.BUTTON_POSITIVE)
        sumitButton?.setOnClickListener {
            Log.d(TAG,"sumitButton clicked")
            if(confirmAddress?.text == "") {
                confirmAddress.text = intent.getStringExtra("AddressFromMapSelf")
                Toast.makeText(
                    this@UploadView, "正在获取地址信息",
                    Toast.LENGTH_LONG
                ).show()
                sumitButton.text = "重试"
                return@setOnClickListener
            }

            if(confirmLatlng?.text == "") {
                confirmLatlng.text =  intent.getDoubleExtra("LatFromMapSelf",-1.0).toString() +" "+ intent.getDoubleExtra("LngFromMapSelf",-1.0)
                Toast.makeText(
                    this@UploadView, "正在获取定位信息",
                    Toast.LENGTH_LONG
                ).show()
                sumitButton.text = "重试"
                return@setOnClickListener
            }

            var dLog = AlertDialog.Builder(this)
            dLog.setTitle("确认信息")
            dLog.setCancelable(false)
            dLog.setMessage("数据准备中。。。")
            var dIn = dLog.show()

            bundle = Bundle()
            UIHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message?) {
                    if (msg == null) {
                        return
                    }
                    Log.d(TAG,"message come !!!!!")
                    if (msg.what == UploadView.MESSAGE_PROCESS_START) {
                        dIn.dismiss()
                        dLog.setMessage("数据处理中。。。")
                        dIn = dLog.show()
                    } else if (msg.what == UploadView.MESSAGE_UPLOAD_START) {
                        dIn.dismiss()
                        dLog.setMessage("开始上传。。。")
                        dIn = dLog.show()
                    } else if (msg.what == UploadView.MESSAGE_BLOCK_SUBMIT) {
                        dIn.dismiss()
                        dLog.setMessage("生成区块。。。")
                        dIn = dLog.show()
                    } else if (msg.what == UploadView.MESSAGE_PROGRESS_END) {
                        dIn.dismiss()
                        dLog.setMessage("上传完成！\nHash: "+feedbackBuffer?.getJSONObject("result")?.getString("hash")+"\nHeight: "+feedbackBuffer?.getJSONObject("result")?.getString("height"))
                        dLog.setPositiveButton("确定",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                return@OnClickListener
                            })
                        dIn = dLog.show()
                    } else {
                        dIn.dismiss()
                        dLog.setMessage("错误！！！")
                        dLog.setPositiveButton("确定",
                            DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                                return@OnClickListener
                            })
                        dIn = dLog.show()
                    }
                }
            }
            Thread {
                Log.d(TAG,"run main process")
                confirmedAndSubmit()
            }.start()
            act.dismiss()


        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun makeConfirmDialog(): AlertDialog {
        var resultDialog = AlertDialog.Builder(this)
        resultDialog.setTitle("详细信息")
        resultDialog.setView(R.layout.info_confirm_view)
        resultDialog.setPositiveButton("确定", null)
        resultDialog.setNeutralButton("修改",null)
        resultDialog.setNegativeButton("取消",null)
        var viewTmp = resultDialog.show()
        constructConfirmInfo(viewTmp)
        return viewTmp
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG,"onActivityResult called")
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode ){
            UploadView.RESULT_LOAD_IMAGE ->{
                if(resultCode != RESULT_OK){
                    Log.e(TAG,"RESULT_TAKE_PHOTO NOT OK")
                    return
                }
                var viewTmp = makeConfirmDialog()
                var inputStream: InputStream? = contentResolver.openInputStream(data!!.data)
                    ?: return
                imageBuffer = BitmapFactory.decodeStream(inputStream)
                var imageView = viewTmp.findViewById<ImageView>(R.id.imageDisplay)
                imageView?.setImageBitmap(imageBuffer)
            }
            UploadView.RESULT_TAKE_PHOTO ->{

                if(resultCode != RESULT_OK){
                    Log.e(TAG,"RESULT_TAKE_PHOTO NOT OK")
                    return
                }
                var viewTmp = makeConfirmDialog()
                Log.d(TAG,"Call back called")
                var inputStream: InputStream? = contentResolver.openInputStream(cameraLib.getUri() as Uri)
                    ?: return
                imageBuffer = BitmapFactory.decodeStream(inputStream)
                var imageView = viewTmp.findViewById<ImageView>(R.id.imageDisplay)
                imageView?.setImageBitmap(imageBuffer)

            }
            else ->{
                Log.e(TAG,"123")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmedAndSubmit(){
        Log.d(TAG,"confirmedAndSubmit")
        var msg = Message()
        msg.what = UploadView.MESSAGE_PROCESS_START
        UIHandler?.sendMessage(msg)
        //get datas
        var msg2 = Message()
        msg2.what = UploadView.MESSAGE_UPLOAD_START
        UIHandler?.sendMessage(msg2)
        //upload images
        //report cluster
        //report detection server
        var byteArrayOutputStream =  ByteArrayOutputStream()
        imageBuffer?.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        var fileHash = doUpload(byteArrayOutputStream.toByteArray())

        var resized = Bitmap.createScaledBitmap(imageBuffer, imageBuffer!!.width/8, imageBuffer!!.height/8, true)
        if(resized.width < resized.height){
            var matrix = Matrix();
            matrix.postRotate(90F);
            resized = Bitmap.createBitmap(resized, 0, 0, resized.width, resized.height, matrix, true);
        }
        var byteArrayOutputStream2 =  ByteArrayOutputStream()
        resized?.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream2)
        var previewHash = doUpload(byteArrayOutputStream2.toByteArray())
        byteArrayOutputStream.flush()
        byteArrayOutputStream2.flush()
        imageBuffer?.recycle()
        imageBuffer = null
        resized?.recycle()
        System.gc();
        var msg3 = Message()
        msg3.what = UploadView.MESSAGE_BLOCK_SUBMIT
        UIHandler?.sendMessage(msg3)
        //submit block
        feedbackBuffer = submitTransaction(fileHash,previewHash)
        var msg4 = Message()
        msg4.what = UploadView.MESSAGE_PROGRESS_END
        UIHandler?.sendMessage(msg4)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun doUpload(data: ByteArray) :String{
        var url = URL(uploadURL)
        var connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 300000
        connection.doOutput = true
        connection.doInput = true
        connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ")
        connection.setRequestProperty("Accept","*/*");
        //connection.setRequestProperty("Content-lenght", ""+ (imageBuffer?.size ?: 0))
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
        try {
            connection.connect()
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                    ".jpg" + "\";filename=.jpg\"" +
                    ".jpg" + "\"" + this.crlf);
            outputStream.writeBytes(this.crlf);
            outputStream.write(data)
            Log.i(TAG,"write ${data?.size} bytes")
            outputStream.flush()
        }catch (exception: Exception) {
            throw Exception("Exception ${exception.message}")
        }
        if (connection.responseCode != HttpURLConnection.HTTP_OK && connection.responseCode != HttpURLConnection.HTTP_CREATED) {
            try {


                val reader = BufferedReader(InputStreamReader(connection.errorStream))
                val output: String = reader.readText()

                println("There was error while connecting the chat $output")


            } catch (exception: Exception) {
                throw Exception("Exception while push the notification  $exception.message")
            }
            return ""
        }else{
            val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            var tmp = reader.readText()
            var jObj = JSONObject(tmp)
            Log.i(TAG,tmp)
            // submitTransaction(jObj.getString("Hash"))
            reportCluster(jObj.getString("Hash"))
            return jObj.getString("Hash")
        }
    }
    private fun reportCluster(hash: String){
        val serverURL: String = clusterReportURL+hash
        Log.i(TAG,serverURL)
        val url = URL(serverURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 300000
        connection.connectTimeout = 300000
        connection.doOutput = true
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
            var jObj = JSONObject(tmp)
            Log.i(TAG,tmp)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun submitTransaction(fileHash: String, previewHash: String) : JSONObject? {
        var postObj = JSONObject()
        postObj.put("FileHash",fileHash)
        postObj.put("FilePreviewHash",previewHash)
        postObj.put("Type","Transaction")
        var tmpLI = JSONObject()
        tmpLI.put("Latitude",intent.getDoubleExtra("LatFromMapSelf",-1.0))
        tmpLI.put("Longitude",intent.getDoubleExtra("LngFromMapSelf",-1.0))
        tmpLI.put("Province",intent.getStringExtra("AddressFromMapProvinceSelf"))
        tmpLI.put("City",intent.getStringExtra("AddressFromMapCitySelf"))
        tmpLI.put("Area",intent.getStringExtra("AddressFromMapAreaSelf"))
        tmpLI.put("Street",intent.getStringExtra("AddressFromMapStreetSelf"))
        tmpLI.put("Number",intent.getStringExtra("AddressFromMapNumberSelf"))
        postObj.put("Location",tmpLI)
        postObj.put("User",intent.getStringExtra("UserName"))
        var dateStr = intent.getStringExtra("DateSelect")
        if(dateStr != null && dateStr != ""){
            postObj.put("Date", dateStr)
            intent.putExtra("DateSelect","")
        }else{
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val current = simpleDateFormat.format(Date())
            postObj.put("Date", current.toString())
        }
        postObj.put("TransactionTag",intent.getStringExtra("ActiveTypeSelect"))
        postObj.put("Channel","mobile")
        postObj.put("FileType","image")
        var reqString = postObj.toString()
        Log.i(TAG,reqString)
        var b64 = Base64.encodeToString(reqString.toByteArray(), Base64.DEFAULT)

        val serverURL: String = blockCommitURL+"\"$b64\""
        val url = URL(serverURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 300000
        connection.connectTimeout = 300000
        connection.doOutput = true
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
            var jObj = JSONObject(tmp)
            Log.i(TAG,tmp)
            return jObj
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        imageBuffer?.recycle()
        imageBuffer = null
    }
}