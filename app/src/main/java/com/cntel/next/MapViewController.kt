package com.cntel.next

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*


class MapViewController : AppCompatActivity() {
    private lateinit var mLocationClient: LocationClient
    lateinit var baiduMap: BaiduMap
    private var mapView: MapView? = null

    @SuppressLint("WorldWriteableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.map_view)

        //back
        var backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            val intent= Intent(this,CameraView::class.java)
            startActivity(intent)
        }



        mapView = findViewById<MapView>(R.id.bMapView)
        //定位初始化
        mLocationClient = LocationClient(applicationContext)
        mLocationClient.registerLocationListener(myLocationListener)
        val option = LocationClientOption()
        option.coorType = "bd09ll"
        option.scanSpan = 0
        option.openGps = true
        mLocationClient.locOption = option
        mLocationClient.start()

        //地图初始化
        baiduMap = mapView!!.map
        baiduMap.isMyLocationEnabled = true//开启定位图层

        baiduMap.setOnMapLongClickListener {
            baiduMap.clear();
            var opt = MarkerOptions().position(it).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
            baiduMap.addOverlay(opt)
            var geoCoder = GeoCoder.newInstance()
            var op = ReverseGeoCodeOption()
            op.location(it)
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
                        showAlterDialog( "抱歉，未能找到结果", LatLng(.0,.0))
                    }
//                    Toast.makeText(
//                        this@MapViewController,
//                        "位置：" + result.address, Toast.LENGTH_LONG
//                    )
//                        .show()
                    showAlterDialog( "位置：" + result.address,result.location)
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


//        text_map.onClick {
//            mLocationClient.requestLocation()//重新请求定位
//        }

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission()
        }
    }

    private val myLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(p0: BDLocation?) {
            val lat = p0!!.latitude
            val lng = p0!!.longitude
            val radius = p0!!.radius
           // text_map.setText("lat:$lat, long:$lng")
            val locationData = MyLocationData.Builder()
                .accuracy(radius)
                .latitude(lat)
                .longitude(lng)
                .direction(100f)
                .build()
            baiduMap.setMyLocationData(locationData)
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(MapStatus.Builder().zoom(18.0f).target(
                LatLng(lat, lng)
            ).build()))
        }

    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        mLocationClient.stop()//关闭定位
        baiduMap.isMyLocationEnabled = false//销毁定位图层
        mapView?.onDestroy()//关闭地图
        super.onDestroy()
    }

    private fun checkPermission() {
        val mPermissionList = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, mPermissionList, 123)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationClient.restart()
    }
    fun showAlterDialog(message: String, latLng: LatLng){
        var dLog = AlertDialog.Builder(this)
        dLog.setTitle("确认信息")
        dLog.setMessage(message)
        dLog.setNegativeButton("取消",
            DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                return@OnClickListener })

        dLog.setPositiveButton("确认",
            DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                val intent= Intent(this,CameraView::class.java)
                intent.putExtra("LatFromMap",latLng.latitude)
                intent.putExtra("LngFromMap",latLng.longitude)
                intent.putExtra("AddressFromMap",message)
                startActivity(intent)
            })

        dLog.show()


    }
}