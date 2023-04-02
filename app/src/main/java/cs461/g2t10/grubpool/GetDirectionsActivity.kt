package cs461.g2t10.grubpool

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import cs461.g2t10.grubpool.databinding.ActivityGetDirectionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class GetDirectionsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityGetDirectionsBinding
    private lateinit var origin: String
    private lateinit var destination: String
    private lateinit var apiKey: String
    private lateinit var oriLat: String
    private lateinit var oriLong: String
    private lateinit var desLat: String
    private lateinit var desLong: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val it = intent
        desLat = it.getStringExtra("latitude").toString()
        desLong = it.getStringExtra("longitude").toString()

        binding = ActivityGetDirectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // TODO: retrieve MAPS_API_KEY from local.properties
//        val properties = Properties()
//        val localProperties = File("/local.properties")
//        println(localProperties.absolutePath)
//
//        if (localProperties.exists()) {
//            localProperties.inputStream().use { properties.load(it) }
//        } else {
//            println("helphelphelp")
//        }
//        apiKey = properties.getProperty("MAPS_API_KEY")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtain current location's coordinates
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    oriLat = location.latitude.toString()
                    oriLong = location.longitude.toString()
                    println(oriLat)
                    println(oriLong)
                    getDirections(mMap)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    private fun getDirections(googleMap: GoogleMap) {
        origin = "$oriLat,$oriLong"
        destination = "$desLat,$desLong"
        apiKey = "AIzaSyCJUfpuEzgmkUnxmB9A3zFl5G4YtPMWmNk"
        println(origin)
        println(destination)

        val urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$origin" +
                "&destination=$destination" +
                "&key=$apiKey"

        // Perform network operation on separate thread by using Kotlin Coroutines
        GlobalScope.launch {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream

            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            reader.close()
            val jsonResponse = stringBuilder.toString()
            val jsonObject = JSONObject(jsonResponse)
            val routes = jsonObject.getJSONArray("routes")
            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)
            val steps = leg.getJSONArray("steps")

            // Create a polyline to draw the route on the map
            val polylineOptions = PolylineOptions().width(10f).color(Color.BLUE)
            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val points = step.getJSONObject("polyline").getString("points")
                val decodedPoints = decodePolyline(points)
                for (point in decodedPoints) {
                    polylineOptions.add(point)
                }
            }

            // Use Main thread for UI updates
            withContext(Dispatchers.Main) {
                mMap.addPolyline(polylineOptions)

                // Add markers to the map to indicate the start and end points of the route
                val startLocation = leg.getJSONObject("start_location")
                val endLocation = leg.getJSONObject("end_location")
                val startLatLng = LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"))
                val endLatLng = LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"))
                mMap.addMarker(MarkerOptions().position(startLatLng).title("Your Location"))
                // TODO: Replace marker title with shop name
                mMap.addMarker(MarkerOptions().position(endLatLng).title("Your Destination"))

                // Set the camera position to center on the route
                val boundsBuilder = LatLngBounds.builder()
                boundsBuilder.include(startLatLng)
                boundsBuilder.include(endLatLng)
                val bounds = boundsBuilder.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                mMap.moveCamera(cameraUpdate)
            }
        }
    }

    // Helper function to decode a polyline string into a list of LatLng points
    private fun decodePolyline(polyline: String): List<LatLng> {
        val points = mutableListOf<LatLng>()
        var index = 0
        val len = polyline.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            points.add(latLng)
        }
        return points
    }

    fun openMaps(view: View) {
        val uri = "http://maps.google.com/maps?saddr=$origin&daddr=$destination"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}