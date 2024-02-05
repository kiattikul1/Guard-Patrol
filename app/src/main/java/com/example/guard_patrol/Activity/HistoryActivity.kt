package com.example.guard_patrol.Activity

import BasedActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterDateSelectHistory
import com.example.guard_patrol.Adapter.AdapterTaskHistory
import com.example.guard_patrol.Class.AssignTask
import com.example.guard_patrol.Class.CellType
import com.example.guard_patrol.Class.GetHistoryData
import com.example.guard_patrol.Class.GetLocation
import com.example.guard_patrol.Class.GetTaskData
import com.example.guard_patrol.Class.HistoryClass
import com.example.guard_patrol.Class.Patrol
import com.example.guard_patrol.Class.Task
import com.example.guard_patrol.Class.TaskClass
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.databinding.ActivityHistoryBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.chrono.ThaiBuddhistDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HistoryActivity : BasedActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private var historySelect = ArrayList<HistoryClass>()
    private lateinit var historyAdapter: AdapterDateSelectHistory
    private lateinit var historyTaskAdapter: AdapterTaskHistory
    private val limitDay: Int = 10 //Set the number of days to go back.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val fullDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("th", "TH"))
        val zoneId = ZoneId.of("Asia/Bangkok")
        val currentDate = LocalDate.now(zoneId)
        // Create a YearMonth instance for the current year and month
        val currentYearMonth = YearMonth.from(currentDate)
        // Find the number of days in the current month
        val daysInCurrentMonth = currentYearMonth.lengthOfMonth()
//        Log.d("TestFiveDaysAgo", "Check fiveDaysAgo $daysInCurrentMonth")

        historyAdapter = AdapterDateSelectHistory()
        historyTaskAdapter = AdapterTaskHistory()

        for (i in 1..daysInCurrentMonth) {
            val selectedDate = currentYearMonth.atDay(i)
            val dateSelect = fullDateFormat.format(selectedDate)
            historySelect.add(HistoryClass(dateSelect, null))
            historyAdapter.dataList = historySelect

            //Add Date history
            binding.historyDateRecyclerView.apply {
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                adapter = historyAdapter
            }

            historyAdapter.changeDate = { date , position ->
                binding.txtDateSelect.text = date
            }

//            addHistoryToData(dateSelect)
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
        // Create headers object
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        // Add headers to reqObject
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

                        historySelect.add(HistoryClass(selectDate, GetHistoryData(GetLocation(
                            workspaceName,historyPatrol)))
                        )
                        historyAdapter.dataList = historySelect

                        //Add Date history
                        binding.historyDateRecyclerView.apply {
                            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                            setHasFixedSize(true)
                            adapter = historyAdapter
                        }

                        historyAdapter.changeDate = { date , position ->
                            binding.txtDateSelect.text = date
                            historyTaskAdapter.dataList = historySelect[position!!].data?.history?.patrols!!
                        }

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