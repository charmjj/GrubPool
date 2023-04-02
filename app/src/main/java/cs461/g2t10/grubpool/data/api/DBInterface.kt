package cs461.g2t10.grubpool.data.api

import com.google.gson.JsonObject
import cs461.g2t10.grubpool.data.models.FoodDeal
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DbInterface {

//    val api "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api/grub-deal/store/charmjj";
//
//    const val S3_BASE_URL = "https://mobile-legend-thumbnails.s3.ap-southeast-1.amazonaws.com/"
//    const val BASE_API_ENDPOINT = "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api"

    @GET("grub-deal/store/{user_id}")
    fun getDeals(@Path("user_id") id: String): Single<List<FoodDeal>>

    @PUT("grub-deal/update")
    fun updateDeals(
        @Body data: JsonObject
    ): Single<JSONObject>

    @POST("store/register")
    fun registerUser(
        @Body data: JsonObject
    ): Single<JSONObject>

    @POST("store/login")
    fun loginUser(
        @Body data: JsonObject
    ): Single<JsonObject>
}