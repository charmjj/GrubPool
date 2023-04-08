package cs461.g2t10.grubpool.data.api

import com.google.gson.JsonObject
import cs461.g2t10.grubpool.data.models.FoodDeal
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DbInterface {

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