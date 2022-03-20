package com.example.projectplateformesmobiles


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat


class NewRecipe : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.activity_new_recipe, null)

        val cancelButton: Button = findViewById(R.id.CancelNewRecipe)
        cancelButton.setOnClickListener { cancelButtonOnClick(inflater, view) }

    }

    private fun cancelButtonOnClick(inflater: LayoutInflater, view: View){

        val popupView: View = inflater.inflate(R.layout.cancel_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener(){
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener{closePopupListener()}
        val continuePopupButton = popupView.findViewById<Button>(R.id.ContinueButtonPopup)
        continuePopupButton.setOnClickListener{closePopupListener()}

        val confirmCancelButton = popupView.findViewById<Button>(R.id.ConfirmButtonPopup)
        confirmCancelButton.setOnClickListener{
            this.finish()
        }


    }
}