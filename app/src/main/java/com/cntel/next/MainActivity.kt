package com.cntel.next

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var sp: SharedPreferences? = null
    private var username_et: EditText? = null;
    private var password_et: EditText? = null ;
    private var rem: CheckBox? = null ;
    private var auto: CheckBox? = null;
    private var login: Button? = null;
    private var username: String? = null;
    private var password: String? = null;

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp=getSharedPreferences("userInfo", MODE_PRIVATE);


        username_et = findViewById<EditText>(R.id.userNameInput);
        password_et = findViewById<EditText>(R.id.passwdInput);
//        rem =  findViewById<CheckBox>(R.id.rememberMe);
//        auto= findViewById<CheckBox>(R.id.autoLogin);
//        singin= findViewById<Button>(R.id.signinbutton);
//        forgetPassword= findViewById<Button>(R.id.forgetPassword);
////        login= findViewById<Button>(R.id.loginButton)
////        rem!!.isChecked = sp!!.getBoolean("rememberMe",false)
////        auto!!.isChecked = sp!!.getBoolean("autoLogin",false)
//
//        if (rem!!.isChecked) {
//            var uNameTmp = sp?.getString("username", "")
//            username_et!!.setText(uNameTmp);
//            password_et!!.setText(sp?.getString("password", ""));
//
//            if (auto!!.isChecked) {
//                val intent= Intent(this,CameraView::class.java)
//                intent.putExtra("UserName",uNameTmp)
//                startActivity(intent)
//            }
//
//        }



//        login!!.setOnClickListener{
//            username=username_et!!.text.toString();
//            password=password_et!!.text.toString();
//
//            if (username != "" && password != "") {
//                Toast.makeText(applicationContext, "登录成功", Toast.LENGTH_SHORT).show();
//                if (rem!!.isChecked) {
//                    var editor=sp?.edit();
//                    editor?.putString("username", username);
//                    editor?.putString("password", password);
//                    rem?.isChecked?.let { it1 -> editor?.putBoolean("rememberMe", it1) }
//                    auto?.isChecked?.let { it1 -> editor?.putBoolean("autoLogin",it1)}
//                    editor?.commit();
//                }
//
//                val intent= Intent(this,CameraView::class.java)
//                intent.putExtra("UserName",username)
//                startActivity(intent)
//            }
//        }
    }
}