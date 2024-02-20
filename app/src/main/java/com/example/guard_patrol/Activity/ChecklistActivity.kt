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
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("SetTextI18n")
class ChecklistActivity : BasedActivity() {
    private var allTask = ArrayList<Task>()
    private var cellTypeList = ArrayList<TaskClass>()
    private lateinit var mAdapter: AdapterListTask
    private lateinit var binding: ActivityChecklistBinding
    private lateinit var selectWorkspace : String
    private lateinit var pointId : String
    private lateinit var pointName : String
    private var captureIV : ImageView? = null
    private var positionImage : Int? = null
    private var idImage : String? = null
    private var imageFiles = ArrayList<ArrayList<File>>()
    private val storageRequestCode = 1
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var orientationEventListener: OrientationEventListener? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9

    private var cropImage = registerForActivityResult(CropImageContract()
    ) { result: CropImageView.CropResult ->
        if (result.isSuccessful) {
            try {
                val cropped =
                    BitmapFactory.decodeFile(result.getUriFilePath(applicationContext, true))
                // Add a button to parse the result here
                val maxWidth = 800 // Set the maximum width
                val maxHeight = 800 // Set the maximum height
                val scaledBitmap = Bitmap.createScaledBitmap(cropped, maxWidth, maxHeight, true)

                if(captureIV != null){
                    captureIV!!.setImageBitmap(null)
                    captureIV!!.setImageBitmap(scaledBitmap)

                    //Fill image in ConstraintLayout
                    captureIV!!.scaleType = ImageView.ScaleType.CENTER_CROP
                    val params = captureIV!!.layoutParams as ConstraintLayout.LayoutParams
                    params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                    params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                    captureIV!!.layoutParams = params
                }

                if (cropped != null) {
                    saveImage(scaledBitmap)

                    val wrapper = ContextWrapper(this)
                    var newImageFile = wrapper.getDir("myCapturedImages", Context.MODE_PRIVATE)
                    newImageFile =
                        File(newImageFile, "FILENAME-${idImage!!}-POSITION-${positionImage!!}.jpg")
                    // Check if the file already exists
                    if (newImageFile.exists()) newImageFile.delete()
                    // Create a new file
                    val stream: OutputStream = FileOutputStream(newImageFile)
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    stream.flush()
                    stream.close()

                    while (imageFiles.size <= positionImage!!) {
                        imageFiles.add(ArrayList())
                    }
                    val oldFileIndex =
                        imageFiles[positionImage!!].indexOfFirst { it.path == newImageFile.path }
                    if (oldFileIndex != -1) {
                        imageFiles[positionImage!!][oldFileIndex] = newImageFile
                    } else {
                        imageFiles[positionImage!!].add(newImageFile)
                    }
                    positionImage = null
                }

                binding.layoutCameraX.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("TestError", "Error : $e")
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

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.capture.setOnClickListener {
            takePhoto()
        }

        binding.closeCamera.setOnClickListener {
            binding.layoutCameraX.visibility = View.GONE
        }

        binding.toggleFlash.setOnClickListener {
            setFlashIcon(camera)
        }

        binding.flipCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }

        // Get value SharedPreferences
        val valuesWorkspace = workspacePreference.getPreferences()
        selectWorkspace = valuesWorkspace[1]
        pointId = intent.getStringExtra("PointId").toString()
        pointName = intent.getStringExtra("PointName").toString()

        binding.txtWorkspace.text = "ไซต์ : $selectWorkspace"
        binding.txtPoint.text = "จุดที่ลาดตระเวน : $pointName"

        mAdapter = AdapterListTask(actionSendNormalStatus = { position, isNormal ->
            cellTypeList[position].isNormal = isNormal
        }, actionCamera = { id,position,image ->
            idImage = id
            positionImage = position
            captureIV = image
            startCamera()

        },actionSendRemarks = { position, remark ->
            cellTypeList[position].remark = remark

        }, actionSubmitForm = {
            val dataItems = cellTypeList.filter { it.cellType == CellType.TASK }
            var errorDialogShown = false
            val tasksArray = JsonArray()
            dataItems.forEach{ data ->
                val taskObject = JsonObject()
                taskObject.addProperty("task_id", data.data?.assignTask?.tasks?.firstOrNull()?.id)
                taskObject.addProperty("isNormal", data.isNormal)

                if (data.isNormal == null && !errorDialogShown){
                    val title = "ไม่สามารถส่งรายงานได้"
                    val message = "กรุณาตรวจสอบให้แน่ใจว่าได้ทำการ\nตรวจเช็คทุกรายการแล้ว"
                    val iconResId = R.drawable.ic_error
                    showCustomErrorDialogBox(title, message, iconResId)
                    errorDialogShown = true
                } else {
                    if (data.isNormal == false) {
                        if ((data.remark == null || data.remark == "") && !errorDialogShown){
                            val title = "ไม่สามารถส่งรายงานได้"
                            val message = "จำเป็นต้องกรอกหมายเหตุ"
                            val iconResId = R.drawable.ic_error
                            showCustomErrorDialogBox(title, message, iconResId)
                            errorDialogShown = true
                        } else {
                            taskObject.addProperty("remark", data.remark)
                        }
                    }

                    tasksArray.add(taskObject)
                }
            }

            if(errorDialogShown == false){
                val isNormalFalsePresent = tasksArray.any {
                    it.asJsonObject.get("isNormal")?.asBoolean == false
                }
                if (isNormalFalsePresent){
                    //Loading
                    if (imageFiles.isNotEmpty()){
                        uploadAllImages(imageFiles) { list ->
                            list.forEachIndexed { index, strings ->
                                var sendObject = tasksArray.get(index).asJsonObject
                                val imageJsonArray = JsonArray()
                                strings.forEach { imageJsonArray.add(it) }
                                if(strings.isNotEmpty()){
                                    for (i in 0 until tasksArray.size()){
                                        val task = tasksArray.get(index)
                                        val isNormal = task.asJsonObject.get("isNormal")?.asBoolean

                                        if (isNormal == false){
                                            sendObject.add("evidenceImages", imageJsonArray)
                                            tasksArray.set(index, sendObject)
                                        }
                                    }
                                }
                            }
//                            Log.d("TestSendReport Status False ","Check $tasksArray")
                            showLoadingDialog(this)
//                            sendReport(tasksArray)
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    dismissLoadingDialog()
                                    showCustomPassDialogBox()
                                },
                                5000 // value in milliseconds
                            )
                        }
                    }else{
//                        Log.d("TestSendReport Status False ","Check $tasksArray")
                        showLoadingDialog(this)
//                        sendReport(tasksArray)
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                dismissLoadingDialog()
                                showCustomPassDialogBox()
                            },
                            5000 // value in milliseconds
                        )
                    }
                }else{
//                    Log.d("TestSendReport Status True ","Check $tasksArray")
                    showLoadingDialog(this)
//                    sendReport(tasksArray)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            dismissLoadingDialog()
                            showCustomPassDialogBox()
                        },
                        5000 // value in milliseconds
                    )
                }
            }
        })

        this.binding.recyclerViewCheckList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        addDataToList()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    //TODO : do something
                    Log.e("TestCameraX", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageUri = Uri.fromFile(photoFile)
                    val cropImageOptions = CropImageOptions()
                    cropImageOptions.imageSourceIncludeGallery = true
                    cropImageOptions.imageSourceIncludeCamera = true
                    cropImageOptions.allowRotation = true
                    val cropImageContractOptions  = CropImageContractOptions (imageUri, cropImageOptions)
                    cropImage.launch(cropImageContractOptions)
                }
            })
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_off_24)
            } else {
                camera.cameraControl.enableTorch(false)
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_on_24)
            }
        } else {
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            binding.toggleFlash.isEnabled = false
        }
    }

    private fun bindCameraUserCases() {
        val rotation = binding.cameraPreview.display.rotation

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val myRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture!!.targetRotation = myRotation
            }
        }
        orientationEventListener?.enable()

        try {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
//            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        binding.layoutCameraX.apply {
            visibility = View.VISIBLE
            setOnTouchListener{view, event ->
                true}
        }
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun uploadAllImages(list:ArrayList<ArrayList<File>>, cb:((ArrayList<ArrayList<String>>) -> Unit)) {
        var b = ArrayList<ArrayList<String>>()
        list.forEach { _ -> b.add(ArrayList()) }
        var finishCount = 0

        list.forEachIndexed { index, files ->
            uploadImages(files) { imageUrlsList ->
                b[index] = imageUrlsList
                finishCount++
                if (finishCount >= list.count()) {
                    cb(b)
                }
            }
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
//                        Log.d("TestQlCheckList", "Pass Test $responseBody")
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
        submitTaskObject.add("tasks", tasksArray)
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
//        Log.d("TestSendReport", "Check reqObject $reqObject")

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.d("TestSendReport", "Pass Test $responseBody")
                        dismissLoadingDialog()
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

    private fun showCustomPassDialogBox(){
        val title = "การส่งรายงานเสร็จสิ้น"
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_pass_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val icDialog : ImageView = dialog.findViewById(R.id.icDialog)
        val txtTitleDialog : TextView = dialog.findViewById(R.id.txtTitleDialog)

        icDialog.setImageResource(R.drawable.ic_pass)
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

    private fun uploadImages(imageFiles: ArrayList<File>, cb: ((ArrayList<String>) -> Unit)) {
        // Check if imageFiles is empty
        if (imageFiles.isEmpty()) {
            Log.e("TestImageURL", "Error: No image files to upload")
            // Handle this case accordingly
            cb.invoke(ArrayList<String>())
        }

        val retrofitServiceAPI = AllService.getURL()
        val imageUrlsList = ArrayList<String>()
        val imageParts = ArrayList<MultipartBody.Part>()

        for (file in imageFiles) {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData("images", file.name, requestFile)
            imageParts.add(imagePart)
        }

        retrofitServiceAPI.callRestAPIService(imageParts)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body()?.string()
                            val jsonObject = responseBody?.let { JSONObject(it) }
                            val imagesUrlArray = jsonObject?.getJSONArray("imagesUrl")
//                            Log.d("TestImageURL", "Check imagesUrlArray $imagesUrlArray")

                            if (imagesUrlArray != null) {
                                for (i in 0 until imagesUrlArray.length()) {
                                    val imageUrl = imagesUrlArray.getString(i)
                                    imageUrlsList.add(imageUrl)
                                }
                                Log.d("TestImageURL", "Pass Check $imageUrlsList")
                                cb.invoke(imageUrlsList)
                            }
                        } catch (e: Exception) {
                            Log.e("TestImageURL", "Error parsing response body: $e")
                        }
                    } else {
                        Log.e("TestImageURL", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("TestImageURL", "Failure: $t")
                }
            })
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

    // Function to rotate a Bitmap
    private fun rotateBitmap(source: Bitmap?, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source!!, 0, 0, source.width, source.height, matrix, true)
    }

    private fun getImageOrientation(context: Context, imageUri: Uri): Int {
        val cursor = context.contentResolver.query(imageUri, arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null)
        cursor.use { c ->
            if (c != null && c.moveToFirst()) {
                val orientationColumnIndex = c.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                if (orientationColumnIndex != -1) {
                    return c.getInt(orientationColumnIndex)
                }
            }
        }
        return 0
    }


}