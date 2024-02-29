package com.example.guard_patrol.Activity

import BasedActivity
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.guard_patrol.Adapter.AdapterHistoryDetailTask
import com.example.guard_patrol.Class.HistoryDetailClass
import com.example.guard_patrol.Class.SOPS
import com.example.guard_patrol.Class.Tasks
import com.example.guard_patrol.Data.Service.AllService
import com.example.guard_patrol.databinding.ActivityHistoryDetailBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


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

        loadingDialog.showLoadingDialog(this)

        //Back Button
        binding.btnBack.setOnClickListener{
            // Use onBackPressed() for back to HistoryActivity
            onBackPressed()
        }

        pointId = intent.getStringExtra("PointId").toString()
        workspaceAt = intent.getStringExtra("WorkspaceAt").toString()

        taskAdapter = AdapterHistoryDetailTask(showImage = { imageUrl ->
            showImage(imageUrl)
        })
        listData(){
            taskAdapter.dataList = allTask
//            Log.d("TestDetailTaskHistory","Check allTask $allTask")
            binding.recyclerViewCheckList.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = taskAdapter
                viewTreeObserver.addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            loadingDialog.dismissLoadingDialog()
                        }
                    }
                )
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
                            Log.d("TestDetailTaskHistory", "titleTask $titleTask")
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
                            var evidenceImages = ArrayList<String?>()
                            var remark : String? = null
                            if (isNormal == false){
                                if (task.evidenceImages != null) {
                                    val listImage = task.evidenceImages
                                    evidenceImages = listImage
                                }
                                remark = task.remark.toString()
                            }
                            allTask.add(Tasks(taskId,titleTask,listSop,isNormal,evidenceImages,remark))
                            Log.d("TestDetailTaskHistory", "Check allTask $allTask")
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

    private fun showImage(imageUrl: String) {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        builder.setOnDismissListener {
            //nothing;
        }
        val imageView = ImageView(this)
        Glide.with(imageView.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                800, 800
            )
        )
        builder.show()
    }

}