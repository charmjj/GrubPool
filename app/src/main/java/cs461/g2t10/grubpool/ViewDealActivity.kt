package cs461.g2t10.grubpool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import cs461.g2t10.grubpool.data.models.Urls
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ViewDealActivity : AppCompatActivity() {
    private lateinit var latitude: String
    private lateinit var longitude: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_deal)
        val it = intent

        // Receives an Intent with item ID
        val dealId = it.getStringExtra("dealId")

        // Query DB for item information by item ID and insert into View
        fetchFoodDealsData(dealId!!)

    }

    // Get Directions to shop via Google Maps API
    fun getDirections(view: View) {
        val myIntent = Intent(this, GetDirectionsActivity::class.java)
        myIntent.putExtra("latitude", latitude)
        myIntent.putExtra("longitude", longitude)
        startActivity(myIntent)
    }

    private fun fetchFoodDealsData(dealId: String) {
        Thread {
            val url = URL(Urls.BASE_API_ENDPOINT + "/grub-deal/" + dealId)
            val connection = url.openConnection() as HttpsURLConnection

            if (connection.responseCode == 200) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                while (true) {
                    val line = reader.readLine() ?: break
                    var dealRes = JSONArray(line).getJSONObject(0)
                    latitude = dealRes.getString("latitude")
                    longitude = dealRes.getString("longitude")

                    runOnUiThread {
                        // Insert item information into View
                        var dealImage = findViewById<ImageView>(R.id.dealImage)
                        var itemLocation = findViewById<TextView>(R.id.itemLocation)
                        var itemShopName = findViewById<TextView>(R.id.itemShopName)
                        var itemName = findViewById<TextView>(R.id.itemName)
                        var itemDesc = findViewById<TextView>(R.id.itemDesc)
                        var itemPrice = findViewById<TextView>(R.id.itemPrice)
                        var itemDiscount = findViewById<TextView>(R.id.itemDiscount)

                        itemLocation.text = dealRes.getString("location")
                        itemShopName.text = dealRes.getString("vendor")
                        itemName.text = dealRes.getString("name")
                        itemDesc.text = dealRes.getString("description")
                        itemPrice.text = "\$" + dealRes.getString("price")
                        itemDiscount.text = dealRes.getString("discount") + "%"

                        dealImage.setImageResource(R.drawable.rounded_corner)

                        Glide.with(this)
                            .load(Urls.S3_BASE_URL + dealRes.getString("image_url"))
                            .fitCenter()
                            .into(dealImage)
                        dealImage.scaleType = ImageView.ScaleType.FIT_XY

                    }
                }

                inputStream.close()
            }
        }.start()
    }
}