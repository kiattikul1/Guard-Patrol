package com.example.guard_patrol.Activity

import BasedActivity
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.guard_patrol.Class.ScanQrClass
import com.example.guard_patrol.Class.TokenClass
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.ActivityScannerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.Locale


class ScannerActivity : BasedActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var myLatitude : Double? = null
    private var myLongitude : Double? = null
    private var qrWorkspaceId : String? = null
    private var qrLatitude : Double? = null
    private var qrLongitude : Double? = null
    private val setDistance : Double = 200.0
    private var pointId : String? = null
    private var pointName : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        codeScanner = CodeScanner(this, binding.scannerView)
        activityResultLauncher.launch(arrayOf(Manifest.permission.CAMERA))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun cancelScan() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setupQRScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS

        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                try {
                    val scanResult = it.text
                    Log.d("TestResult","Check $scanResult")

                    getTokenQR(scanResult){ qrCorrect ->
                        if (qrCorrect){
                            getLocation()
                        }else{
                            //Error QR
                            val title = "รูปแบบ QR โค้ดไม่ถูกต้อง"
                            val message = "กรุณาลองใหม่อีกครั้ง"
                            showCustomErrorDialogBox(title ,message ,R.drawable.ic_error)
                        }
                    }

//                    val json = JSONObject(scanResult)
////                    Log.d("TestResultJsonObject","Check $jsonObject")
//                    if (json.has("workspace_id") && json.has("point_id") && json.has("point_name") && json.has("lat") && json.has("lng")) {
//                        qrWorkspaceId = json.getString("workspace_id")
//                        pointId = json.getString("point_id")
//                        pointName = json.getString("point_name")
//                        qrLatitude = json.getDouble("lat")
//                        qrLongitude = json.getDouble("lng")
//                        getLocation()
//                    } else {
//                        //Error QR
//                        val title = "รูปแบบ QR โค้ดไม่ถูกต้อง"
//                        val message = "กรุณาลองใหม่อีกครั้ง"
//                        showCustomErrorDialogBox(title ,message ,R.drawable.ic_error)
//                    }
                } catch (e: JSONException) {
                    //Error QR
                    val title = "รูปแบบ QR โค้ดไม่ถูกต้อง"
                    val message = "กรุณาลองใหม่อีกครั้ง"
                    showCustomErrorDialogBox(title ,message ,R.drawable.ic_error)
//                    Toast.makeText(this, "Invalid QR Code content: Malformed JSON", Toast.LENGTH_LONG).show()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread { Toast.makeText(this, "Error Scanner: ${it.message}",Toast.LENGTH_LONG).show() }
        }
        codeScanner.startPreview()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            permissions.entries.forEach{
                val isGranted = it.value
                if(isGranted){
                    setupQRScanner()
                }else{
                    Toast.makeText(applicationContext,"Please allow access to the camera.",Toast.LENGTH_SHORT).show()
                }
            }

    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        myLatitude = list[0].latitude
                        myLongitude = list[0].longitude
//                        Log.d("TestMyLocation", "check $myLatitude -- $myLongitude")
//                        Log.d("TestLocation", "Latitude\\n${list[0].latitude}")
//                        Log.d("TestLocation", "Longitude\\n${list[0].longitude}")
                        val distance = calculateDistance(qrLatitude!!, qrLongitude!!, myLatitude!!, myLongitude!!)
                        validateQR(qrWorkspaceId!!,distance.toDouble())
//                        Log.d("TestQRLat", "check $qrLatitude")
//                        Log.d("TestQRLng", "check $qrLongitude")
//                        Log.d("TestDistance", "check $distance meters")
//                        Log.d("TestLocation", "Country Name\\n${list[0].countryName}")
//                        Log.d("TestLocation", "Locality\\n${list[0].locality}")
//                        Log.d("TestLocation", "Address\\n${list[0].getAddressLine(0)}")
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun getTokenQR(scanResult: String,cb: ((qrCorrect: Boolean) -> Unit)){
        val paramObject = JsonObject()
        paramObject.addProperty("tokenQr", scanResult)
        val query: String = "query ScanQrcode(\$tokenQr: String!) {\n" +
                "  scanQrcode(token_qr: \$tokenQr) {\n" +
                "    workspace_id\n" +
                "    point_id\n" +
                "    point_name\n" +
                "    lat\n" +
                "    lng\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", paramObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val gson = Gson()
                        val scanQrClass = gson.fromJson(responseBody, ScanQrClass::class.java)
                        val qrValue = scanQrClass.data?.scanQrcode
                        Log.d("TestScanQR", "Pass Test $qrValue")

                        qrWorkspaceId = qrValue?.workspaceId
                        pointId = qrValue?.pointId
                        pointName = qrValue?.pointName
                        qrLatitude = qrValue?.lat?.toDouble()
                        qrLongitude = qrValue?.lng?.toDouble()

                        cb.invoke(
                            qrWorkspaceId != null &&
                                    pointId != null &&
                                    pointName != null &&
                                    qrLatitude != null &&
                                    qrLongitude != null
                        )

                    } catch (e: Exception) {
                        Log.e("TestScanQR", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestScanQR", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestScanQR","Error $t")
            }
        })
    }

    private fun validateQR(qrWorkspaceId: String,distance : Double){
        val valuesList = workspacePreference.getPreferences()
        if (qrWorkspaceId == valuesList[0] && distance <= setDistance) {
            // Pass
            val title = "การสแกนเสร็จสิ้น"
            showCustomPassDialogBox(title ,R.drawable.ic_pass)
        } else {
            // Error Location
            val title = "คุณไม่ได้อยู่ในพื้นที่"
            val message = "โปรดตรวจสอบว่าคุณอยู่ในพื้นที่ของ QR โค้ด"
            val iconResId = R.drawable.ic_location_slash
            showCustomErrorDialogBox(title, message, iconResId)
        }
    }

    //Find distance between user and QR
    private fun calculateDistance(latitudeA: Double, longitudeA: Double, latitudeB: Double, longitudeB: Double): Float {
        val startPoint = Location("QR location")
        startPoint.latitude = latitudeA
        startPoint.longitude = longitudeA

        val endPoint = Location("My Location")
        endPoint.latitude = latitudeB
        endPoint.longitude = longitudeB

        return startPoint.distanceTo(endPoint)
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
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("PointId", pointId)
            intent.putExtra("PointName", pointName);
            startActivity(intent)
            dialog.dismiss()
        }, 1000)
    }

}