package cs461.g2t10.grubpool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ViewDealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_deal)

        // Receives an Intent with item ID

        // Query DB for item information by item ID

        // Insert item information into View
        var dealImage = findViewById<ImageView>(R.id.dealImage)
        var itemLocation = findViewById<TextView>(R.id.itemLocation)
        var itemShopName = findViewById<TextView>(R.id.itemShopName)
        var itemName = findViewById<TextView>(R.id.itemName)
        var itemDesc = findViewById<TextView>(R.id.itemDesc)
        var itemPrice = findViewById<TextView>(R.id.itemPrice)
        var itemDiscount = findViewById<TextView>(R.id.itemDiscount)

        dealImage.setImageResource(R.drawable.rounded_corner)
        itemLocation.text = ""
        itemShopName.text = ""
        itemName.text = ""
        itemDesc.text = ""
        itemPrice.text = ""
        itemDiscount.text = ""

    }

    // Get Directions to shop via Google Maps API
    fun getDirections(view: View) {

    }
}