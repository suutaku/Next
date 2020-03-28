package com.cntel.next

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import com.github.promeg.pinyinhelper.Pinyin


class MapViewController(baseCon: Context,context: AlertDialog) : AppCompatActivity() {
    private val TAG = "MapViewController"
    private lateinit var mLocationClient: LocationClient
    lateinit var baiduMap: BaiduMap
    private var mapView: MapView? = null
    private var mContext = context
    private var mBaseContext = baseCon
    fun init(){
        var cancelButton =  mContext.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelButton?.setOnClickListener {
            mContext.dismiss()
        }
        mapView = mContext.findViewById<MapView>(R.id.bMapView2)
        //定位初始化
        mLocationClient = LocationClient(mBaseContext.applicationContext)
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
                    showAlterDialog( result)
                }

                // 地理编码查询结果回调函数
                override fun onGetGeoCodeResult(result: GeoCodeResult) {
                    if (result.error != SearchResult.ERRORNO.NO_ERROR
                    ) { // 没有检测到结果
                    }
                }
            }
            geoCoder.setOnGetGeoCodeResultListener(listener)
            geoCoder.reverseGeoCode(op)


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

    fun showAlterDialog(result: ReverseGeoCodeResult){
        var dLog = mBaseContext?.let { AlertDialog.Builder(it) }
        dLog?.setTitle("确认信息")
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
            dLog?.setMessage("未能获取地址信息")
            dLog?.setPositiveButton("确定",
                DialogInterface.OnClickListener{ _: DialogInterface, _: Int ->

                }
            )
        }else{
            dLog?.setMessage(result.address)
            dLog?.setNegativeButton("取消",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    return@OnClickListener })

            dLog?.setPositiveButton("确认",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    var act = mBaseContext as Activity
                    val intent= act.intent
                    intent.putExtra("LatFromMapSelect",result.location.latitude)
                    intent.putExtra("LngFromMapSelect",result.location.longitude)
                    intent.putExtra("AddressFromMapSelect",result.address)
                    intent.putExtra("AddressFromMapProvinceSelect",Pinyin.toPinyin(result.addressDetail.province,"").toLowerCase())
                    intent.putExtra("AddressFromMapCitySelect",Pinyin.toPinyin(result.addressDetail.city,"").toLowerCase())
                    intent.putExtra("AddressFromMapAreaSelect",Pinyin.toPinyin(result.addressDetail.district,"").toLowerCase())

                })
        }

        dLog?.show()


    }
}