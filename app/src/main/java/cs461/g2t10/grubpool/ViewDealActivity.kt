package cs461.g2t10.grubpool

import android.content.Intent
import android.net.Uri
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
        itemLocation.text = "181 Orchard Road Orchard Central, Singapore 238896"
        itemShopName.text = "Don Don Donki Orchard Central"
        itemName.text = "Omu Rice Set (Pork Contained)"
        itemDesc.text = "Japanese Omelette Rice with Pork"
        itemPrice.text = "\$4.50"
        itemDiscount.text = "40%"

    }

    // Get Directions to shop via Google Maps API
    fun getDirections(view: View) {
        val myIntent = Intent(this, GetDirectionsActivity::class.java)
        myIntent.putExtra("dealId", "")
        startActivity(myIntent)

    }
}