package com.example.projectplateformesmobiles


import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_recipe.*


class NewRecipe : AppCompatActivity() {

    private companion object {
        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_CODE = 100
    }

    private lateinit var addPhotoButton: Button
    private lateinit var addTitle: EditText
    private lateinit var addDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.activity_new_recipe, null)

        addPhotoButton = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener { photoButtonOnClick(inflater, view) }
        addTitle = findViewById(R.id.addTitle)
        addDescription = findViewById(R.id.addDescription)

        val cancelButton: Button = findViewById(R.id.CancelNewRecipe)
        cancelButton.setOnClickListener { cancelButtonOnClick(inflater, view) }

        val confirmNEwRecipeButton: Button = findViewById(R.id.ConfirmNewRecipe)
        val db = Firebase.firestore

        confirmNEwRecipeButton.setOnClickListener {
            val recipe = hashMapOf(
                "title" to addTitle.text.toString(),
                "description" to addDescription.text.toString()
            )
            db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }




    }




    private fun photoButtonOnClick(inflater: LayoutInflater, view: View){
        val popupView: View = inflater.inflate(R.layout.photo_popup, null)

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

        val takePictureButton: Button = popupView.findViewById(R.id.takePictureButton)
        takePictureButton.setOnClickListener{
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.w("error", e)
            }
            popupWindow.dismiss()
        }

        val choosePictureButton: Button = popupView.findViewById(R.id.choosePictureButton)
        choosePictureButton.setOnClickListener{
            val openGalleryIntent = Intent(Intent.ACTION_PICK)
            openGalleryIntent.type = "image/*"
            startActivityForResult(openGalleryIntent,REQUEST_CODE)
            popupWindow.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            addPhotoButton.background = BitmapDrawable(resources, imageBitmap)
        }
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            addPhotoButton.background = Drawable.createFromPath(data?.dataString)
        }
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
            closePopupListener()
            this.finish()
        }

    }
}