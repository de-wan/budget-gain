package co.ke.foxlysoft.budgetgain.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.ke.foxlysoft.budgetgain.LoginActivity
import co.ke.foxlysoft.budgetgain.R
import co.ke.foxlysoft.budgetgain.databinding.ActivityIntroBinding
import com.google.android.material.tabs.TabLayoutMediator

class IntroActivity : AppCompatActivity() {
    lateinit var binding: ActivityIntroBinding
    lateinit var introSliderAdapter: IntroSliderAdapter

    fun updatePrevOrSkipBtnText() {
        // set prev_or_skip_btn text to "Skip" if on first slide
        if (binding.introSlide.currentItem == 0) {
            binding.prevOrSkipBtn.text = getString(R.string.skip_text)
        } else {
            binding.prevOrSkipBtn.text = getString(R.string.prev_text)
        }
    }

    fun updateNextOrFinishBtnText() {
        // set next_or_finish_btn text to "Finish" if on last slide
        if (binding.introSlide.currentItem == introSliderAdapter.itemCount - 1) {
            binding.nextOrFinishBtn.text = getString(R.string.finish_text)
        } else {
            binding.nextOrFinishBtn.text = getString(R.string.next_text)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        introSliderAdapter = IntroSliderAdapter(this)
        binding.introSlide.adapter = introSliderAdapter

        // set prev_or_skip_btn text to "Skip" if on first slide
        updatePrevOrSkipBtnText()
        // set next_or_finish_btn text to "Finish" if on last slide
        updateNextOrFinishBtnText()

        binding.prevOrSkipBtn.setOnClickListener {
            if (binding.introSlide.currentItem != 0) {
                binding.introSlide.currentItem--
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                finish()
            }

            updatePrevOrSkipBtnText()
            updateNextOrFinishBtnText()
        }

        binding.nextOrFinishBtn.setOnClickListener {
            if (binding.introSlide.currentItem != introSliderAdapter.itemCount - 1) {
                binding.introSlide.currentItem++
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                finish()
            }

            updatePrevOrSkipBtnText()
            updateNextOrFinishBtnText()
        }



    }
}