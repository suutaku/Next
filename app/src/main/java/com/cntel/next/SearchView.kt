package com.cntel.next

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.text.Editable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.widget.*
import androidx.appcompat.app.AlertDialog

class SearchView : AppCompatActivity(){
    val TAG = "SearchView"
    var sp: SharedPreferences? = null
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_view)

        // spinners
        var spManager = SpinnerManager(this)
        spManager.init()

        var menuButton = findViewById<Button>(R.id.menuButton)

        menuButton.setOnClickListener {
            var popMenu = PopupMenu(this,it)
            popMenu.inflate(R.layout.menu_main)
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
                        val intent= Intent(this,UploadView::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.search -> {
//                        val intent= Intent(this,SearchView::class.java)
//                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
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

        var searchButton = findViewById<ImageView>(R.id.searchButton)
        searchButton.setOnClickListener {
            Log.d(TAG,spManager.getFilterValue().toString())

            val intent= Intent(this,SmartScrollView::class.java)
            //check filter value
            if(!checkFilterValue(spManager.getFilterValue())){
                Toast.makeText(
                    this@SearchView, "请输入合法的关键字进行搜索！",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            intent.putExtra("FilterValue",spManager.getFilterValue().toString())
            startActivity(intent)

        }

        var mapSelectorButton = findViewById<Button>(R.id.mapSelectorButton)
        mapSelectorButton.setOnClickListener {
            var resultDialog = AlertDialog.Builder(this)
            resultDialog.setTitle("选择地址")
            resultDialog.setView(R.layout.map_view)
            resultDialog.setPositiveButton("确定", null)
            resultDialog.setNegativeButton("取消",null)
            resultDialog.setOnDismissListener{
                var regLat = intent.getDoubleExtra("LatFromMapSelect",-1.0)
                var regLng = intent.getDoubleExtra("LngFromMapSelect",-1.0)
                if(regLat != -1.0 && regLng != -1.0){
                    var lngEdit = findViewById<EditText>(R.id.lngEdit)
                    var latEdit = findViewById<EditText>(R.id.latEdit)
                    var adressD = findViewById<TextView>(R.id.adressDisplay)
                    var addressStr = intent.getStringExtra("AddressFromMapSelect")
                    latEdit.text = Editable.Factory.getInstance().newEditable(regLat.toString())
                    lngEdit.text = Editable.Factory.getInstance().newEditable(regLng.toString())
                    adressD.text = addressStr
                }
            }
            var viewTmp = resultDialog.show()
            var mapViewCtl = MapViewController(this,viewTmp)
            mapViewCtl.init()


        }


    }

    private fun checkFilterValue(input: JSONObject): Boolean{
        if(input == null){
            return false
        }
        if(!input.has("MainKey")){
            return false
        }

        if(input.getString("MainKey") != "全部"){
            if(!input.has("MainValue")){
                return false
            }
            if( input.getString("MainValue") == ""){
                return false
            }
        }else{

        }

        return true
    }

}

