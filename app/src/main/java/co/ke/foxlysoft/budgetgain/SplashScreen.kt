package co.ke.foxlysoft.budgetgain

import SettingsManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.ke.foxlysoft.budgetgain.databinding.ActivitySplashScreenBinding
import co.ke.foxlysoft.budgetgain.intro.IntroActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    lateinit var binding: ActivitySplashScreenBinding
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        settingsManager = SettingsManager(this)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale)

        binding.logoFg.startAnimation(scale)
        binding.textAppName.startAnimation(fadeIn)

        // Delay the transition to the main activity
        Handler(Looper.getMainLooper()).postDelayed({
            val isFirstTime = settingsManager.get("isFirstTime") ?: "true"
            if (isFirstTime == "true") {
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
                finish()
                return@postDelayed
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}