package cs461.g2t10.grubpool

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.PutObjectResult
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList

class OcrActivity : AppCompatActivity() {
    private var logger = Logger.getLogger("OcrActivity")
    private lateinit var imageFilePath: String

    private lateinit var dealImage: ImageView
    private lateinit var itemLocation: EditText
    private lateinit var itemShopName: EditText
    private lateinit var itemName: EditText
    private lateinit var itemDesc: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemDiscount: EditText

    private var cuisineArr = ArrayList<String>()
    private var restrictionArr = ArrayList<String>()
    private var selectedCuisine = 1;
    private var selectedRestriction = 1;

    private var latitude = 0.0
    private var longitude = 0.0
    private var currLocation = ""

    // Variables associated with OCR library
    private var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.level = Level.ALL
        setContentView(R.layout.activity_ocr)

        initializeViewComponents()
        getLocationAccessPermission()
        loadCuisinesAndRestrictions()
        bindListViewItems()

        val intent = intent
        imageFilePath = intent.getStringExtra("imageFilePath").toString()

        val fileInputStream: FileInputStream = this.openFileInput(imageFilePath)
        val imageBitmap = BitmapFactory.decodeStream(fileInputStream)
        fileInputStream.close()

        dealImage.setImageBitmap(imageBitmap)
        extractTextFromImage(imageBitmap)
    }


    fun onSubmitItem(view: View) {
        uploadImageToS3()

        val dealLocation = itemLocation.text.toString().trim()
        val dealVendor = itemShopName.text.toString().trim()
        val dealName = itemName.text.toString().trim()
        val dealDesc = itemDesc.text.toString().trim()
        val dealPrice = itemPrice.text.toString().trim().toFloat()
        val dealDiscount = itemDiscount.text.toString().trim().toFloat()

        val url = "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api/grub-deal/add"
        val requestBody = JSONObject().apply {
            put("user_id", "")
            put("location", dealLocation)
            put("vendor", dealVendor)
            put("name", dealName)
            put("description", dealDesc)
            put("price", dealPrice)
            put("discount", dealDiscount)
            put("latitude", "")
            put("longitude", "")
            put("cuisine_id", selectedCuisine)
            put("restriction_id", selectedRestriction)
            put("image_url", imageFilePath)
        }
        val request = JsonObjectRequest(Request.Method.POST, url, requestBody, { response ->
            Toast.makeText(
                this, "Successfully created food deal!", Toast.LENGTH_SHORT
            ).show()
        }, { error ->
            Toast.makeText(
                this, "Snap! Something went wrong", Toast.LENGTH_SHORT
            ).show()
            Log.e("OCRActivity", "Error creating food deal: ${error.localizedMessage}")
        })

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
            val resultText = visionText.text
            logger.info("This is the text generated from the image: $resultText")
        }.addOnFailureListener { e ->
            logger.log(Level.SEVERE, "An error occurred while processing the image", e)
        }
    }

    private fun initializeViewComponents() {
        dealImage = findViewById(R.id.dealImage)
        itemLocation = findViewById(R.id.itemLocation)
        itemLocation.setText(currLocation)
        itemShopName = findViewById(R.id.itemShopName)
        itemName = findViewById(R.id.itemName)
        itemDesc = findViewById(R.id.itemDesc)
        itemPrice = findViewById(R.id.itemPrice)
        itemDiscount = findViewById(R.id.itemDiscount)
    }

    private fun getLocationAccessPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude

                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses!!.isNotEmpty()) {
                        val address = addresses[0]
                        currLocation = address.getAddressLine(0)
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }

    private fun bindListViewItems() {
        val cuisineListView: ListView = findViewById(R.id.cuisineListView)
        val cuisineAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, cuisineArr)
        cuisineListView.adapter = cuisineAdapter
        cuisineListView.choiceMode = ListView.CHOICE_MODE_SINGLE
        cuisineListView.setItemChecked(0, true)

        cuisineListView.setOnItemClickListener { parent, view, position, id ->
            selectedCuisine = position + 1
            cuisineListView.setItemChecked(position, true)
        }

        val restrictionListView: ListView = findViewById(R.id.restrictionListView)
        val restrictionAdapter = ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_single_choice, restrictionArr
        )
        restrictionListView.adapter = restrictionAdapter
        restrictionListView.choiceMode = ListView.CHOICE_MODE_SINGLE
        restrictionListView.setItemChecked(0, true)

        restrictionListView.setOnItemClickListener { parent, view, position, id ->
            selectedRestriction = position + 1
            restrictionListView.setItemChecked(position, true)
        }
    }

    private fun uploadImageToS3() {
        val credentials =
            BasicAWSCredentials("", "")
        val s3Client = AmazonS3Client(credentials, Region.getRegion("ap-southeast-1"))

        val objectMetadata = ObjectMetadata()
        val inputStream = this.openFileInput(imageFilePath)
        val objectRequest =
            PutObjectRequest("mobile-legend-thumbnails", imageFilePath, inputStream, objectMetadata)

        s3Client.putObject(objectRequest)
    }

    private fun loadCuisinesAndRestrictions() {
        val volleyQueue = Volley.newRequestQueue(this)
        val cuisineUrl = "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api/cuisines"

        val cuisineObjectResponse =
            JsonObjectRequest(Request.Method.GET, cuisineUrl, null, { response ->
                val cuisines = response.get("data") as JSONArray

                val cuisineString = ArrayList<String>()
                for (i in 0 until cuisines.length()) {
                    val item = cuisines.getJSONObject(i)
                    cuisineString.add(item["cuisine"].toString())
                }
                cuisineArr = cuisineString
            }, { error ->
                Toast.makeText(
                    this, "Some error occurred! Cannot fetch all cuisines", Toast.LENGTH_SHORT
                ).show()
                Log.e("OCRActivity", "Error loading cuisine: ${error.localizedMessage}")
            })

        val restrictionUrl =
            "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api/restrictions"
        val restrictionObjectResponse =
            JsonObjectRequest(Request.Method.GET, restrictionUrl, null, { response ->
                val restrictions = response.get("data") as JSONArray

                val restrictionString = ArrayList<String>()
                for (i in 0 until restrictions.length()) {
                    val item = restrictions.getJSONObject(i)
                    item["restriction_id"] as Integer
                    restrictionString.add(item["restriction"].toString())
                }
                restrictionArr = restrictionString
            }, { err ->
                Toast.makeText(
                    this,
                    "Some error occurred! Cannot fetch all dietary restrictions",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("OCRActivity", "Error loading dietary restrictions: ${err.localizedMessage}")
            })

        volleyQueue.add(cuisineObjectResponse)
        volleyQueue.add(restrictionObjectResponse)
    }
}