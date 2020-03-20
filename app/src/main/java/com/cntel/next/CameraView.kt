package com.cntel.next

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.net.URL
import java.net.HttpURLConnection
import android.util.Base64
import android.widget.*
import org.json.JSONArray

class CameraView : AppCompatActivity(){
    val TAG = "CameraView"
    private var cameraLib = CameraLib(this)
    private var MESSAGE_UPDATE_INFO_DISPLAY = 1
    private var MESSAGE_HIDE_INFO_DISPLAY = 2
    private var MESSAGE_SHOW_INFO_DISPLAY = 3
    private var MESSAGE_REST_VIEWS = 4
    private var MESSAGE_NOTIFICATION = 5
    var sp: SharedPreferences? = null
    private var imageUri :Uri ? = null
    private var locationValue :String ? = null
    private var locationManager : LocationManager? = null
    companion object {
        val RESULT_LOAD_IMAGE = 3//选择图片
        val RESULT_TAKE_PHOTO = 4//拍照
    }
    var submitButton :Button ? = null
    var logoutButton :Button ? = null
    var mainHost = "http://demo.cotnetwork.xyz"
    var uploadURL = "$mainHost:30001/api/v0/add"
    var blockCommitURL = "$mainHost:26657/broadcast_tx_commit?tx="
   // var blockCommitURL = "http://192.168.0.179:26657/broadcast_tx_commit?tx="
    var clusterReportURL = "$mainHost:30000/pins/"
    //var attachmentFileName = file_name_with_ext;
    var crlf = "\r\n";
    var twoHyphens = "--";
    var boundary =  "*****";
    var imageBuffer :Bitmap ? = null
    var locationBuffer = DoubleArray(2)
    var userNameBuffer: String? = null
    private var UIHandler :Handler? = null
    private var bundle :Bundle? = null

    private var newView: TextView? = null

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_view)
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
        // spinners
        var spManager = SpinnerManager(this)
        spManager.init()

        var searchButton = findViewById<ImageView>(R.id.searchButton)
        searchButton.setOnClickListener {
            Log.d(TAG,spManager.getFilterValue().toString())
        }

        var mapSelectorButton = findViewById<Button>(R.id.mapSelectorButton)
        mapSelectorButton.setOnClickListener {
            val intent= Intent(this,MapViewController::class.java)
            startActivity(intent)
        }
       // newView!!.isVisible = false
        bundle = Bundle()
        UIHandler = @SuppressLint("HandlerLeak")
        object : Handler(){
            override fun handleMessage(msg :Message?){
                if (msg == null){
                    return
                }
                if(msg.what == MESSAGE_UPDATE_INFO_DISPLAY){
                    newView!!.text  = bundle!!.getString("MESSAGE_UPDATE_INFO_DISPLAY")
                }else if(msg.what == MESSAGE_HIDE_INFO_DISPLAY){
                    newView!!.isVisible = false
                }else if(msg.what == MESSAGE_SHOW_INFO_DISPLAY){
                    newView!!.isVisible = true
                }else if(msg.what == MESSAGE_REST_VIEWS){
//                    var userName = findViewById<TextView>(R.id.displayUserName)
//                    userName.text = "UserName: $userNameBuffer"
//                    var locationDisplay = findViewById<TextView>(R.id.displayLocation)
//                    locationDisplay.text = "Location: ${locationBuffer[0]}:${locationBuffer[1]}"
//                    var imageView = findViewById<ImageView>(R.id.imageView)
//                    imageView.setImageResource(android.R.color.transparent)
//                    val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_mm_ss")
//                    val current = simpleDateFormat.format(Date())
//                    var dateDisplay = findViewById<TextView>(R.id.displayDate)
//                    dateDisplay.text = "Date: $current"
//                    takePicture?.isVisible = true
//                    takePicture?.isEnabled = true
//                    takePicture?.isClickable = true
                }else if(msg.what == MESSAGE_NOTIFICATION){
                    var notif = bundle!!.getString("MESSAGE_NOTIFICATION")
                    Toast.makeText(applicationContext, notif, Toast.LENGTH_LONG).show();
                }else{

                }
            }
        }

//        userNameBuffer = intent.getStringExtra("UserName");
//        submitButton = findViewById(R.id.submit)
//        submitButton?.isVisible = false
//        submitButton?.setOnClickListener{
//            submitButton?.isEnabled = false
//            submitButton?.isClickable = false
//            submitButton?.isVisible = false
////            takePicture?.isVisible = false
////            takePicture?.isEnabled = false
////            takePicture?.isClickable = false
//            Log.i(TAG,"submit button clicked")
//            Thread{
////
//                uploadImage()
////                submitButton?.isEnabled = true
////                submitButton?.isClickable = true
////                submitButton?.setTextColor(0xFFFFFF)
//
//            }.start()
//
//
//        }

//        var userName = findViewById<TextView>(R.id.displayUserName)
//        userName.text = "UserName: $userNameBuffer"
//        var locationDisplay = findViewById<TextView>(R.id.displayLocation)
//        locationDisplay.text = "Location: $locationValue"
//        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_mm_ss")
//        val current = simpleDateFormat.format(Date())
//        var dateDisplay = findViewById<TextView>(R.id.displayDate)
//        dateDisplay.text = "Date: $current"

        supportActionBar?.hide()
        Log.d(TAG,"created")
        val currentAPIVersion = android.os.Build.VERSION.SDK_INT
