package com.example.guard_patrol.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.guard_patrol.Activity.HistoryActivity
import com.example.guard_patrol.Activity.ScannerActivity
import com.example.guard_patrol.Activity.SelectWorkspaceActivity
import com.example.guard_patrol.Class.HomeClass
import com.example.guard_patrol.Class.Patrols
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


class HomeFragment : Fragment(), OnMapReadyCallback{

    private lateinit var binding: FragmentHomeBinding  // Declare the binding variable
    private lateinit var tokenPreference: TokenPref
    private lateinit var workspacePreference: WorkspacePref
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var valuesList : ArrayList<String>
    private var patrolPointList = ArrayList<Patrols>()
    private lateinit var gMap : GoogleMap
    private lateinit var map : FrameLayout
    private lateinit var clusterManager: ClusterManager<MyItem>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestLocationPermissions()
        // Inflate the layout for this fragment using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        tokenPreference = TokenPref(requireContext())
        workspacePreference = WorkspacePref(requireContext())

        binding.selectWorkspace.setOnClickListener {
            val intent = Intent(requireContext(), SelectWorkspaceActivity::class.java)
            startActivity(intent)
        }
        binding.buttonScan.setOnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            startActivity(intent)
        }
        binding.buttonHistory.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // Get value SharedPreferences
        valuesList = workspacePreference.getPreferences()
        binding.txtWorkspace.text = valuesList[1]
        binding.txtWorkspace.setTextColor(Color.WHITE)
        binding.txtWorkspace.alpha = 1F

        getPatrolPoint(){
            map = binding.map
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }

        return binding.root
    }

    private fun getPatrolPoint(cb: (() -> Unit)){
        val paramObject = JsonObject()
        paramObject.addProperty("workspaceId", valuesList[0])
        val query: String = "query PatrolHome(\$workspaceId: ID!) {\n" +
                "  patrolHome(workspace_id: \$workspaceId) {\n" +
                "    workspace\n" +
                "    patrols {\n" +
                "      points_id\n" +
                "      point_name\n" +
                "      lat\n" +
                "      lng\n" +
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

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
//                        Log.d("TestHomePatrol", "Pass Test $responseBody")
                        val gson = Gson()
                        val taskResponse = gson.fromJson(responseBody, HomeClass::class.java)
                        val listPatrolPoint = taskResponse.data?.patrolHome?.patrols
//                        Log.d("TestHomePatrol", "Check listPatrolPoint $listPatrolPoint")
                        for (point in listPatrolPoint!!){
                            patrolPointList.add(point)
                        }
                        cb.invoke()
//                        Log.d("TestHomePatrol", "Check patrolPointList $patrolPointList")

                    } catch (e: Exception) {
                        Log.e("TestHomePatrol", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestHomePatrol", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestHomePatrol","Error $t")
            }
        })
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        this.gMap = googleMap
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val myLocation = LatLng(location.latitude, location.longitude)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))

                    val myMarker: MarkerOptions = MarkerOptions()
                        .position(myLocation).title("คุณอยู่ตรงนี้")
                        //TODO : Change icon
                        .icon(bitmapFromVector(requireContext(),R.drawable.ic_my_location))

                    gMap.addMarker(myMarker)
                }

                setUpCluster()
                val renderer = CustomClusterRenderer(requireContext(), googleMap, clusterManager)
                clusterManager.renderer = renderer
            }
        }
    }

    // Request location permissions if not granted
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpCluster() {
        clusterManager = ClusterManager(requireContext(), gMap)
        gMap.setOnCameraIdleListener(clusterManager)
        gMap.setOnMarkerClickListener(clusterManager)
        addItems()
    }


    private fun addItems() {
        for (point in patrolPointList) {
            val pointName = point.pointName
            val lat = point.lat?.toDouble()
            val lng = point.lng?.toDouble()
            val patrolPoint = LatLng(lat!!, lng!!)
            val pointMarker: MarkerOptions = MarkerOptions().position(patrolPoint).title(pointName.toString())
            pointMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
//            gMap.addMarker(pointMarker)
            //TODO : Change pointName and snippet
            val title = pointName!!
            val snippet = "and this is the snippet."
            val offsetItem = MyItem(lat, lng, title, snippet)
            clusterManager.addItem(offsetItem)
        }
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(
            context, vectorResId
        )
        vectorDrawable!!.setBounds(
            0, 0, vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    class CustomClusterRenderer(
        //TODO : add color for Cluster
        context: Context?,
        map: GoogleMap?,
        clusterManager: ClusterManager<MyItem>?
    ) : DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions)
            // Set custom marker icon here
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        }
    }

    inner class MyItem(
        lat: Double,
        lng: Double,
        title: String,
        snippet: String
    ) : ClusterItem {

        private val position: LatLng
        private val title: String
        private val snippet: String

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String {
            return title
        }

        override fun getSnippet(): String {
            return snippet
        }

        init {
            position = LatLng(lat, lng)
            this.title = title
            this.snippet = snippet
        }
    }


}