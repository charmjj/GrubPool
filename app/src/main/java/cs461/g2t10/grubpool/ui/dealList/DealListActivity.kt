package cs461.g2t10.grubpool.ui.dealList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cs461.g2t10.grubpool.ImageCaptureActivity
import cs461.g2t10.grubpool.R
import cs461.g2t10.grubpool.data.api.BASE_API_ENDPOINT
import cs461.g2t10.grubpool.data.api.DbClient
import cs461.g2t10.grubpool.data.api.DbInterface
import cs461.g2t10.grubpool.data.models.CuisineModel
import cs461.g2t10.grubpool.data.models.FoodDeal
import cs461.g2t10.grubpool.data.models.RestrictionModel
import cs461.g2t10.grubpool.data.models.Urls
import cs461.g2t10.grubpool.databinding.ActivityDealListBinding
import cs461.g2t10.grubpool.databinding.DialogLayoutEditDealBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class DealListActivity : AppCompatActivity(), DealsAdapter.ClickListener {
    private var adapter: DealsAdapter? = null
    private lateinit var binding: ActivityDealListBinding
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra(USER_ID).orEmpty()

        setupRecyclerView()

        fetchFoodDealsData(userId)

        // For Ruwan's ocrx button for onlick
        val addDeal: com.google.android.material.floatingactionbutton.FloatingActionButton =
            findViewById(R.id.addDeal)
        addDeal.setOnClickListener {
            val intent = Intent(this, ImageCaptureActivity::class.java)
            intent.putExtra(USER_ID, userId)
            startActivity(intent)
//            finish()
        }

    }

    private fun setupRecyclerView() {
        adapter = DealsAdapter()
        adapter?.listener = this
        binding.rvDeals.layoutManager = LinearLayoutManager(this)
        binding.rvDeals.adapter = adapter
    }

    private fun fetchFoodDealsData(id: String) {
        val api = DbClient.getClient()
        api.getDeals(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter?.addItems(it)
            }, {
                Log.e("TAG", "onCreate: ${it.message}")

            })
    }

    override fun onClick(data: FoodDeal) {
        showDialog(data)
    }

    private fun showDialog(data: FoodDeal) {
        val builder = AlertDialog.Builder(this)
        val binding = DialogLayoutEditDealBinding.inflate(layoutInflater, null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.etName.setText(data.name)
        binding.etDesc.setText(data.description)
        binding.etWas.setText("${data.price}")
        binding.etDiscount.setText("${data.discount}")

        val cuisineList = listOf(
            CuisineModel(1, "Japanese"),
            CuisineModel(2, "Chinese"),
            CuisineModel(3, "Italian"),
            CuisineModel(4, "Greek"),
            CuisineModel(5, "Spanish"),
            CuisineModel(6, "Others"),
        )
        val restrictionList = listOf(
            RestrictionModel(1, "Halal"),
            RestrictionModel(2, "Vegetarian"),
            RestrictionModel(3, "No Restrictions"),
            RestrictionModel(4, "No Beef"),
        )

        val cuisineAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, cuisineList
        )
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCousin.adapter = cuisineAdapter

        val restrictionAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, restrictionList
        )
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRestriction.adapter = restrictionAdapter


        binding.btnUpdate.setOnClickListener {
            val ob = JsonObject().apply {
                addProperty("deal_id", data.dealId)
                addProperty("user_id", userId)
                addProperty("name", binding.etName.text.toString())
                addProperty("description", binding.etDesc.text.toString())
                addProperty("price", binding.etWas.text.toString())
                addProperty("discount", binding.etDiscount.text.toString())
                add("cuisine_ids", JsonArray().apply {
                    add(cuisineList[binding.spCousin.selectedItemPosition].cuisine_id)
                })
                add("restriction_ids", JsonArray().apply {
                    add(
                        restrictionList[binding.spRestriction.selectedItemPosition].restriction_id
                    )
                })
            }
            updateFoodDealsData(ob)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateFoodDealsData(data: JsonObject) {
        val api = DbClient.getClient()
        api.updateDeals(data).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(
                    this@DealListActivity, "Updated!", Toast.LENGTH_SHORT
                ).show()
                fetchFoodDealsData(userId)
            }, {
                Log.e("TAG", "showDialog: ${it.message}")
            })
    }

    companion object {
        const val USER_ID = "USER_ID"
    }
}