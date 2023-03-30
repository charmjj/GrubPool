package cs461.g2t10.grubpool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun shareGrub(view: View) {
        val shareGroupIntent = Intent(this, ImageCaptureActivity::class.java)
        startActivity(shareGroupIntent)
    }

    fun findGrub(view: View) {
        val intent = Intent(this, FindGrubActivity::class.java)
        startActivity(intent)
    }
}