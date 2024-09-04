package co.ke.foxlysoft.budgetgain

import SettingsManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.ke.foxlysoft.budgetgain.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var settingsManager: SettingsManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        settingsManager = SettingsManager(this)

        val savedUsername = settingsManager.get("username")

        if (savedUsername == null) {
            binding.confirmPasswordLayout.visibility = View.VISIBLE
            binding.btnRegisterAcc.visibility = View.VISIBLE
            binding.registerHelper.visibility = View.VISIBLE

            binding.loginBtn.visibility = View.GONE
        } else {
            binding.confirmPasswordLayout.visibility = View.GONE
            binding.btnRegisterAcc.visibility = View.GONE
            binding.registerHelper.visibility = View.GONE

            binding.loginBtn.visibility = View.VISIBLE
        }
        binding.invalidCredentialsTxt.visibility = View.GONE

        binding.loginBtn.setOnClickListener {
            var hasError = false
            // clear error messages
            binding.invalidCredentialsTxt.visibility = View.GONE
            binding.usernameLayout.error = null
            binding.passwordLayout.error = null

            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (username.isEmpty()) {
                binding.usernameLayout.error = "Username cannot be empty"
                hasError = true
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password cannot be empty"
                hasError = true
            }

            if (!hasError) {
                if (savedUsername != username) {
                    binding.invalidCredentialsTxt.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                val isPasswordCorrect = settingsManager.verifyHashed("password", password)
                if (!isPasswordCorrect) {
                    binding.invalidCredentialsTxt.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                val isFirstTime = settingsManager.get("isFirstTime") ?: "true"
                if (isFirstTime == "false") {
                    settingsManager.put("isFirstTime", "false")
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.btnRegisterAcc.setOnClickListener {
            var hasError = false
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            // clear error messages
            binding.usernameLayout.error = null
            binding.passwordLayout.error = null
            binding.confirmPasswordLayout.error = null

            if (username.isEmpty()) {
                binding.usernameLayout.error = "Username cannot be empty"
                hasError = true
            }
            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password cannot be empty"
                hasError = true
            }

            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordLayout.error = "Confirm Password cannot be empty"
                hasError = true
            }

            if (password != confirmPassword) {
                binding.confirmPasswordLayout.error = "Passwords do not match"
                hasError = true
            }

            if (!hasError) {
                settingsManager.put("username", username)
                settingsManager.putHashed("password", password)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}