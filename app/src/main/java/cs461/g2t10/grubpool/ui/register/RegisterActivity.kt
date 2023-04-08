package cs461.g2t10.grubpool.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import cs461.g2t10.grubpool.data.api.DbClient
import cs461.g2t10.grubpool.databinding.ActivityRegisterBinding
import cs461.g2t10.grubpool.ui.login.LoginActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.txtLogin.setOnClickListener {
            openLoginActivity()
        }
        binding.btnRegister.setOnClickListener {
            val ob = JsonObject().apply {
                addProperty("user_id", binding.etEmail.text.toString())
                addProperty("vendor", binding.etName.text.toString())
                addProperty("location", binding.etAddress.text.toString())
                addProperty("telephone_number", binding.etPhone.text.toString())
                addProperty("password", binding.etPassword.text.toString())
            }
            registerUser(ob)
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerUser(data: JsonObject) {
        val api = DbClient.getClient()
        api.registerUser(data).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                Toast.makeText(
                    this@RegisterActivity, "Registered!", Toast.LENGTH_SHORT
                ).show()
                openLoginActivity()
            }, {
                Log.e("TAG", "registerUser: ${it.message}")
            })
    }

}