package com.example.guard_patrol.Activity

import BasedActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterEmergencyCall
import com.example.guard_patrol.Class.EmergencyCallClass
import com.example.guard_patrol.Class.SelectEmergency
import com.example.guard_patrol.Data.Service.AllService
import com.example.guard_patrol.databinding.ActivityEmergencyCallBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


class EmergencyCallActivity : BasedActivity()  {
    private lateinit var binding : ActivityEmergencyCallBinding
    private lateinit var emergencyAdapter: AdapterEmergencyCall

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        emergencyAdapter = AdapterEmergencyCall()
        emergencyAdapter.callPhone = {number ->
//            Log.d("TestEC", "Number $number")
            if (number != null) {
                checkPermission(number)
            }else{
                Toast.makeText(this, "There is no phone number.", Toast.LENGTH_SHORT).show()
            }
        }

        addData()

    }

    private fun addData() {
        val query: String = "query SelectEmergency {\n" +
                "  selectEmergency {\n" +
                "    id\n" +
                "    title\n" +
                "    phone_number\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val gson = Gson()
                        val dataResponse = gson.fromJson(responseBody, EmergencyCallClass::class.java)
                        val emergencyData = dataResponse.data
                        val emergencyList = ArrayList<SelectEmergency>()
                        emergencyData?.selectEmergency?.forEach { it ->
                            val id = it.id
                            val title = it.title
                            val phoneNumber = it.phoneNumber
                            emergencyList.add(SelectEmergency(id,title,phoneNumber))
                        }
                        emergencyAdapter.dataList = emergencyList
//                        Log.d("TestEC", "EmergencyList $emergencyList")
                        binding.emergencyCllRecyclerView.apply {
                            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
                            setHasFixedSize(true)
                            adapter = emergencyAdapter
                        }

                    } catch (e: Exception) {
                        Log.e("TestEC", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestEC", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestEC","Error $t")
            }
        })
    }

    private fun makePhoneCall(phoneNumber: String) {
        val dialIntent = Intent(
            Intent.ACTION_DIAL, Uri.parse(
                "tel:$phoneNumber"
            )
        )
        startActivity(dialIntent)
    }

    private fun checkPermission(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42)
            }
        } else {
            // Permission has already been granted
            makePhoneCall(phoneNumber)
        }
    }

}