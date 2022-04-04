package com.example.projectplateformesmobiles


import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.setMargins
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class NewRecipe : AppCompatActivity() {

    private companion object {
        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_CODE = 100
    }

    private lateinit var addPhotoButton: Button
    private lateinit var addIngredientButton: Button
    private lateinit var addIngredientLinearLayout: LinearLayout
    private lateinit var addStepLinearLayout: LinearLayout
    private lateinit var addStepButton: Button
    private lateinit var addTitle: EditText
    private lateinit var addDescription: EditText
    private lateinit var recipeImage: Drawable

    private var ingredients = mutableMapOf<String, String>()
    private var steps = mutableMapOf<String, MutableMap<String, String>>()
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)
        storageReference = FirebaseStorage.getInstance().reference


        val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.activity_new_recipe, null)

        addPhotoButton = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener { photoButtonOnClick(inflater, view) }
        addTitle = findViewById(R.id.addTitle)
        addDescription = findViewById(R.id.addDescription)
        addIngredientLinearLayout = findViewById(R.id.addIngredient)
        addStepLinearLayout = findViewById(R.id.addStep)
        addIngredientButton = findViewById(R.id.addIngredientButton)
        addStepButton = findViewById(R.id.addStepButton)

        addIngredientButton.setOnClickListener {
            addIngredient(inflater, view)
        }

        addStepButton.setOnClickListener {
            addStep(inflater, view)
        }


        val cancelButton: Button = findViewById(R.id.CancelNewRecipe)
        cancelButton.setOnClickListener { cancelButtonOnClick(inflater, view) }

        val confirmNEwRecipeButton: Button = findViewById(R.id.ConfirmNewRecipe)
        val db = Firebase.firestore

        confirmNEwRecipeButton.setOnClickListener {
            if (addTitle.text.toString() != "" && addDescription.text.toString() != ""
                && ingredients.isNotEmpty() && steps.isNotEmpty() && recipeImage != null
            ) {
                val recipe = hashMapOf(
                    "title" to addTitle.text.toString(),
                    "description" to addDescription.text.toString(),
                    "ingredients" to ingredients,
                    "steps" to steps
                )

                db.collection("recipes")
                    .add(recipe)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        val bitmap = recipeImage.toBitmap()
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val imageRef = storageReference?.child(documentReference.id)
                        val uploadTask = imageRef?.putBytes(data)
                        if (uploadTask != null) {
                            uploadTask.addOnFailureListener {
                                // Handle unsuccessful uploads
                                Log.w(TAG, "image non enregistrée")

                            }.addOnSuccessListener { taskSnapshot ->
                                Log.w(TAG, "image enregistrée")
                                this.finish()
                            }
                        }

                        Firebase.auth.currentUser?.let {
                            var userRef = db.collection("users").document(it.uid)
                            userRef.update("recipes", FieldValue.arrayUnion(documentReference.id))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            } else Toast
                .makeText(
                    this.applicationContext,
                    "Il manque des informations",
                    Toast.LENGTH_SHORT
                )
                .show()
        }

    }



        private fun photoButtonOnClick(inflater: LayoutInflater, view: View) {
        val popupView: View = inflater.inflate(R.layout.photo_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        fun closePopupListener() {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener { closePopupListener() }

        val takePictureButton: Button = popupView.findViewById(R.id.takePictureButton)
        takePictureButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.w("error", e)
            }
            closePopupListener()
        }

        val choosePictureButton: Button = popupView.findViewById(R.id.choosePictureButton)
        choosePictureButton.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK)
            openGalleryIntent.type = "image/*"
            startActivityForResult(openGalleryIntent, REQUEST_CODE)
            closePopupListener()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            addPhotoButton.background = BitmapDrawable(resources, imageBitmap)
        }
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data!!))
            addPhotoButton.background = BitmapDrawable(resources, bitmap)
        }
        recipeImage = addPhotoButton.background
    }


    private fun cancelButtonOnClick(inflater: LayoutInflater, view: View) {

        val popupView: View = inflater.inflate(R.layout.cancel_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener() {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener { closePopupListener() }

        val continuePopupButton = popupView.findViewById<Button>(R.id.ContinueButtonPopup)
        continuePopupButton.setOnClickListener { closePopupListener() }

        val confirmCancelButton = popupView.findViewById<Button>(R.id.ConfirmButtonPopup)
        confirmCancelButton.setOnClickListener {
            closePopupListener()
            this.finish()
        }

    }

    private fun addIngredient(inflater: LayoutInflater, view: View) {
        val popupView: View = inflater.inflate(R.layout.add_ingredient_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener() {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener { closePopupListener() }

        val addIngredientEditText: EditText = popupView.findViewById(R.id.addEditText)
        val addQuantityEditText: EditText = popupView.findViewById(R.id.addQuantityEditText)
        val addUnityEditText: EditText = popupView.findViewById(R.id.addUnityEditText)

        val confirmNewIngredientButton: Button = popupView.findViewById(R.id.confirmNewRecipePopup)
        confirmNewIngredientButton.setOnClickListener {
            if (addIngredientEditText.text.toString() != "") {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val newTextView = TextView(this.applicationContext)
                if (addQuantityEditText.text.toString() != "")
                    newTextView.text =
                        addIngredientEditText.text.toString() + " x" + addQuantityEditText.text.toString() + addUnityEditText.text.toString()
                else
                    newTextView.text = addIngredientEditText.text.toString()
                newTextView.setTextColor(Color.BLACK)
                newTextView.textSize = 17f
                newTextView.paintFlags = 0

                params.marginStart =
                    resources.getDimension(R.dimen.activity_horizontal_margin).toInt() + 50

                newTextView.layoutParams = params
                addIngredientLinearLayout.addView(newTextView)
                ingredients[addIngredientEditText.text.toString()] = addQuantityEditText.text.toString() + addUnityEditText.text.toString()
            }
            closePopupListener()
        }

    }

    private fun addStep(inflater: LayoutInflater, view: View) {
        val popupView: View = inflater.inflate(R.layout.add_step_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener() {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener { closePopupListener() }

        val addStepTitleEditText: EditText = popupView.findViewById(R.id.addStepTitleEditText)
        val addStepDescriptionEditText: EditText = popupView.findViewById(R.id.addStepDescriptionEditText)

        val stepGrid: GridLayout = popupView.findViewById(R.id.addStepGrid)
        val buttons = mutableSetOf<Button>()
        for ((k, v) in ingredients) {
            val newButton = ToggleButton(this.applicationContext)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            newButton.text = k
            newButton.textOff = k
            newButton.textOn = k
            newButton.background = resources.getDrawable(R.drawable.light_button)
            newButton.setTextColor(resources.getColor(R.color.dark_green))

            params.setMargins(10)
            newButton.layoutParams = params

            newButton.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    newButton.background = resources.getDrawable(R.drawable.plain_button)
                    newButton.setTextColor(resources.getColor(R.color.white))
                    buttons.add(newButton)
                } else {
                    newButton.background = resources.getDrawable(R.drawable.light_button)
                    newButton.setTextColor(resources.getColor(R.color.dark_green))
                    buttons.remove(newButton)
                }
            }
            stepGrid.addView(newButton)
        }

        val confirmNewStepButton: Button = popupView.findViewById(R.id.confirmNewStepPopup)
        confirmNewStepButton.setOnClickListener {

                      if(addStepTitleEditText.text.toString() != ""
                          && addStepDescriptionEditText.text.toString() != ""){

                          val params = LinearLayout.LayoutParams(
                              LinearLayout.LayoutParams.MATCH_PARENT,
                              LinearLayout.LayoutParams.WRAP_CONTENT
                          )
                          val newTextView = TextView(this.applicationContext)
                          newTextView.text = addStepTitleEditText.text.toString()
                          newTextView.setTextColor(Color.BLACK)
                          newTextView.textSize = 17f
                          newTextView.paintFlags = 0

                          params.marginStart =
                              resources.getDimension(R.dimen.activity_horizontal_margin).toInt() + 50

                          newTextView.layoutParams = params
                          addStepLinearLayout.addView(newTextView)

                          var saveIngredient = ""
                          for(b in buttons){
                              saveIngredient += b.text.toString() + " "
                          }
                          steps.set(addStepTitleEditText.text.toString(),
                              mutableMapOf("ingredients" to saveIngredient,
                                  "description" to addStepDescriptionEditText.text.toString()))

                          closePopupListener()
                      } else {
                          Toast.makeText(this.applicationContext, "Il manque des informations", Toast.LENGTH_SHORT)
                              .show()
                      }
        }

    }
}