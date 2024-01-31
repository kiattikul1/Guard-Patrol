package com.example.guard_patrol.Activity

import BasedActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterWorkspaceAll
import com.example.guard_patrol.Adapter.AdapterWorkspaceFilter
import com.example.guard_patrol.Adapter.AdapterWorkspaceToday
import com.example.guard_patrol.Class.WorkspaceClass
import com.example.guard_patrol.Class.WorkspaceItem
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.databinding.ActivitySelectworkspaceBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.Locale

class SelectWorkspaceActivity : BasedActivity() {
    private var workspaceTodayList = ArrayList<WorkspaceItem>()
    private var workspaceAllList = ArrayList<WorkspaceItem>()
    private lateinit var wTodayAdapter: AdapterWorkspaceToday
    private lateinit var wAllAdapter: AdapterWorkspaceAll
    private lateinit var wFilterAdapter: AdapterWorkspaceFilter
    private lateinit var binding: ActivitySelectworkspaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectworkspaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (workspacePreference.getPreferences().isNullOrEmpty()){
            binding.btnBack.visibility = View.GONE
        }else{
            binding.btnBack.visibility = View.VISIBLE
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        wTodayAdapter = AdapterWorkspaceToday()
        wAllAdapter = AdapterWorkspaceAll()
        wFilterAdapter = AdapterWorkspaceFilter()
        addDataToList()

        //Search Workspace
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                checkWorkspaceTodayListEmpty()
                return false
            }

        })
    }

    private fun filterList(query: String?) {
        if (!query.isNullOrBlank()) {
            binding.recyclerViewWorkspaceFilter.visibility = View.VISIBLE
            binding.txtWorkspaceToday.visibility = View.GONE
            binding.txtWorkspaceAll.visibility = View.GONE
            binding.recyclerViewWorkspaceTodayList.visibility = View.GONE
            binding.recyclerViewWorkspaceAllList.visibility = View.GONE
            val filteredList = ArrayList<WorkspaceItem>()
            val sumWorkspace = ArrayList<WorkspaceItem>()
            sumWorkspace.addAll(workspaceTodayList)
            sumWorkspace.addAll(workspaceAllList)
            for (i in sumWorkspace) {
//                 Log.d("TestFilter", "Check ${i.workspace}")
                if (query.let { i.workspace?.toLowerCase(Locale.ROOT)?.contains(it) } == true) {
                    filteredList.add(i)
                }
            }
            wFilterAdapter.setFilteredList(filteredList)

        } else {
            binding.recyclerViewWorkspaceFilter.visibility = View.GONE
            binding.txtWorkspaceToday.visibility = View.VISIBLE
            binding.txtWorkspaceAll.visibility = View.VISIBLE
            binding.recyclerViewWorkspaceTodayList.visibility = View.VISIBLE
            binding.recyclerViewWorkspaceAllList.visibility = View.VISIBLE
        }
    }

    //Add Workspace
    private fun addDataToList(){

        val query: String = "query PerformTask {\n" +
                "  performTask {\n" +
                "    workspace_all {\n" +
                "      id\n" +
                "      workspace\n" +
                "    }\n" +
                "    workspace_today {\n" +
                "      id\n" +
                "      workspace\n" +
                "    }\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)

        AllService.initialize(applicationContext)
        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val gson = Gson()
                        val workspaceResponse = gson.fromJson(responseBody, WorkspaceClass::class.java)
                        val workspaceAll = workspaceResponse.data?.performTask?.workspaceAll
                        val workspaceToday = workspaceResponse.data?.performTask?.workspaceToday
                        val listWorkspaceAllId = workspaceAll?.map { it.id }
                        val listWorkspaceAllName = workspaceAll?.map { it.workspace }
                        val listWorkspaceTodayId = workspaceToday?.map { it.id }
                        val listWorkspaceTodayName = workspaceToday?.map { it.workspace }
//                        Log.d("TestGetWorkspaceAll", "pass test $workspaceAllList")
//                        Log.d("TestGetWorkspaceToday", "pass test $workspaceTodayList")
                        val valuesList = workspacePreference.getPreferences()
                        if (listWorkspaceTodayName != null && listWorkspaceTodayId != null) {
                            for ((index, name) in listWorkspaceTodayName.withIndex()) {
                                val id = listWorkspaceTodayId.getOrNull(index) ?: ""
                                var stats = false
                                if (valuesList.isNotEmpty()){
                                    val selectedWorkspace = valuesList[1]
                                    stats = (name == selectedWorkspace)
                                }
                                workspaceTodayList.add(WorkspaceItem(id, name, stats))
                                wTodayAdapter.dataList = workspaceTodayList
//                                Log.d("Test WorkspaceList", "check $workspaceList")
                            }
                            checkWorkspaceTodayListEmpty()
                        }

                        if (listWorkspaceAllName != null && listWorkspaceAllId != null) {
                            for ((index, name) in listWorkspaceAllName.withIndex()) {
                                val id = listWorkspaceAllId.getOrNull(index) ?: ""
                                var stats = false
                                if (valuesList.isNotEmpty()){
                                    val selectedWorkspace = valuesList[1]
                                    stats = (name == selectedWorkspace)
                                }
                                workspaceAllList.add(WorkspaceItem(id, name, stats))
                                wAllAdapter.dataList = workspaceAllList
//                                Log.d("Test WorkspaceList", "check $workspaceAllList")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TestGetWorkspace", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestGetWorkspace", "Fail Test ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestGetWorkspace","Error $t")
            }

        })

        //Filter
        wFilterAdapter.action = {id,name ->
            if (id != null && name != null) {
                workspacePreference.setPreferences(id,name)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        this.binding.recyclerViewWorkspaceFilter.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = wFilterAdapter
        }

        //TODAY
        wTodayAdapter.action = {id,name ->
            if (id != null && name != null) {
                workspacePreference.setPreferences(id,name)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        this.binding.recyclerViewWorkspaceTodayList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = wTodayAdapter
        }

        //ALL
        wAllAdapter.action = { id,name ->
            if (id != null && name != null) {
                workspacePreference.setPreferences(id,name)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        this.binding.recyclerViewWorkspaceAllList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = wAllAdapter
        }

    }

    fun checkWorkspaceTodayListEmpty(){
        if (workspaceTodayList.isEmpty()){
            binding.txtWorkspaceToday.visibility = View.GONE
            binding.recyclerViewWorkspaceTodayList.visibility = View.GONE
        }else{
            binding.txtWorkspaceToday.visibility = View.VISIBLE
            binding.recyclerViewWorkspaceTodayList.visibility = View.VISIBLE
        }
    }

}