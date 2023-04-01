package cs461.g2t10.grubpool.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class FoodDeal(@SerializedName("deal_id") val dealId: String, @SerializedName("user_id") val userId: String) {
    var location: String? = null
    var vendor: String? = null
    var name: String? = null
    var description: String? = null
    var price: Double = 0.0
    var discount: Double = 0.0
    var date: Date? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var cuisine: String? = null
    var restriction: String? = null
    @SerializedName("image_url")
    var imageUrl: String? = null
}