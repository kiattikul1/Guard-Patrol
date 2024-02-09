package com.example.guard_patrol.Activity

import BasedActivity
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterHistoryDetailTask
import com.example.guard_patrol.Class.HistoryDetailClass
import com.example.guard_patrol.Class.SOPS
import com.example.guard_patrol.Class.Tasks
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.databinding.ActivityHistoryDetailBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


@SuppressLint("SetTextI18n")
class HistoryDetailActivity : BasedActivity() {
    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var workspaceAt : String
    private lateinit var pointId : String
    private var allTask = ArrayList<Tasks>()
    private lateinit var taskAdapter: AdapterHistoryDetailTask
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Back Button
        binding.btnBack.setOnClickListener{
            // Use onBackPressed() for back to HistoryActivity
            onBackPressed()
        }

        pointId = intent.getStringExtra("PointId").toString()
        workspaceAt = intent.getStringExtra("WorkspaceAt").toString()
//        Log.d("TestHistoryDetail","Check pointId $pointId")
//        Log.d("TestHistoryDetail","Check WorkspaceAt $WorkspaceAt")

        taskAdapter = AdapterHistoryDetailTask()
        listData(){
            taskAdapter.dataList = allTask
            Log.d("TestDetailTaskHistory","Check allTask $allTask")
            binding.recyclerViewCheckList.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = taskAdapter
            }
        }

    }

    private fun listData(cb: (()-> Unit)) {
        val paramObject = JsonObject()
        paramObject.addProperty("pointsId", pointId)
        paramObject.addProperty("workspaceAt", workspaceAt)
        val query: String = "query DetailHistory(\$pointsId: ID!, \$workspaceAt: String!) {\n" +
                "  detailHistory(points_id: \$pointsId, workspace_at: \$workspaceAt) {\n" +
                "    workspace\n" +
                "    points_id\n" +
                "    point_name\n" +
                "    tasks {\n" +
                "      id\n" +
                "      title_task\n" +
                "      is_normal\n" +
                "      evidenceImages\n" +
                "      remark\n" +
                "      sops {\n" +
                "        id\n" +
                "        title_sop\n" +
                "        description\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", paramObject)
        val token = tokenPreference.getPreferences()
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        reqObject.add("headers", headersObject)
//        Log.d("TestDetailTaskHistory", "Check reqObject $reqObject")

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.d("TestDetailTaskHistory", "Pass Test $responseBody")
                        val gson = Gson()
                        val detailResponse = gson.fromJson(responseBody, HistoryDetailClass::class.java)
                        val listTask = detailResponse.data?.detailHistory
                        listTask?.tasks?.forEach { task ->
                            val taskId = task.id
                            val titleTask = task.titleTask
                            val sops = task.sops!!
                            val listSop = ArrayList<SOPS>()
                            sops.forEach { sop ->
                                val sopId = sop.id
                                val titleSop = sop.titleSop
                                val description = sop.description
                                val sopItem = SOPS(sopId, titleSop, description)
                                listSop.add(sopItem)
                            }
                            val isNormal = task.isNormal
                            val evidenceImages = ArrayList<String?>()
                            var remark : String? = null
                            if (isNormal == false){
                                if (task.evidenceImages != null) {
                                    val listImage = task.evidenceImages
//                                    Log.d("TestDetailTaskHistory", "Check listImage $listImage")
                                    for (imageUrl in listImage) {
                                        try {
                                            val bitmap = imageUrl?.let { MyAsync(it).execute().get() }
//                                            Log.d("TestDetailTaskHistory", "Check bitmap $bitmap")
                                            bitmap?.let {
                                                evidenceImages.add(it.toString())
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                remark = task.remark.toString()
                            }
                            allTask.add(Tasks(taskId,titleTask,listSop,isNormal,evidenceImages,remark))
                        }

                        binding.txtWorkspace.text = "ไซต์ : ${listTask?.workspace}"
                        binding.txtPoint.text = "จุดที่ลาดตระเวน : ${listTask?.pointName}"
                        cb.invoke()

                    } catch (e: Exception) {
                        Log.e("TestDetailTaskHistory", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestDetailTaskHistory", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestDetailTaskHistory","Error $t")
            }
        })
    }

    // MyAsync สำหรับให้ระบบรอการทำงานส่วนนี้ก่อนค่อยไปทำส่วนอื่นต่อ
    class MyAsync(private val src: String) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void?): Bitmap? {
            return try {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

}