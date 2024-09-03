package co.ke.foxlysoft.budgetgain.intro

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.ke.foxlysoft.budgetgain.intro.IntroSlide1Fragment

class IntroSliderAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> IntroSlide1Fragment()
            1 -> IntroSlide2Fragment()
            2 -> IntroSlide3Fragment()
            else -> IntroSlide1Fragment()
        }
    }
}