package cs461.g2t10.grubpool

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.Calendar
import java.util.logging.Level
import java.util.logging.Logger

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

    // Variables associated with OCR library
    private var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.level = Level.ALL
        setContentView(R.layout.activity_ocr)

        initializeViewComponents()

        val intent = intent
        imageFilePath = intent.getStringExtra("imageFilePath").toString()
        val fileInputStream: FileInputStream = this.openFileInput(imageFilePath)
        val imageBitmap = BitmapFactory.decodeStream(fileInputStream)
        fileInputStream.close()

        dealImage.setImageBitmap(imageBitmap)
        extractTextFromImage(imageBitmap)
    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
            val resultText = visionText.text
            logger.info("This is the text generated from the image: $resultText")
//            for (block in visionText.textBlocks) {
//                val blockText = block.text
//                val blockCornerPoints = block.cornerPoints
//                val blockFrame = block.boundingBox
//                for (line in block.lines) {
//                    val lineText = line.text
//                    val lineCornerPoints = line.cornerPoints
//                    val lineFrame = line.boundingBox
//                    for (element in line.elements) {
//                        val elementText = element.text
//                        val elementCornerPoints = element.cornerPoints
//                        val elementFrame = element.boundingBox
//                    }
//                }
//            }
        }.addOnFailureListener { e ->
            logger.log(Level.SEVERE, "An error occurred while processing the image", e)
        }
    }


    // The recommended way to persist data onto your database is through a RESTful API endpoint
    fun onSubmitItem(view: View) {
//        TODO("Figure out a way to store the deal image")
        val dealLocation = itemLocation.text.toString().trim()
        val dealVendor = itemShopName.text.toString().trim()
        val dealName = itemName.text.toString().trim()
        val dealDesc = itemDesc.text.toString().trim()
        val dealPrice = itemPrice.text.toString().trim().toFloat()
        val dealDiscount = itemDiscount.text.toString().trim().toFloat()
        val currentTimestamp = Calendar.getInstance().time

        val conn = getDatabaseConnection()
        val statement = conn.createStatement()
        val query =
            "INSERT INTO grub_deals(user_id, deal_location, deal_vendor, deal_name, deal_description, deal_price, deal_discount, deal_date) VALUES ('fizzbuzz', '$dealLocation', '$dealVendor', '$dealName', '$dealDesc', '$dealPrice', '$dealDiscount', '$currentTimestamp');"
        statement.executeUpdate(query)
        statement.close()
        conn.close()
    }

    private fun getDatabaseConnection(): Connection {
        val url = ""
        return DriverManager.getConnection(url)
    }

    private fun initializeViewComponents() {
        dealImage = findViewById(R.id.dealImage)
        itemLocation = findViewById(R.id.itemLocation)
        itemShopName = findViewById(R.id.itemShopName)
        itemName = findViewById(R.id.itemName)
        itemDesc = findViewById(R.id.itemDesc)
        itemPrice = findViewById(R.id.itemPrice)
        itemDiscount = findViewById(R.id.itemDiscount)
    }
}