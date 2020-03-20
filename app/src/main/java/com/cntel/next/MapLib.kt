package com.cntel.next

import android.content.Context
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng

class MapLib(context: Context) {
    private lateinit var mLocationClient: LocationClient
    init {
        SDKInitializer.initialize(context)
        mLocationClient = LocationClient(context)
    }
    fun startSelfLocation(){

        mLocationClient.registerLocationListener(myLocationListener)
        val option = LocationClientOption()
        option.coorType = "bd09ll"
        option.scanSpan = 0
        option.openGps = true
        mLocationClient.locOption = option
        mLocationClient.start()
    }
    private val myLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(p0: BDLocation?) {
            val lat = p0!!.latitude
            val lng = p0!!.longitude
            val radius = p0!!.radius
            // text_map.setText("lat:$lat, long:$lng")

        }

    }
}