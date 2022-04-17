package com.example.projectplateformesmobiles.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.projectplateformesmobiles.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class EditRecipe : AppCompatActivity() {

    private val ONE_MEGABYTE: Long = 1024 * 1024

    private lateinit var ingredientsLayout: GridLayout
    val recipeIngredients = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        val inflater: LayoutInflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.activity_new_recipe, null)

        val ID: String = intent.extras?.get("ID") as String

        val photoButton: Button = findViewById(R.id.editRecipeaddPhotoButton)
        val imageRef =
            FirebaseStorage.getInstance().reference.child(ID!!)
        imageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { image ->
                photoButton.background = BitmapDrawable(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeByteArray(
                        image,
                        0,
                        image.size
                    ),
                    1000,
                    1000,
                    true
                ))
            }

        ingredientsLayout = findViewById(R.id.EditRecipeIngredients)

        val db = Firebase.firestore
        val recipeRef = db.collection("recipes").document(ID!!)
        recipeRef.get().addOnSuccessListener { recipeDocument ->
            val titleEditText: EditText = findViewById(R.id.EditRecipeAddTitle)
            val title = recipeDocument.get("title") as String
            titleEditText.setText(title)

            val descriptionEditText: EditText = findViewById(R.id.EditReicpeAddDescription)
            val description = recipeDocument.get("description") as String
            descriptionEditText.setText(description)

            val addIngredientButton: Button = findViewById(R.id.addIngredientButton)

            addIngredientButton.setOnClickListener {
                addIngredient(inflater, view)
            }

            val ingredients = recipeDocument.get("ingredients") as  HashMap<String, String>
            for ((k, v) in ingredients) {
                showIngredient(k, v, inflater, view)

                recipeIngredients[k] = v
            }

            val stepsLayout: LinearLayout = findViewById(R.id.EditRecipeSteps)
            val steps = recipeDocument.get("steps") as HashMap<String, HashMap<String, String>>
            for((k, v) in steps){
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val newTextView = TextView(this.applicationContext)

                newTextView.text = k
                newTextView.setTextColor(Color.BLACK)
                newTextView.textSize = 17f
                newTextView.paintFlags = 0

                params.setMargins(
                    resources.getDimension(R.dimen.activity_horizontal_margin).toInt() + 50,
                    resources.getDimension(R.dimen.activity_vertical_margin).toInt(),
                    0,
                    resources.getDimension(R.dimen.activity_vertical_margin).toInt())

                newTextView.layoutParams = params
                stepsLayout.addView(newTextView)
            }
        }

        val cancelButton: Button = findViewById(R.id.CancelEditRecipe)
        cancelButton.setOnClickListener { this.finish() }
    }

    private fun showIngredient(ingredient: String, value: String, inflater: LayoutInflater, view: View){
        val newButton = Button(this)
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.setMargins(resources.getDimension(R.dimen.activity_vertical_margin).toInt())

        if(value == ""){
            newButton.text = ingredient
        } else newButton.text = ingredient + " x" + value
        newButton.background = resources.getDrawable(R.drawable.light_button)
        newButton.setTextColor(resources.getColor(R.color.dark_green))
        newButton.setPadding(20)
        newButton.isAllCaps = false
        newButton.layoutParams = buttonParams
        newButton.setOnClickListener {
            val textSplit = newButton.text.toString().split(" ")
            var ingName = ""
            var ingInfos = ""
            var i = 1
            for (text in textSplit){
                if(i < textSplit.size){
                    ingName += text + " "
                } else ingInfos += text
                i++
            }
            EditIngredient(inflater, view, ingredientsLayout, newButton, ingName, ingInfos)
        }
        ingredientsLayout.addView(newButton)
    }

    private fun EditIngredient(inflater: LayoutInflater, view: View, container: GridLayout, parent: Button, ingredient: String, value: String) {
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

        var quantity = ""
        var unity = ""
        var multiply = true
        for (c in value){
            if(c.toString().matches("\\d+(\\.\\d+)?".toRegex()) || c.toString() == "."){
                quantity += c
            } else if(c.toString() == "x" && multiply){
                multiply = false
            }
            else {
                unity += c
            }
        }

        val addIngredientEditText: EditText = popupView.findViewById(R.id.addEditText)
        addIngredientEditText.setText(ingredient)
        val addQuantityEditText: EditText = popupView.findViewById(R.id.addQuantityEditText)
        addQuantityEditText.setText(quantity)
        val addUnityEditText: EditText = popupView.findViewById(R.id.addUnityEditText)
        addUnityEditText.setText(unity)

        val confirmNewIngredientButton: Button = popupView.findViewById(R.id.confirmNewRecipePopup)
        confirmNewIngredientButton.setOnClickListener {

            if (addQuantityEditText.text.toString() != "")
                parent.text =
                    addIngredientEditText.text.toString() +
                            " x" + addQuantityEditText.text.toString() +
                            addUnityEditText.text.toString()
            else parent.text = addIngredientEditText.text

            closePopupListener()
        }

        val deleteIngredientButton: Button = popupView.findViewById(R.id.deleteNewIngredientPopup)
        deleteIngredientButton.setOnClickListener{
            recipeIngredients.remove(ingredient)
            container.removeView(parent)
            closePopupListener()
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
                showIngredient(addIngredientEditText.text.toString(), addQuantityEditText.text.toString() + addUnityEditText.text.toString(), inflater, view)

                recipeIngredients[addIngredientEditText.text.toString()] = addQuantityEditText.text.toString() + addUnityEditText.text.toString()
            }
            closePopupListener()
        }

        val deleteIngredientButton: Button = popupView.findViewById(R.id.deleteNewIngredientPopup)
        deleteIngredientButton.background = resources.getDrawable(R.drawable.disabled_button)
        deleteIngredientButton.setTextColor(Color.WHITE)
        deleteIngredientButton.isEnabled = false

    }
}