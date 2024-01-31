package com.example.guard_patrol.Activity

import BasedActivity
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterListTask
import com.example.guard_patrol.Class.AssignTask
import com.example.guard_patrol.Class.CellType
import com.example.guard_patrol.Class.GetTaskData
import com.example.guard_patrol.Class.SOP
import com.example.guard_patrol.Class.Task
import com.example.guard_patrol.Class.TaskClass
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.ActivityChecklistBinding
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


@SuppressLint("SetTextI18n")
class ChecklistActivity : BasedActivity() {
    private var allTask = ArrayList<Task>()
    private var cellTypeList = ArrayList<TaskClass>()
    private lateinit var mAdapter: AdapterListTask
    private lateinit var binding: ActivityChecklistBinding
    private lateinit var captureIV : ImageView
    private lateinit var imageUri : Uri
    private lateinit var selectWorkspace : String
    private lateinit var pointId : String
    private lateinit var pointName : String
    private var positionImage : Int? = null
    private lateinit var idImage : String
    private var imageFiles:  ArrayList<ArrayList<File>>? = null
    private val imageUrlsMap = ArrayList<ArrayList<String>>()
    private val storageRequestCode = 1

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){ result ->
        if (result) {
            val bitmap: Bitmap? = decodeUriToBitmap(imageUri)
            captureIV.setImageBitmap(null)
            captureIV.setImageBitmap(bitmap)

            if (bitmap != null) {
                saveImage(bitmap)

                val wrapper = ContextWrapper(this)
                var newImageFile = wrapper.getDir("myCapturedImages", Context.MODE_PRIVATE)
                newImageFile = File(newImageFile, "FILENAME-$idImage-POSITION-$positionImage.jpg")
                // Check if the file already exists
                if (newImageFile.exists()) newImageFile.delete()
                // Create a new file
                val stream: OutputStream = FileOutputStream(newImageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()

                // Add the new image file to the list
                if (imageFiles != null) {
                    // Check if the position exists in the array
                    if (positionImage != null && positionImage!! >= 0 && positionImage!! < imageFiles!!.size) {
                        // Find the position of the old file in the array
                        val oldFileIndex = imageFiles!![positionImage!!].indexOfFirst { it.path == newImageFile.path }
                        if (oldFileIndex != -1) {
                            // Replace the old file with the new file in the list at the specified position
                            imageFiles!![positionImage!!][oldFileIndex] = newImageFile
                        } else {
                            // If the old file is not found in the list, add the new file to the list at the specified position
                            imageFiles!![positionImage!!].add(newImageFile)
                        }
                    } else {
                        // If the position is out of bounds, create a new list and add the file to it
                        val newList = ArrayList<File>()
                        newList.add(newImageFile)
                        imageFiles!!.add(newList)
                    }
                }

                Log.d("TestCheckFile", "$imageFiles")

            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestStoragePermission(this)

        //Back Button
        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        imageFiles = ArrayList()

        // Get value SharedPreferences
        val valuesWorkspace = workspacePreference.getPreferences()
        selectWorkspace = valuesWorkspace[1]
        pointId = intent.getStringExtra("PointId").toString()
        pointName = intent.getStringExtra("PointName").toString()

        binding.txtWorkspace.text = "ไซต์ : $selectWorkspace"
        binding.txtPoint.text = "จุดที่ลาดตระเวน : $pointName"

        mAdapter = AdapterListTask(actionSendNormalStatus = { position, isNormal ->
            cellTypeList[position].isNormal = isNormal
//            Log.d("###", "")
        }, actionCamera = { id,position,image ->
            idImage = id
            positionImage = position
            captureIV = image
            imageUri = createImageUri()
            contract.launch(imageUri)

        },actionSendRemarks = { position, remark ->
            cellTypeList[position].remark = remark

        }, actionSubmitForm = {
            val dataItems = cellTypeList.filter { it.cellType == CellType.TASK }
            val taskId = cellTypeList.firstOrNull()?.data?.assignTask?.tasks?.firstOrNull()?.id
            imageFiles?.let { uploadImages(it) {
//                cellTypeList[positionImage!!].imageUrls.addAll(imageUrlsMap[positionImage!!] ?: emptyList())
            } }
            var errorDialogShown = false
            if (taskId != null) {
                val tasksArray = JsonArray()
                dataItems.forEach { data ->
                    val taskObject = JsonObject()
                    taskObject.addProperty("task_id", taskId)
                    taskObject.addProperty("isNormal", data.isNormal)

                    if (data.isNormal == null && !errorDialogShown){
                        val title = "ไม่สามารถส่งรายงานได้"
                        val message = "กรุณาตรวจสอบให้แน่ใจว่าได้ทำการ\nตรวจเช็คทุกรายการแล้ว"
                        val iconResId = R.drawable.ic_error
                        showCustomErrorDialogBox(title, message, iconResId)
                        errorDialogShown = true

                    }else{
                        if (data.isNormal == false) {
                            if ((data.remark == null || data.remark == "") && !errorDialogShown){
                                val title = "ไม่สามารถส่งรายงานได้"
                                val message = "จำเป็นต้องกรอกหมายเหตุ"
                                val iconResId = R.drawable.ic_error
                                showCustomErrorDialogBox(title, message, iconResId)
                                errorDialogShown = true

                            }else{
                                taskObject.addProperty("remark", data.remark)
                                //1. URI image from Adapter
                                //2. Submit Image File to API -> Get list of [Https URL]
                                //3. Loop Https URL []
                                //TODO: Loop Https URL []
                                val imageJsonArray = JsonArray()


                                taskObject.add("imageUrls", imageJsonArray)
                            }
                        }
                        tasksArray.add(taskObject)
                    }
                }
                Log.d("TestSendReport","Check $tasksArray")
//                sendReport(tasksArray)
//                val title = "การส่งรายงานเสร็จสิ้น"
//                showCustomPassDialogBox(title ,R.drawable.ic_pass)
                //TODO: - Call API Submit -> tasksArray

            }
        })

        this.binding.recyclerViewCheckList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        addDataToList()
    }

    private fun createImageUri():Uri {
        val image = File(filesDir,"camera_photos.png")
        return FileProvider.getUriForFile(this,
            "com.example.guard_patrol.fileProvider",
            image)
    }

    // ฟังก์ชั่นเพื่อแปลง URI เป็น Bitmap
    private fun decodeUriToBitmap(uri: Uri?): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri!!)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    //Add CheckList
    private fun addDataToList() {
        val paramObject = JsonObject()
        paramObject.addProperty("pointsId", pointId)
        val query: String = "query Query(\$pointsId: ID!) {\n" +
                "  assignTask(points_id: \$pointsId) {\n" +
                "    tasks {\n" +
                "      id\n" +
                "      title_task\n" +
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
        // Create headers object
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        // Add headers to reqObject
        reqObject.add("headers", headersObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
//                        Log.e("TestQlCheckList", "Pass Test $responseBody")
                        val gson = Gson()
                        val taskResponse = gson.fromJson(responseBody, TaskClass::class.java)
                        val listTask = taskResponse.data?.assignTask
                        listTask?.tasks?.forEach { task ->
                            // ดึงข้อมูล Task
                            val taskId = task.id
                            val titleTask = task.titleTask
                            val sops = task.sops!!
                            // สร้าง List ของ SOP สำหรับ Task นี้
                            val listDetail = ArrayList<SOP>()
                            sops.forEach { sop ->
                                // ดึงข้อมูล SOP
                                val sopId = sop.id
                                val titleSop = sop.titleSop
                                val description = sop.description
                                // สร้าง SOP และเพิ่มลงใน List
                                val sopItem = SOP(sopId, titleSop, description)
                                listDetail.add(sopItem)
                            }
                            allTask.add(Task(taskId,titleTask,listDetail))
                            cellTypeList = ArrayList<TaskClass>()
                            allTask.forEach { item ->
                                cellTypeList.add(TaskClass(CellType.TASK, GetTaskData(AssignTask(
                                    arrayListOf(item)))))
                            }
                            cellTypeList.add(TaskClass(CellType.BUTTON, null))
                            mAdapter.dataList = cellTypeList
                        }

                    } catch (e: Exception) {
                        Log.e("TestQlCheckList", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestQlCheckList", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestQlCheckList","Error $t")
            }
        })
    }

    private fun sendReport(tasksArray : JsonArray){
        val paramObject = JsonObject()
        val submitTaskObject = JsonObject()
        submitTaskObject.addProperty("points_id", pointId)
        // เพิ่ม tasksArray เข้าไปใน submitTaskObject
        submitTaskObject.add("tasks", tasksArray)
        // เพิ่ม submitTaskObject เข้าไปใน paramObject
        paramObject.add("submitTask", submitTaskObject)

        val query: String = "mutation SubmitTasks(\$submitTask: SubmitTaskInput) {\n" +
                "  submitTasks(submitTask: \$submitTask) {\n" +
                "    massager\n" +
                "    status\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", paramObject)
        val token = tokenPreference.getPreferences()
        // Create headers object
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        // Add headers to reqObject
        reqObject.add("headers", headersObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.e("TestSendReport", "Pass Test $responseBody")
                    } catch (e: Exception) {
                        Log.e("TestSendReport", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestSendReport", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestSendReport","Error $t")
            }
        })
    }

    private fun showCustomErrorDialogBox(title: String? ,message: String?, iconDrawable: Int){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_error_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val icDialog : ImageView = dialog.findViewById(R.id.icDialog)
        val txtTitleDialog : TextView = dialog.findViewById(R.id.txtTitleDialog)
        val txtMessageDialog : TextView = dialog.findViewById(R.id.txtMessageDialog)
        val btnOk : CardView = dialog.findViewById(R.id.btnOk)

        icDialog.setImageResource(iconDrawable)
        txtTitleDialog.text = title
        txtMessageDialog.text = message

        btnOk.setOnClickListener{
            onResume()
            dialog.dismiss()
        }

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.gravity = Gravity.BOTTOM
        window?.attributes = layoutParams

        dialog.show()
    }

    private fun showCustomPassDialogBox(title: String?, iconDrawable: Int){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_pass_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val icDialog : ImageView = dialog.findViewById(R.id.icDialog)
        val txtTitleDialog : TextView = dialog.findViewById(R.id.txtTitleDialog)

        icDialog.setImageResource(iconDrawable)
        txtTitleDialog.text = title

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams

        dialog.show()

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }, 2000)
    }

    private fun saveImage(showedImage: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/DCIM/myCapturedImages")
        Log.d("FilePass","Check root $root")
        myDir.mkdirs()
        val fName = "IMAGE-$idImage-POSITION-$positionImage.jpg"
        val file = File(myDir, fName)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            showedImage.compress(Bitmap.CompressFormat.JPEG, 80, out)
//            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        applicationContext.sendBroadcast(mediaScanIntent)
    }

    private fun uploadImages(imageFiles: ArrayList<ArrayList<File>>, cb: (() -> Unit)) {
        val retrofitServiceAPI = AllService.getURL()
        for (position in imageFiles.indices) {
            val files = imageFiles[position]
            val imageParts = mutableListOf<MultipartBody.Part>()
            val imageUrls = ArrayList<String>()

            for (file in files) {
                if (!file.exists()) {
                    Log.d("TestCheckFile", "Image file does not exist: ${file.name}")
                    // ถ้าไฟล์ไม่มีอยู่ในตำแหน่งที่กำหนดให้ข้ามไปทำตำแหน่งถัดไป
                    continue
                }
                // สร้าง MultipartBody.Part จากไฟล์ภาพ
                val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "images",
                    file.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file)
                )
                // เพิ่ม MultipartBody.Part เข้าไปในลิสต์
                imageParts.add(imagePart)
            }
            // ตรวจสอบว่ามีไฟล์ภาพที่จะอัปโหลดหรือไม่
            if (imageParts.isNotEmpty()) {
                retrofitServiceAPI.callRestAPIService(imageParts).enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            try {
                                val responseBody = response.body()?.string()
//                                Log.d("TestImageURL", "Response: $responseBody")
                                val jsonObject = responseBody?.let { JSONObject(it) }
                                val imagesUrlArray = jsonObject?.getJSONArray("imagesUrl")

                                if (imagesUrlArray != null) {
                                    for (i in 0 until imagesUrlArray.length()) {
                                        val imageUrl = imagesUrlArray.getString(i)
                                        imageUrls.add(imageUrl)
                                    }
                                }

                                // เพิ่ม imageUrls ลงในลิสต์หลัก (ตำแหน่ง position)
                                if (imageUrls.isNotEmpty()) {
                                    imageUrlsMap.add(imageUrls)
                                }

                                cb.invoke()
                                Log.d("TestMapURL", "MapURL $imageUrlsMap")
                            } catch (e: Exception) {
                                Log.d("TestImageURL", "Error parsing response body: $e")
                            }
                        } else {
                            Log.d("TestImageURL", "Error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.d("TestImageURL", "Failure: $t")
                    }
                })
            } else {
                Log.d("TestImageURL", "No images to upload.")
            }
        }
    }


    private fun checkAndRequestStoragePermission(activity: BasedActivity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                storageRequestCode
            )
        }
    }

}