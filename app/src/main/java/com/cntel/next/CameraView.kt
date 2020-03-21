package com.cntel.next

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
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
import android.text.Editable
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
import androidx.appcompat.app.AlertDialog
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import org.json.JSONArray
import org.w3c.dom.Text

class CameraView : AppCompatActivity(){
    val TAG = "CameraView"
    private var cameraLib = CameraLib(this)
    private lateinit var mLocationClient: LocationClient
    var sp: SharedPreferences? = null
    private var imageUri :Uri ? = null
    private var locationValue :String ? = null
    private var locationManager : LocationManager? = null
    companion object {
        val RESULT_LOAD_IMAGE = 3//选择图片
        val RESULT_TAKE_PHOTO = 4//拍照
        val MESSAGE_PROCESS_START = 1
        var MESSAGE_UPLOAD_START = 2
        var MESSAGE_BLOCK_SUBMIT = 3
        var MESSAGE_PROGRESS_END = 4
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

        var regLat = intent.getDoubleExtra("LatFromMap",-1.0)
        var regLng = intent.getDoubleExtra("LngFromMap",-1.0)
        if(regLat != -1.0 && regLng != -1.0){
            var lngEdit = findViewById<EditText>(R.id.lngEdit)
            var latEdit = findViewById<EditText>(R.id.latEdit)
            var adressD = findViewById<TextView>(R.id.adressDisplay)
            var addressStr = intent.getStringExtra("AddressFromMap")
            latEdit.text = Editable.Factory.getInstance().newEditable(regLat.toString())
            lngEdit.text = Editable.Factory.getInstance().newEditable(regLng.toString())
            adressD.text = addressStr
        }

        var takePicture = findViewById<Button>(R.id.takePictrue)
        takePicture.setOnClickListener {
            cameraLib.openCamera()

        }

        var loadImage = findViewById<Button>(R.id.loadPicture)
        loadImage.setOnClickListener {
            cameraLib.loadImage()

        }


        supportActionBar?.hide()
        Log.d(TAG,"created")

    }

    private val myLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(p0: BDLocation?) {
            // text_map.setText("lat:$lat, long:$lng")
            intent.putExtra("LatFromMap",p0!!.latitude)
            intent.putExtra("LngFromMap",p0!!.longitude)
            var geoCoder = GeoCoder.newInstance()
            var op = ReverseGeoCodeOption()
            op.location(LatLng(p0!!.latitude,p0!!.longitude))
            val listener: OnGetGeoCoderResultListener = object : OnGetGeoCoderResultListener {
                // 反地理编码查询结果回调函数
                override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult) {
                    if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR
                    ) { // 没有检测到结果
//                        Toast.makeText(
//                            this@MapViewController, "抱歉，未能找到结果",
//                            Toast.LENGTH_LONG
//                        ).show()

                    }
//                    Toast.makeText(
//                        this@MapViewController,
//                        "位置：" + result.address, Toast.LENGTH_LONG
//                    )
//                        .show()
                    intent.putExtra("AddressFromMap",result.address)
                }

