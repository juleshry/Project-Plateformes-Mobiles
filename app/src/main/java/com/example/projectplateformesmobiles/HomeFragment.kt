package com.example.projectplateformesmobiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.content.Intent
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)

        val newRecipeIntent = Intent(getActivity(), NewRecipe::class.java)
        val newRecipeButton: Button = view.findViewById<Button>(R.id.newRecipeButton)
        newRecipeButton.setOnClickListener{
            startActivity(newRecipeIntent)
        }

        val userButton: Button = view.findViewById(R.id.userButton)
        userButton.setOnClickListener{
            userButtonOnClickListener(inflater, view)
        }

        return view
    }

    private fun userButtonOnClickListener(inflater: LayoutInflater, view: View){
        val popupView: View = inflater.inflate(R.layout.user_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.requireActivity().window.statusBarColor = ContextCompat.getColor(this.requireActivity(), R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener(){
            this.requireActivity().window.statusBarColor = ContextCompat.getColor(this.requireActivity(), R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener{closePopupListener()}
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}