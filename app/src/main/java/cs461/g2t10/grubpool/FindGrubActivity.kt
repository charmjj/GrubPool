package cs461.g2t10.grubpool

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.IOException

class FindGrubActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    internal lateinit var lastLocation: Location // user's actual live location
    internal var mCurrLocationMarker: Marker? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var filterPanelBehavior: BottomSheetBehavior<View?>? = null
    private var locationPanelBehavior: BottomSheetBehavior<View?>? = null

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

    private fun placeMarkerOnMap(currentLatLong: LatLng, location: String?) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        val title = location ?: "Current Location"
        markerOptions.title(title)
        mCurrLocationMarker = mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

    private fun configureFilterPanel() {
        val fragment = supportFragmentManager.findFragmentById(R.id.filterPanel)

        fragment?.let {
            BottomSheetBehavior.from(it.requireView())?.let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                // Set the trigger that will expand your view
                val filterButton = findViewById<Button>(R.id.filterButton)
                filterButton.setOnClickListener { bsb.state = BottomSheetBehavior.STATE_EXPANDED }

                // Set the reference into class attribute (will be used latter)
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

                // Set the reference into class attribute (will be used latter)
                locationPanelBehavior = bsb
            }
        }
    }

    override fun onBackPressed() {
        // With the reference of the BottomSheetBehavior stored
        filterPanelBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        } ?: onBackPressedDispatcher.onBackPressed()

        locationPanelBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
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
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                geoCoder.getFromLocationName(location, 1, object: Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        addressList = addresses
                    }
                })
            } catch (e: IllegalArgumentException) { // if locationName is null
                e.printStackTrace()
            }
        }

        val address = addressList!![0]
        val latLng = LatLng(address.latitude, address.longitude)
        locationPanelBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        placeMarkerOnMap(latLng, location)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }
}