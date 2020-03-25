package com.cntel.next

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.promeg.pinyinhelper.Pinyin
import org.json.JSONObject

class SpinnerManager(context: Context) : AppCompatActivity() {
    private var con = context
    private var act = context as Activity
    private var mainTextValue: EditText? = null
    private var spinner: Spinner? =  null //findViewById(R.id.planets_spinner)
    private var spinnerActive: Spinner? = null // = act.findViewById(R.id.spinner_active_type)
    private var spinnerChannel: Spinner? = null // = findViewById(R.id.spinner_channel)
    private var spinnerProvince: Spinner? = null // = findViewById(R.id.spinner_province)
    private var spinnerCity: Spinner? = null // = findViewById(R.id.spinner_city)
    private var spinnerArea: Spinner? = null // = findViewById(R.id.spinner_area)
    private var spinnerYearStart: Spinner? = null // = findViewById(R.id.spinner_year_start)
    private var spinnerYearEnd: Spinner? = null // = findViewById(R.id.spinner_year_end)
    private var spinnerMonthStart: Spinner? = null// = findViewById(R.id.spinner_month_start)
    private var spinnerMonthEnd: Spinner? = null // = findViewById(R.id.spinner_month_end)
    private var spinnerDayStart: Spinner? = null // = findViewById(R.id.spinner_day_start)
    private var spinnerDayEnd: Spinner? = null // = findViewById(R.id.spinner_day_end)
    private var editLat: EditText? = null
    private var mFilterValue = JSONObject()
// Create an ArrayAdapter using the string array and a default spinner layout
    private val TAG = "SpinnerController"
    fun makeSpinner(id: Int, layoutID: Int, arrayID: Int) :Spinner{

        var tmp = act.findViewById<Spinner>(id)
        ArrayAdapter.createFromResource(
            con,
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

    fun getFilterValue() :JSONObject{
        mFilterValue.put("MainValue",mainTextValue!!.text.toString())
        if(editLat?.text.toString() != ""){
            val intent= act.intent
            mFilterValue.put("Province",intent.getStringExtra("AddressFromMapProvinceSelect"))
            mFilterValue.put("City",intent.getStringExtra("AddressFromMapCitySelect"))
            mFilterValue.put("Area",intent.getStringExtra("AddressFromMapAreaSelect"))
        }
        return mFilterValue
    }

    fun init(){
        // search
        editLat = act.findViewById<EditText>(R.id.latEdit)
        mainTextValue = act.findViewById<EditText>(R.id.mainValue)
        spinner = makeSpinner(R.id.planets_spinner,R.layout.spinner_item,R.array.planets_array)
        spinner!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                Log.d(TAG,parent.getItemAtPosition(pos).toString())
                mFilterValue.put("MainKey",parent.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinner!!.setSelection( 0,true);
        // active

        spinnerActive = makeSpinner(R.id.spinner_active_type,R.layout.spinner_item_2,R.array.active_array)
        spinnerActive!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                var selectValue = parent.getItemAtPosition(pos).toString()
                if(selectValue != "全部"){
                    selectValue = Pinyin.toPinyin(selectValue,"").toLowerCase()
                }
                mFilterValue.put("ActiveType",selectValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerActive!!.setSelection( 0,true);

        spinnerChannel = makeSpinner(R.id.spinner_channel,R.layout.spinner_item_2,R.array.channel_array)
        spinnerChannel!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                var valueTmp = parent.getItemAtPosition(pos).toString()
                if(valueTmp == "移动客户端"){
                    mFilterValue.put("Channel","mobile")
                }else if (valueTmp == "API"){
                    mFilterValue.put("Channel","api")
                }else{
                    mFilterValue.put("Channel",valueTmp)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerChannel!!.setSelection( 0,true);
        mFilterValue.put("Province","全部")
        mFilterValue.put("City","全部")
        mFilterValue.put("Area","全部")
        spinnerProvince = makeSpinner(R.id.spinner_province,R.layout.spinner_item_2,R.array.province_array)
        spinnerProvince!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                var selectedValue = parent.getItemAtPosition(pos).toString()
                if(selectedValue != "全部"){
                    selectedValue = Pinyin.toPinyin(selectedValue,"").toLowerCase()
                }
                mFilterValue.put("Province",selectedValue)
                if(selectedValue == "sichuan"){
                    spinnerCity = makeSpinner(R.id.spinner_city,R.layout.spinner_item_2,R.array.city_sichuan_array)
                    spinnerCity!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            // An item was selected. You can retrieve the selected item using
                            // parent.getItemAtPosition(pos)
                            var selectCity = parent.getItemAtPosition(pos).toString()
                            if(selectCity != "全部"){
                                selectCity = Pinyin.toPinyin(selectCity,"").toLowerCase()
                            }
                            mFilterValue.put("City",selectCity)
                            spinnerArea = makeSpinner(R.id.spinner_area,R.layout.spinner_item_2,R.array.area_chengdu_array)
                            spinnerArea!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                                    // An item was selected. You can retrieve the selected item using
                                    // parent.getItemAtPosition(pos)
                                    var selectArea = parent.getItemAtPosition(pos).toString()
                                    if(selectArea != "全部"){
                                        selectArea = Pinyin.toPinyin(selectArea,"").toLowerCase()
                                    }
                                    mFilterValue.put("Area",selectArea)

                                }
                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Another interface callback
                                    Log.d(TAG,"nothing selected")
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Another interface callback
                            Log.d(TAG,"nothing selected")
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerProvince!!.setSelection( 0,true);


        mFilterValue.put("YearStart","全部")
        mFilterValue.put("MonthStart","01")
        mFilterValue.put("DayStart","01")
        spinnerYearStart = makeSpinner(R.id.spinner_year_start,R.layout.spinner_item_2,R.array.year_array)
        spinnerYearStart!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                mFilterValue.put("YearStart",parent.getItemAtPosition(pos).toString())
                spinnerMonthStart = makeSpinner(R.id.spinner_month_start,R.layout.spinner_item_2,R.array.month_array)
                spinnerMonthStart!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                        // An item was selected. You can retrieve the selected item using
                        // parent.getItemAtPosition(pos)

                        mFilterValue.put("MonthStart",parent.getItemAtPosition(pos).toString())
                        spinnerDayStart = makeSpinner(R.id.spinner_day_start,R.layout.spinner_item_2,R.array.day_array)
                        spinnerDayStart!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                                // An item was selected. You can retrieve the selected item using
                                // parent.getItemAtPosition(pos)
                                mFilterValue.put("DayStart",parent.getItemAtPosition(pos).toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // Another interface callback
                                Log.d(TAG,"nothing selected")
                            }
                        }
                        spinnerDayStart!!.setSelection( 0,true);
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                        Log.d(TAG,"nothing selected")
                    }
                }
                spinnerMonthStart!!.setSelection( 0,true);
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerYearStart!!.setSelection( 0,true);

        mFilterValue.put("YearEnd","全部")
        mFilterValue.put("MonthEnd","12")
        mFilterValue.put("DayEnd","31")
        spinnerYearEnd = makeSpinner(R.id.spinner_year_end,R.layout.spinner_item_2,R.array.year_array)
        spinnerYearEnd!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                mFilterValue.put("YearEnd",parent.getItemAtPosition(pos).toString())
                spinnerMonthEnd = makeSpinner(R.id.spinner_month_end,R.layout.spinner_item_2,R.array.month_array)
                spinnerMonthEnd!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                        // An item was selected. You can retrieve the selected item using
                        // parent.getItemAtPosition(pos)

                        mFilterValue.put("MonthEnd",parent.getItemAtPosition(pos).toString())
                        spinnerDayEnd = makeSpinner(R.id.spinner_day_end,R.layout.spinner_item_2,R.array.day_array)
                        spinnerDayEnd!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                                // An item was selected. You can retrieve the selected item using
                                // parent.getItemAtPosition(pos)
                                mFilterValue.put("DayEnd",parent.getItemAtPosition(pos).toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // Another interface callback
                                Log.d(TAG,"nothing selected")
                            }
                        }
                        spinnerDayEnd!!.setSelection( 0,true);
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                        Log.d(TAG,"nothing selected")
                    }
                }
                spinnerMonthEnd!!.setSelection( 0,true);
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerYearEnd!!.setSelection( 0,true);
    }

}