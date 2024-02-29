package com.example.guard_patrol.Activity

import BasedActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterContactList
import com.example.guard_patrol.Class.AuthenticationPhone
import com.example.guard_patrol.Class.ContactListClass
import com.example.guard_patrol.Data.Service.AllService
import com.example.guard_patrol.databinding.ActivityContactListBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.Locale

class ContactListActivity : BasedActivity() {
    private lateinit var binding: ActivityContactListBinding
    private lateinit var contactListAdapter: AdapterContactList
    private var contact = ArrayList<AuthenticationPhone>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        contactListAdapter = AdapterContactList()
        addData()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return false
            }

        })
    }

    private fun filterList(query: String?) {
        if (!query.isNullOrBlank()) {
            val filteredList = ArrayList<AuthenticationPhone>()
            val sumContact = ArrayList<AuthenticationPhone>()
            sumContact.addAll(contact)
            for (i in sumContact) {
                if (query.let { i.firstname?.toLowerCase(Locale.ROOT)?.contains(it) } == true
                    || query.let { i.lastname?.toLowerCase(Locale.ROOT)?.contains(it) } == true
                    || query.let { i.phoneNumber?.toLowerCase(Locale.ROOT)?.contains(it) } == true) {
                    filteredList.add(i)
                }
            }
//            Log.d("TestFilter", "Check filteredList $filteredList")
            contactListAdapter.setFilteredList(filteredList)
        }else{
            contactListAdapter.setFilteredList(contact)
        }
    }

    private fun addData() {
        val token = tokenPreference.getPreferences()
        val query: String = "query SelectAuthentication {\n" +
                "  authenticationPhone {\n" +
                "    firstname\n" +
                "    lastname\n" +
                "    phone_number\n" +
                "    role\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        reqObject.add("headers", headersObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.d("TestPN", "responseBody $responseBody")
                        val gson = Gson()
                        val dataResponse = gson.fromJson(responseBody, ContactListClass::class.java)
                        val contactListData = dataResponse.data
                        contactListData?.authenticationPhone?.forEach { it ->
                            val firstname = it.firstname
                            val lastname = it.lastname
                            val phoneNumber = it.phoneNumber
                            val role = it.role
                            contact.add(AuthenticationPhone(firstname,lastname,phoneNumber,role))
                        }
                        contactListAdapter.dataList = contact
//                        Log.d("TestEC", "EmergencyList $emergencyList")

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

        contactListAdapter.callPhone = {number ->
            if (number != null) {
                checkPermission(number)
            }else{
                Toast.makeText(this, "This contact fail.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.contactListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = contactListAdapter
        }
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