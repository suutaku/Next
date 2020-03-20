package com.cntel.next

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class SpinnerManager(context: Context) : AppCompatActivity() {
    private var con = context
    private var act = context as Activity
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
        return mFilterValue
    }

    fun init(){
        // search
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
                mFilterValue.put("ActiveType",parent.getItemAtPosition(pos).toString())
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
                mFilterValue.put("Channel",parent.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerChannel!!.setSelection( 0,true);

        spinnerProvince = makeSpinner(R.id.spinner_province,R.layout.spinner_item_2,R.array.province_array)
        spinnerProvince!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                var electedValue = parent.getItemAtPosition(pos).toString()
                mFilterValue.put("Province",electedValue)
                if(electedValue == "四川"){
                    spinnerCity = makeSpinner(R.id.spinner_city,R.layout.spinner_item_2,R.array.city_sichuan_array)
                    spinnerCity!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            // An item was selected. You can retrieve the selected item using
                            // parent.getItemAtPosition(pos)
                            mFilterValue.put("City",parent.getItemAtPosition(pos).toString())
                            spinnerArea = makeSpinner(R.id.spinner_area,R.layout.spinner_item_2,R.array.area_chengdu_array)
                            spinnerArea!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

                                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                                    // An item was selected. You can retrieve the selected item using
                                    // parent.getItemAtPosition(pos)
                                    mFilterValue.put("Area",parent.getItemAtPosition(pos).toString())
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

        spinnerYearStart = makeSpinner(R.id.spinner_year_start,R.layout.spinner_item_2,R.array.year_array)
        spinnerYearStart!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                mFilterValue.put("YearStart",parent.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerYearStart!!.setSelection( 0,true);


        spinnerYearEnd = makeSpinner(R.id.spinner_year_end,R.layout.spinner_item_2,R.array.year_array)
        spinnerYearEnd!!.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                mFilterValue.put("YearEnd",parent.getItemAtPosition(pos).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
                Log.d(TAG,"nothing selected")
            }
        }
        spinnerYearEnd!!.setSelection( 0,true);
    }

}