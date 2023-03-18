package cs461.g2t10.grubpool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun shareGrub() {
        val shareGroupIntent = Intent(this, ImageCaptureActivity::class.java)
        startActivity(shareGroupIntent)
    }
}