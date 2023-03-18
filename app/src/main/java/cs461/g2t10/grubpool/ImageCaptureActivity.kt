package cs461.g2t10.grubpool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class ImageCaptureActivity : AppCompatActivity() {
    private var logger = Logger.getLogger("ImageCaptureActivity")
    private val cameraRequest = 100
    private lateinit var imageView: ImageView
    lateinit var imageBitmap: Bitmap
    lateinit var currentPhotoPath: String
    lateinit var captureImageButton: Button
    lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.level = Level.ALL
        setContentView(R.layout.activity_image_capture)

        imageView = findViewById(R.id.imageDisplay)
        continueButton = findViewById(R.id.continueButton)
        continueButton.isEnabled = false

        captureImageButton = findViewById(R.id.snapButton)
        captureImageButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        }

        if (ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, cameraRequest)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA), cameraRequest
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest && resultCode == RESULT_OK) {
            var capturedImageBitmap = data!!.extras!!.get("data") as Bitmap
            capturedImageBitmap = increaseImageResolution(capturedImageBitmap)
            imageView.setImageBitmap(capturedImageBitmap)
            imageBitmap = capturedImageBitmap

            captureImageButton.text = "Retake"
            continueButton.isEnabled = true
        } else {
            Toast.makeText(
                this,
                "Permission denied for camera access, you can allow it in the settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onContinue(view: View) {
        val ocrIntent = Intent(this, OcrActivity::class.java)
        generateFilename()

        val stream: FileOutputStream = this.openFileOutput(currentPhotoPath, Context.MODE_PRIVATE)
        val bitmap: Bitmap = imageBitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        ocrIntent.putExtra("imageFilePath", currentPhotoPath)
        startActivity(ocrIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraRequest && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        } else {
            Toast.makeText(
                this,
                "Permission denied for camera access, you can allow it in the settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun generateFilename() {
        // Create an image file name
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        currentPhotoPath = "YUMMERZ_${timestamp}_.png"
    }

    private fun increaseImageResolution(image: Bitmap): Bitmap {
        val width = image.width * 2
        val height = image.height * 2
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}