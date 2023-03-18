package cs461.g2t10.grubpool

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.logging.Level
import java.util.logging.Logger

class OcrActivity : AppCompatActivity() {
    private var logger = Logger.getLogger("OcrActivity")

    // Variables associated with OCR library
    private var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.level = Level.ALL
        setContentView(R.layout.activity_ocr)
    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        logger.info("Extracting text from image...")
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
            val resultText = visionText.text
            logger.info("This is the text generated from the image: $resultText")
            for (block in visionText.textBlocks) {
                val blockText = block.text
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines) {
                    val lineText = line.text
                    val lineCornerPoints = line.cornerPoints
                    val lineFrame = line.boundingBox
                    for (element in line.elements) {
                        val elementText = element.text
                        val elementCornerPoints = element.cornerPoints
                        val elementFrame = element.boundingBox
                    }
                }
            }
        }.addOnFailureListener { e ->
            logger.log(Level.SEVERE, "An error occurred while processing the image", e)
        }
    }
}