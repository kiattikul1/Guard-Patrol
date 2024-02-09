package com.example.guard_patrol.Activity

import BasedActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterDateSelectHistory
import com.example.guard_patrol.Adapter.AdapterHistoryTask
import com.example.guard_patrol.Class.HistoryClass
import com.example.guard_patrol.Class.Patrol
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.databinding.ActivityHistoryBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryActivity : BasedActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private var historyDateSelect = ArrayList<HistoryClass>()
    private lateinit var historyAdapter: AdapterDateSelectHistory
    private lateinit var historyTaskAdapter: AdapterHistoryTask
    private lateinit var workspaceAt: String
    private var limitDay: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("th", "TH"))
        val zoneId = ZoneId.of("Asia/Bangkok")
        var currentDate = LocalDate.now(zoneId)

        historyAdapter = AdapterDateSelectHistory()
        historyTaskAdapter = AdapterHistoryTask()

        for (i in 0 until limitDay) {
            val selectedDate = currentDate.minusDays(i.toLong())
            val dateSelect = dateFormat.format(selectedDate)
            historyDateSelect.add(HistoryClass(dateSelect, null))
            historyAdapter.dataList = historyDateSelect
            if(currentDate.toString() == dateSelect){
                historyAdapter.positionSelect = (i)
            }
        }

        //Add Date history
        binding.historyDateRecyclerView.apply {
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, true)
            setHasFixedSize(true)
            adapter = historyAdapter
        }

        historyAdapter.changeDate = { date->
            binding.txtDateSelect.text = date
            val fullDateFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale("th", "TH"))
            val selectDate = LocalDate.parse(date, fullDateFormat)
//            Log.d("TestSelectDate","$selectDate")
            workspaceAt = selectDate.toString()
            addHistoryToData(selectDate.toString())
        }

        historyTaskAdapter.historyDetail = { pointId ->
//            Log.d("TestHistoryDetail","Check pointId $pointId")
            val intent = Intent(this, HistoryDetailActivity::class.java)
            intent.putExtra("PointId", pointId)
            intent.putExtra("WorkspaceAt", workspaceAt)
            startActivity(intent)
        }
    }

    private fun addHistoryToData(selectDate: String) {
        val historyObject = JsonObject()
        val valuesList = workspacePreference.getPreferences()
        val workspaceId = valuesList[0]
        historyObject.addProperty("workspaceId", workspaceId)
        historyObject.addProperty("workspaceAt", selectDate)

        val query: String = "query History(\$workspaceId: ID!, \$workspaceAt: String!) {\n" +
                "  history(workspace_id: \$workspaceId, workspace_at: \$workspaceAt) {\n" +
                "    workspace\n" +
                "    patrols {\n" +
                "      points_id\n" +
                "      point_name\n" +
                "      process_date\n" +
                "      completed_at\n" +
                "    }\n" +
                "    \n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", historyObject)
        val token = tokenPreference.getPreferences()
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        reqObject.add("headers", headersObject)
//        Log.d("TestReq", "Check reqObject $reqObject")

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
//                        Log.d("TestQlHistory", "Pass Test $responseBody")
                        val gson = Gson()
                        val historyResponse = gson.fromJson(responseBody, HistoryClass::class.java)
                        val workspaceName = historyResponse.data?.history?.workspace
//                        Log.d("TestQlHistory", "Check workspaceName $workspaceName")
                        val patrolList = historyResponse.data?.history
//                        Log.d("TestQlHistory", "Check patrolList $patrolList")
                        val historyPatrol = ArrayList<Patrol>()
                        patrolList?.patrols?.forEach { patrol ->
                            val pointsId = patrol.pointsId
                            val pointName = patrol.pointName
                            val processDate = patrol.processDate
                            val completedAt = patrol.completedAt
                            historyPatrol.add(Patrol(pointsId,pointName,processDate,completedAt))
                        }
//                        Log.d("TestQlHistory", "Check historyPatrol $historyPatrol")
                        historyTaskAdapter.dataList = historyPatrol
                        //Add Task history
                        binding.historyTaskRecyclerView.apply {
                            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
                            setHasFixedSize(true)
                            adapter = historyTaskAdapter
                        }
                    } catch (e: Exception) {
                        Log.e("TestQlHistory", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestQlHistory", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestQlHistory","Error $t")
            }
        })
    }
}