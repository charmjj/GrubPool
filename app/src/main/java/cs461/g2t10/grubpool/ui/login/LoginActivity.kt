package cs461.g2t10.grubpool.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import cs461.g2t10.grubpool.MainActivity
import cs461.g2t10.grubpool.data.api.DbClient
import cs461.g2t10.grubpool.databinding.ActivityLoginBinding
import cs461.g2t10.grubpool.ui.dealList.DealListActivity
import cs461.g2t10.grubpool.ui.dealList.DealListActivity.Companion.USER_ID
import cs461.g2t10.grubpool.ui.register.RegisterActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtRegister.setOnClickListener {
            openRegisterActivity()
        }

        binding.btnLogin.setOnClickListener {
            val ob = JsonObject().apply {
                addProperty("user_id", binding.etEmail.text.toString())
                addProperty("password", binding.etPassword.text.toString())
            }
            loginUser(ob)
        }
    }

    private fun openRegisterActivity(){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openMainActivity(userId: String){
        val intent = Intent(this, DealListActivity::class.java)
        intent.putExtra(USER_ID,userId)
        startActivity(intent)
        finish()
    }

    private fun loginUser(data: JsonObject) {
        val api = DbClient.getClient()
        api.loginUser(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val message = it.get("data")?.asString
                if(message!="Login Successful")
                {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login Failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@subscribe
                }
                Toast.makeText(
                    this@LoginActivity,
                    "Login Success!",
                    Toast.LENGTH_SHORT
                ).show()
                openMainActivity(data["user_id"].asString)
            }, {
                Log.e("TAG", "registerUser: ${it.message}")
            })
    }

}