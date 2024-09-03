package co.ke.foxlysoft.budgetgain.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ke.foxlysoft.budgetgain.R

/**
 * A simple [Fragment] subclass.
 * Use the [IntroSlide3Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroSlide3Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro_slide3, container, false)
    }
}