                // 地理编码查询结果回调函数
                override fun onGetGeoCodeResult(result: GeoCodeResult) {
                    if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR
                    ) { // 没有检测到结果
                    }
                }
            }
            geoCoder.setOnGetGeoCodeResultListener(listener)
            geoCoder.reverseGeoCode(op)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun constructConfirmInfo(){
        var confirmUserName = findViewById<TextView>(R.id.confirmUserName)
        var confirmAddress = findViewById<TextView>(R.id.confirmAddress)
        var confirmLatlng = findViewById<TextView>(R.id.confirmLatlng)
        var confirmDate = findViewById<TextView>(R.id.confirmDate)
        var confirmPermission = findViewById<TextView>(R.id.confirmPermission)

        confirmUserName.text =  intent.getStringExtra("UserName")
        confirmAddress.text =  intent.getStringExtra("AddressFromMap")
        confirmLatlng.text =  intent.getDoubleExtra("LatFromMap",-1.0).toString() +" "+ intent.getDoubleExtra("LngFromMap",-1.0)
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_mm_ss")
        val current = simpleDateFormat.format(Date())
        confirmDate.text = current.toString()
        confirmPermission.text = "管理员"

        var cancelButton = findViewById<Button>(R.id.cancelSubmit)
        cancelButton.setOnClickListener {
            setContentView(R.layout.camera_view)
        }
        var sumitButton = findViewById<Button>(R.id.submit)
        sumitButton.setOnClickListener {
            Log.d(TAG,"sumitButton clicked")
            if(confirmAddress.text == "") {
                confirmAddress.text = intent.getStringExtra("AddressFromMap")
                Toast.makeText(
                    this@CameraView, "正在获取地址信息",
                    Toast.LENGTH_LONG
                ).show()
                sumitButton.text = "重试"
                return@setOnClickListener
            }

            if(confirmLatlng.text == "") {
                confirmLatlng.text =  intent.getDoubleExtra("LatFromMap",-1.0).toString() +" "+ intent.getDoubleExtra("LngFromMap",-1.0)
                Toast.makeText(
                    this@CameraView, "正在获取定位信息",
                    Toast.LENGTH_LONG
                ).show()
                sumitButton.text = "重试"
                return@setOnClickListener
            }

            var dLog = AlertDialog.Builder(this)
            dLog.setTitle("确认信息")
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
                    if (msg.what == MESSAGE_PROCESS_START) {
                        dIn.dismiss()
                        dLog.setMessage("数据处理中。。。")
                        dIn = dLog.show()
                    } else if (msg.what == MESSAGE_UPLOAD_START) {
                        dIn.dismiss()
                        dLog.setMessage("开始上传。。。")
                        dIn = dLog.show()
                    } else if (msg.what == MESSAGE_BLOCK_SUBMIT) {
                        dIn.dismiss()
                        dLog.setMessage("生成区块。。。")
                        dIn = dLog.show()
                    } else if (msg.what == MESSAGE_PROGRESS_END) {
                        dIn.dismiss()
                        dLog.setMessage("上传完成！")
                        dLog.setPositiveButton("确定",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                return@OnClickListener })
                        dIn = dLog.show()
                    } else {
                        dIn.dismiss()
                        dLog.setMessage("错误！！！")
                        dLog.setPositiveButton("确定",
                            DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                                return@OnClickListener })
                        dIn = dLog.show()
                    }
                }
            }
            Thread {
                Log.d(TAG,"run main process")
                confirmedAndSubmit()
            }.start()


        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG,"onActivityResult called")
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode ){
            CameraView.RESULT_LOAD_IMAGE ->{
                setContentView(R.layout.info_confirm_view)
                constructConfirmInfo()
                val selectedImage = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(selectedImage,
                    filePathColumn, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                var imageView = findViewById<ImageView>(R.id.imageDisplay)
                imageView.background = Drawable.createFromPath(picturePath)
            }
            CameraView.RESULT_TAKE_PHOTO ->{
                setContentView(R.layout.info_confirm_view)
                constructConfirmInfo()
                if(resultCode != RESULT_OK){
                    Log.e(TAG,"RESULT_TAKE_PHOTO NOT OK")
                    return
                }
                Log.d(TAG,"Call back called")
                var inputStream: InputStream? = contentResolver.openInputStream(cameraLib.getUri() as Uri)
                    ?: return
                imageBuffer = BitmapFactory.decodeStream(inputStream)
                var imageView = findViewById<ImageView>(R.id.imageDisplay)
                imageView.setImageBitmap(imageBuffer)

            }
            else ->{
                Log.e(TAG,"123")
            }
        }
    }

    private fun confirmedAndSubmit(){
        Log.d(TAG,"confirmedAndSubmit")
        var msg = Message()
        msg.what = MESSAGE_PROCESS_START
        UIHandler?.sendMessage(msg)
        //get datas
        var msg2 = Message()
        msg2.what = MESSAGE_UPLOAD_START
        UIHandler?.sendMessage(msg2)
        //upload images
            //report cluster
            //report detection server
        var msg3 = Message()
        msg3.what = MESSAGE_BLOCK_SUBMIT
        UIHandler?.sendMessage(msg3)
        //submit block
        var msg4 = Message()
        msg4.what = MESSAGE_PROGRESS_END
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
    @RequiresApi(Build.VERSION_CODES.O)
//    private fun uploadImage(){
//        if(locationBuffer[0] == null || locationBuffer[1] == null || (locationBuffer[0] == 0.0 && locationBuffer[1] == 0.0)){
//            return
//        }
//
//        var msg = Message()
//        msg.what = MESSAGE_SHOW_INFO_DISPLAY
//        UIHandler?.sendMessage(msg)
//        var msg2 = Message()
//        msg2.what = MESSAGE_UPDATE_INFO_DISPLAY
//        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","图像准备中...")
//        UIHandler?.sendMessage(msg2)
//
//        val stream = ByteArrayOutputStream()
//        imageBuffer?.compress(Bitmap.CompressFormat.PNG, 90, stream)
//        var originalData = stream.toByteArray()
//        Log.i(TAG,"Image compressed")
//        var msg3 = Message()
//        msg3.what = MESSAGE_UPDATE_INFO_DISPLAY
//        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","生成预览图像...")
//        UIHandler?.sendMessage(msg3)
//
//        var previewmap = Bitmap.createScaledBitmap(imageBuffer, imageBuffer!!.width/8, imageBuffer!!.height/8, false)
//        if(previewmap.width < previewmap.height){
//            var matrix = Matrix();
//            matrix.postRotate(90F);
//            previewmap = Bitmap.createBitmap(previewmap, 0, 0, previewmap.width, previewmap.height, matrix, true);
//        }
//
//        var msg4 = Message()
//        msg4.what = MESSAGE_UPDATE_INFO_DISPLAY
//        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","图像上传中...")
//        UIHandler?.sendMessage(msg4)
//        var stream2 = ByteArrayOutputStream()
//        Log.i(TAG,"Image resized")
//        previewmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream2)
//        Log.i(TAG,"Image compressed to jpeg")
//        var previewData = stream2.toByteArray()
//        Log.i(TAG,"toByteArray")
//        var fileHash = doUpload(originalData)
//        Log.i(TAG,"Original image uploaded")
//       var previewHash = doUpload(previewData)
//        Log.i(TAG,"Preview image uploaded")
//        var msg5 = Message()
//        msg5.what = MESSAGE_UPDATE_INFO_DISPLAY
//        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY","区块提交中...")
//        UIHandler?.sendMessage(msg5)
//
//        var ret = submitTransaction(fileHash,previewHash)
//        var msg6 = Message()
//        msg6.what = MESSAGE_UPDATE_INFO_DISPLAY
//        bundle?.putString("MESSAGE_UPDATE_INFO_DISPLAY", "区块提交完成！！！")
//        UIHandler?.sendMessage(msg6)
//        var msg7 = Message()
//        msg7.what = MESSAGE_HIDE_INFO_DISPLAY
//        UIHandler?.sendMessage(msg7)
//        var msg71 = Message()
//        msg71.what = MESSAGE_NOTIFICATION
//        bundle?.putString("MESSAGE_NOTIFICATION", "生成区块：${ret?.getJSONObject("result")?.getString("hash")}\n高度：${ret?.getJSONObject("result")?.getString("height")}")
//        UIHandler?.sendMessage(msg71)
//        var msg8 = Message()
//        msg8.what = MESSAGE_REST_VIEWS
//        UIHandler?.sendMessage(msg8)
//
//
//    }
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

