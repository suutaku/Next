package com.cntel.next

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    var sp: SharedPreferences? = null
    private var username_et: EditText? = null;
    private var password_et: EditText? = null ;
    private var rem: CheckBox? = null ;
    private var login: Button? = null;
    private var username: String? = null;
    private var password: String? = null;
    private var forgetPassword: TextView? = null
    private val mPermissionList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE)

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp=getSharedPreferences("userInfo", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission(mPermissionList)
        }


        username_et = findViewById<EditText>(R.id.userNameInput);
        password_et = findViewById<EditText>(R.id.passwdInput);
        rem =  findViewById<CheckBox>(R.id.rememberMe);
        login = findViewById<Button>(R.id.loginButton);
        forgetPassword= findViewById<Button>(R.id.forgotenButton);
        rem!!.isChecked = sp!!.getBoolean("rememberMe",false)
        if (rem!!.isChecked) {
            var uNameTmp = sp?.getString("username", "")
            username_et!!.setText(uNameTmp);
            password_et!!.setText(sp?.getString("password", ""));
//            // TODO autherization
//            val intent= Intent(this,CameraView::class.java)
//            intent.putExtra("UserName",uNameTmp)
//            startActivity(intent)
        }



        login!!.setOnClickListener{
            username=username_et!!.text.toString();
            password=password_et!!.text.toString();
            if (username != "" && password != "") {
                Toast.makeText(applicationContext, "登录成功", Toast.LENGTH_SHORT).show();
                if (rem!!.isChecked) {
                    var editor=sp?.edit();
                    editor?.putString("username", username);
                    editor?.putString("password", password);
                    rem?.isChecked?.let { it1 -> editor?.putBoolean("rememberMe", it1) }
                    editor?.commit();
                }
                // TODO autherization
                val intent= Intent(this,UploadView::class.java)
                intent.putExtra("UserName",username)
                startActivity(intent)
            }
        }
    }

    private fun checkPermission(list:  Array<out String> ) {

        list.forEach {
            if (ContextCompat.checkSelfPermission(this,
                    it)
                != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        it)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(it),
                        123)
                }
            } else {
                // Permission has already been granted
            }
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var denined =  ArrayList<String>()

        for(i in permissions.indices){
            if(grantResults[i] != 0){
                denined.add(permissions[i])
            }
        }
        if(denined.size > 0){
            checkPermission(denined.toArray() as Array<out String>)
        }else{
        }
    }
}