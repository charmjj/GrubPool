package cs461.g2t10.grubpool

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.time.Instant
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FindGrubActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val imageBaseUrl: String = Urls.S3_BASE_URL

    private lateinit var mMap: GoogleMap
    internal lateinit var lastLocation: Location // user's actual live location
    internal var mCurrLocationMarker: Marker? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    public var filterPanelBehavior: BottomSheetBehavior<View?>? = null
    private var locationPanelBehavior: BottomSheetBehavior<View?>? = null
    private var foodDeals: List<FoodDeal> = listOf() // stores ALL deals for now
    private var filteredDeals: List<FoodDeal> = listOf()

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_grub)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        configureFilterPanel()
        configureLocationPanel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
        fetchFoodDealsData().start()
    }

    @SuppressLint("MissingPermission")
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong, null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            } // TODO: else set arbitrary location
        }
     }

    fun placeMarkerOnMap(currentLatLong: LatLng, location: String?) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        val title = location ?: "Current Location"
        markerOptions.title(title)
        runOnUiThread {
            mCurrLocationMarker = mMap.addMarker(markerOptions)
        }
    }

    private fun fetchFoodDealsData(): Thread {
        return Thread {
            val url = URL(Urls.BASE_API_ENDPOINT + "/grub-deals")
            val connection = url.openConnection() as HttpsURLConnection

            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                foodDeals = Gson().fromJson(inputStreamReader, Array<FoodDeal>::class.java).toMutableList() as ArrayList<FoodDeal>
                displayFoodDealMarkers(foodDeals)

                inputStreamReader.close()
                inputSystem.close()
            }
        }

    }

    private fun displayFoodDealMarkers(foodDeals: List<FoodDeal>) {
        for (foodDeal in foodDeals) {
            val latLong = LatLng(foodDeal.latitude, foodDeal.longitude)
            var markerOptions: MarkerOptions? = null
            if (foodDeal.imageUrl != null) {
                val url = URL(imageBaseUrl + foodDeal.imageUrl)
                val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                val bmp = Bitmap.createScaledBitmap(image, 85, 85, false)
                markerOptions =
                    MarkerOptions()
                        .position(latLong)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
            } else {
                val image = BitmapFactory.decodeResource(resources, R.drawable.dish)
                val bmp = Bitmap.createScaledBitmap(image, 85, 85, false)
                markerOptions =
                    MarkerOptions()
                        .position(latLong)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
            }
            runOnUiThread {
                val title = foodDeal.location ?: ""
                markerOptions.title(title)
                mMap.addMarker(markerOptions)
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

    private fun configureFilterPanel() {
        val fragment = supportFragmentManager.findFragmentById(R.id.filterPanel)

        fragment?.let {
            BottomSheetBehavior.from(it.requireView())?.let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN
                bsb.isFitToContents = false
                // Set the trigger that will expand your view
                val filterButton = findViewById<Button>(R.id.filterButton)
                filterButton.setOnClickListener { bsb.state = BottomSheetBehavior.STATE_EXPANDED }

                // Set the reference into class attribute (will be used later)
                filterPanelBehavior = bsb
            }
        }
    }

    private fun configureLocationPanel() {
        val fragment = supportFragmentManager.findFragmentById(R.id.locationPanel)

        fragment?.let {
            BottomSheetBehavior.from(it.requireView())?.let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                // Set the trigger that will expand your view
                val filterButton = findViewById<Button>(R.id.setLocationButton)
                filterButton.setOnClickListener { bsb.state = BottomSheetBehavior.STATE_EXPANDED }

                // Set the reference into class attribute (will be used later)
                locationPanelBehavior = bsb
            }
        }
    }

    override fun onBackPressed() {
        // With the reference of the BottomSheetBehavior stored
        filterPanelBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_HIDDEN
                return
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        } ?: onBackPressedDispatcher.onBackPressed()

        locationPanelBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_HIDDEN
                return
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        } ?: onBackPressedDispatcher.onBackPressed()
    }

//    override fun onLocationChanged(location: Location) {
//        lastLocation = location
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker!!.remove()
//        }
//        val latLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions()
//        markerOptions.position(latLng)
//        markerOptions.title("Current Position")
//
//        mCurrLocationMarker = mMap!!.addMarker(markerOptions)
//        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(11f))
//
////        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//    }

    fun searchLocation(location: String) {
        if (location == null || location == "") {
            Toast.makeText(applicationContext, "Provide location!", Toast.LENGTH_SHORT).show()
            return
        }
        var addressList: List<Address>? = null
        val geoCoder = Geocoder(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
                val address = addressList!![0]
                updateLocationOnMap(location, address)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                geoCoder.getFromLocationName(location, 1, object: Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        addressList = addresses
                        val address = addressList!![0]
                        updateLocationOnMap(location, address)
                    }
                    override fun onError(errorMessage: String?) {
                        print(errorMessage!!)
                    }

                })
            } catch (e: IllegalArgumentException) { // if locationName is null
                e.printStackTrace()
            }
        }
    }

    fun updateLocationOnMap(location: String, address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        locationPanelBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        if (mCurrLocationMarker != null) {
            runOnUiThread {
                mCurrLocationMarker!!.remove()
            }
        }
        placeMarkerOnMap(latLng, location)
        runOnUiThread {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    fun filterDeals(currentSelections: HashMap<String, MutableList<String>>): Thread { // {Cuisine=[All Cuisines], Time Listed=[All Time], Dietary Restrictions=[No Restrictions]}
        mMap.clear()
        return Thread {
            val cuisines = currentSelections["Cuisine"] as List<String>
            val restrictions = currentSelections["Dietary Restrictions"] as List<String>
            val timeListed = currentSelections["Time Listed"]?.get(0)
            filteredDeals = foodDeals.filter {
                val cuisinesPass = if (cuisines.contains("All Cuisines")) true else cuisines.contains(it.cuisine)
                val restrictionsPass = if (restrictions.contains("No Restrictions")) true else restrictions.contains(it.restriction)
                cuisinesPass && restrictionsPass && compareTime(it.date)
            }
            displayFoodDealMarkers(filteredDeals)
        }
    }

    private fun compareTime(date: Date?): Boolean {
        Log.d("DATEEEE", date.toString()) // Sat Mar 25 12:04:14 GMT 2023
        val currentDate = Date()
        return true
    }

}