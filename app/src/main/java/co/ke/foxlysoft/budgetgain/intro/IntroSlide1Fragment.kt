package co.ke.foxlysoft.budgetgain.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.ke.foxlysoft.budgetgain.R


/**
 * A simple [Fragment] subclass.
 * Use the [IntroSlide1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroSlide1Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro_slide1, container, false)
    }
}