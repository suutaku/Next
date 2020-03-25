package com.cntel.next

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CameraLib(con: Context)  : AppCompatActivity(){
    val TAG = "CameraLib"
    private var  activity = con as Activity
    private var context = con
    private var openCameraCallback : ((Any) -> Unit)? = null
    companion object {
        const val RESULT_LOAD_IMAGE = 3//选择图片
        const val RESULT_TAKE_PHOTO = 4//拍照
    }
    //get location
    private var imageUri :Uri?  = null
    private var imageBuffer :Bitmap? = null
    private fun hasSdcard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    fun getUri() : Uri? {
        return imageUri
    }
    fun openCamera(){

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (hasSdcard()){
            Log.d("hello","hash sdcard")
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val fileName = simpleDateFormat.format(Date())
            val tempFile = File(Environment.getExternalStorageDirectory(), "$fileName.jpg")
            val currentAPIVersion =  Build.VERSION.SDK_INT
            if (currentAPIVersion<24){
                //从文件中创建uri
                imageUri = Uri.fromFile(tempFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            }else{
                //兼容android7.0 使用共享文件的形式
                val contentValues = ContentValues(1)
                contentValues.put(MediaStore.Images.Media.DATA, tempFile.absolutePath)
                //检查是否有存储权限，以免崩溃
                if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
                    Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show()
                    return
                }
                imageUri =  context!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            }
        }else{
            Log.d("hello","not have sdcard")
        }
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
        activity?.startActivityForResult(intent, RESULT_TAKE_PHOTO)
    }

    fun loadImage(){
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity?.startActivityForResult(intent, RESULT_LOAD_IMAGE)
    }

}