//        var takeButton = findViewById<Button>(R.id.takePicture)
        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try{
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        }catch (ex: SecurityException){
            Log.d(TAG, "Security Exception, no location available")
        }

//        takeButton.setOnClickListener{
//            cameraLib.openCamera()
//        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG,"onActivityResult called")
        super.onActivityResult(requestCode, resultCode, data)
        var imageView = findViewById<ImageView>(R.id.searchButton)
        when(requestCode ){
            CameraView.RESULT_LOAD_IMAGE ->{
                val selectedImage = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(selectedImage,
                    filePathColumn, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                imageView.background = Drawable.createFromPath(picturePath)
            }
            CameraView.RESULT_TAKE_PHOTO ->{
                if(resultCode != RESULT_OK){
                    Log.e(TAG,"RESULT_TAKE_PHOTO NOT OK")
                    return
                }
                Log.d(TAG,"Call back called")
                var inputStream: InputStream? = contentResolver.openInputStream(cameraLib.getUri() as Uri)
                    ?: return
                imageBuffer = BitmapFactory.decodeStream(inputStream)
                var imgView = findViewById<ImageView>(R.id.searchButton)
                imgView.setImageBitmap(imageBuffer)

            }
            else ->{
                Log.e(TAG,"123")
            }
        }
    }


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var text = ("Location: " + location.longitude + ":" + location.latitude)
            Log.d(TAG,""+location.longitude + ":" + location.latitude)
            locationValue = text
//            var locationDisplay = findViewById<TextView>(R.id.displayLocation)
//            locationDisplay.text = locationValue
//            locationBuffer[0] = location.longitude
//            locationBuffer[1] = location.latitude
            Log.d(TAG,"${locationBuffer[0]} ${locationBuffer[1]}")

        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d(TAG,provider+"in status $status")
        }
        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "$provider disable")
        }
        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "$provider enable")
        }
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage(){
        if(locationBuffer[0] == null || locationBuffer[1] == null || (locationBuffer[0] == 0.0 && locationBuffer[1] == 0.0)){
            return
        }

        var msg = Message()
        msg.what = MESSAGE_SHOW_INFO_DISPLAY
        UIHandler?.sendMessage(msg)
        var msg2 = Message()
        msg2.what = MESSAGE_UPDATE_INFO_DISPLAY
        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","图像准备中...")
        UIHandler?.sendMessage(msg2)

        val stream = ByteArrayOutputStream()
        imageBuffer?.compress(Bitmap.CompressFormat.PNG, 90, stream)
        var originalData = stream.toByteArray()
        Log.i(TAG,"Image compressed")
        var msg3 = Message()
        msg3.what = MESSAGE_UPDATE_INFO_DISPLAY
        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","生成预览图像...")
        UIHandler?.sendMessage(msg3)

        var previewmap = Bitmap.createScaledBitmap(imageBuffer, imageBuffer!!.width/8, imageBuffer!!.height/8, false)
        if(previewmap.width < previewmap.height){
            var matrix = Matrix();
            matrix.postRotate(90F);
            previewmap = Bitmap.createBitmap(previewmap, 0, 0, previewmap.width, previewmap.height, matrix, true);
        }

        var msg4 = Message()
        msg4.what = MESSAGE_UPDATE_INFO_DISPLAY
        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","图像上传中...")
        UIHandler?.sendMessage(msg4)
        var stream2 = ByteArrayOutputStream()
        Log.i(TAG,"Image resized")
        previewmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream2)
        Log.i(TAG,"Image compressed to jpeg")
        var previewData = stream2.toByteArray()
        Log.i(TAG,"toByteArray")
        var fileHash = doUpload(originalData)
        Log.i(TAG,"Original image uploaded")
       var previewHash = doUpload(previewData)
        Log.i(TAG,"Preview image uploaded")
        var msg5 = Message()
        msg5.what = MESSAGE_UPDATE_INFO_DISPLAY
        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","区块提交中...")
        UIHandler?.sendMessage(msg5)

        var ret = submitTransaction(fileHash,previewHash)
        var msg6 = Message()
        msg6.what = MESSAGE_UPDATE_INFO_DISPLAY
        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY", "区块提交完成！！！")
        UIHandler?.sendMessage(msg6)
        var msg7 = Message()
        msg7.what = MESSAGE_HIDE_INFO_DISPLAY
        UIHandler?.sendMessage(msg7)
        var msg71 = Message()
        msg71.what = MESSAGE_NOTIFICATION
        bundle?.putString("MESSAGE_NOTIFICATION", "生成区块：${ret?.getJSONObject("result")?.getString("hash")}\n高度：${ret?.getJSONObject("result")?.getString("height")}")
        UIHandler?.sendMessage(msg71)
        var msg8 = Message()
        msg8.what = MESSAGE_REST_VIEWS
        UIHandler?.sendMessage(msg8)


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
        var array = JSONArray();
        array.put(locationBuffer[0])
        array.put(locationBuffer[1])
        postObj.put("Location",array)
        postObj.put("User",userNameBuffer)
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_mm_ss")
        val current = simpleDateFormat.format(Date())
        postObj.put("Date", current.toString())
        var reqString = postObj.toString()
        Log.i(TAG,reqString)
        var b64 = Base64.encodeToString(reqString.toByteArray(),Base64.DEFAULT)

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
}