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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.squareup.picasso.Picasso
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
    private lateinit var myLocation: LatLng
    private lateinit var clusterManager: ClusterManager<PatrolPointClass>
    private val PERMISSION_REQUEST_CODE = 123

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
                        Log.d("TestHomePatrol", "Check patrolPointList $patrolPointList")

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

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    myLocation = LatLng(location.latitude, location.longitude)
                    val myMarker: MarkerOptions = MarkerOptions()
                        .position(myLocation)
                        .title("คุณอยู่ตรงนี้")
                        .icon(BitmapUtils.bitmapFromVector(requireContext(),R.drawable.user_marker))
                    gMap.addMarker(myMarker)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20f))

                    // Set up clustering
                    clusterManager = ClusterManager(requireContext(), gMap)
                    gMap.setOnCameraIdleListener(clusterManager)
                    gMap.setOnMarkerClickListener(clusterManager)
                    clusterManager.renderer = CustomClusterRenderer(requireContext(), gMap, clusterManager)
                    addItems()
                }
            }
        }
    }

    private fun addItems() {
        var count = 0
        //TODO : Change get image from api
        for (point in patrolPointList) {
            val lat = point.lat?.toDouble()
            val lng = point.lng?.toDouble()
            val title = point.pointName
            val imageUrl: String = if (count == 1){
                "https://firebasestorage.googleapis.com/v0/b/guardpatrol-e250b.appspot.com/o/images%2F6bd00aa5-675a-40bb-80a5-bd76b7657440.jpg?alt=media"
            }else{
                "https://firebasestorage.googleapis.com/v0/b/guardpatrol-e250b.appspot.com/o/images%2Fbd861fe5-f79f-4916-a3b1-698af4fbb4a8.jpg?alt=media"
            }
            val offsetItem = PatrolPointClass(lat!!, lng!!, title!!, imageUrl)
            clusterManager.addItem(offsetItem)
            count++
        }

        clusterManager.markerCollection.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View? {
                val markerView = layoutInflater.inflate(R.layout.custom_info_window_layout, null)
                val imageView = markerView.findViewById<ImageView>(R.id.imageView)
                val titleView = markerView.findViewById<TextView>(R.id.title)
                try {
                    titleView.text = marker.title
                    Picasso.get()
                        .load(marker.snippet)
                        .error(R.mipmap.ic_launcher)
                        .into(imageView)
                }catch(e: Exception){
                    Log.e("TestAddInfoMarker", "Error : $e")
                }
                return markerView
            }
        })
        clusterManager.cluster()
    }

    // Change image to bitmap for create marker
    object BitmapUtils {
        fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
            val vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId
            )
            vectorDrawable!!.setBounds(
                0,  0, vectorDrawable.intrinsicWidth,
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
    }

    @SuppressLint("PotentialBehaviorOverride")
        class CustomClusterRenderer(
        private val context: Context,
        map: GoogleMap?,
        clusterManager: ClusterManager<PatrolPointClass>?
    ) : DefaultClusterRenderer<PatrolPointClass>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: PatrolPointClass, markerOptions: MarkerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions)
            val bitmapDescriptor = BitmapUtils.bitmapFromVector(context, R.drawable.ic_task)
            markerOptions.icon(bitmapDescriptor)
        }

        // Change cluster color
        override fun getColor(clusterSize: Int): Int {
            return Color.rgb(255, 114, 51)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<PatrolPointClass>): Boolean {
            return cluster.size > 1
        }
    }

    inner class PatrolPointClass(
        private val lat: Double,
        private val lng: Double,
        private val title: String,
        private val imageUrl: String
    ) : ClusterItem {

        override fun getPosition(): LatLng {
            return LatLng(lat, lng)
        }

        override fun getTitle(): String {
            return title
        }

        override fun getSnippet(): String {
            return imageUrl
        }

        fun getImageUrl(): String {
            return imageUrl
        }

    }
}