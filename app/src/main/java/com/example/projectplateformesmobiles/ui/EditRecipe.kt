package com.example.projectplateformesmobiles.ui

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import com.example.projectplateformesmobiles.NewRecipe
import com.example.projectplateformesmobiles.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class EditRecipe : AppCompatActivity() {

    private companion object {
        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_CODE = 100
    }

    private val ONE_MEGABYTE: Long = 1024 * 1024

    private lateinit var ingredientsLayout: GridLayout
    private lateinit var stepsLayout: GridLayout
    private lateinit var addPhotoButton: Button
    private lateinit var recipeImage: Drawable
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText

    val recipeIngredients = mutableMapOf<String, String>()
    var recipeSteps = mutableMapOf<String, HashMap<String, String>>()

    var storageReference: StorageReference? = null

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
                photoButton.background = BitmapDrawable(
                    Bitmap.createScaledBitmap(
                        BitmapFactory.decodeByteArray(
                            image,
                            0,
                            image.size
                        ),
                        1000,
                        1000,
                        true
                    )
                )
            }

        addPhotoButton = findViewById(R.id.editRecipeaddPhotoButton)
        addPhotoButton.setOnClickListener { photoButtonOnClick(inflater, view) }

        ingredientsLayout = findViewById(R.id.EditRecipeIngredients)
        stepsLayout = findViewById(R.id.EditRecipeSteps)

        val db = Firebase.firestore
        storageReference = FirebaseStorage.getInstance().reference
        val recipeRef = db.collection("recipes").document(ID!!)
        recipeRef.get().addOnSuccessListener { recipeDocument ->
            titleEditText = findViewById(R.id.EditRecipeAddTitle)
            val title = recipeDocument.get("title") as String
            titleEditText.setText(title)

            descriptionEditText = findViewById(R.id.EditReicpeAddDescription)
            val description = recipeDocument.get("description") as String
            descriptionEditText.setText(description)

            val addIngredientButton: Button = findViewById(R.id.addIngredientButton)

            addIngredientButton.setOnClickListener {
                addIngredient(inflater, view)
            }

            val ingredients = recipeDocument.get("ingredients") as HashMap<String, String>
            for ((k, v) in ingredients) {
                showIngredient(k, v, inflater, view)

                recipeIngredients[k] = v
            }

            val addStepButton: Button = findViewById(R.id.addStepButton)
            addStepButton.setOnClickListener {
                addStep(inflater, view)
            }

            val steps = recipeDocument.get("steps") as HashMap<String, HashMap<String, String>>
            for ((k, v) in steps) {
                showStep(k, v, inflater, view)

                recipeSteps[k] = v
            }
        }

        val confirmButton: Button = findViewById(R.id.ConfirmEditRecipe)
        confirmButton.setOnClickListener {
            if (titleEditText.text.toString() != "" && descriptionEditText.text.toString() != ""
                && recipeIngredients.isNotEmpty() && recipeSteps.isNotEmpty()) {
                val recipe = hashMapOf(
                    "title" to titleEditText.text.toString(),
                    "description" to descriptionEditText.text.toString(),
                    "ingredients" to recipeIngredients,
                    "steps" to recipeSteps
                )

                recipeRef.set(recipe).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "DocumentSnapshot updated with ID: ${ID}")

                    if (recipeImage != null) {
                        val bitmap = recipeImage.toBitmap()
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        val imageRef = storageReference?.child(ID)
                        val uploadTask = imageRef?.putBytes(data)
                        if (uploadTask != null) {
                            uploadTask.addOnFailureListener {
                                // Handle unsuccessful uploads
                                Log.w(ContentValues.TAG, "image non enregistrée")

                            }.addOnSuccessListener { taskSnapshot ->
                                Log.w(ContentValues.TAG, "image enregistrée")
                                this.finish()
                            }
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error updating document", e)
                }
            } else Toast
                .makeText(
                    this.applicationContext,
                    "Il manque des informations",
                    Toast.LENGTH_SHORT
                )
                .show()
        }

        val cancelButton: Button = findViewById(R.id.CancelEditRecipe)
        cancelButton.setOnClickListener { this.finish() }
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
                startActivityForResult(takePictureIntent, EditRecipe.REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.w("error", e)
            }
            closePopupListener()
        }

        val choosePictureButton: Button = popupView.findViewById(R.id.choosePictureButton)
        choosePictureButton.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK)
            openGalleryIntent.type = "image/*"
            startActivityForResult(openGalleryIntent, EditRecipe.REQUEST_CODE)
            closePopupListener()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EditRecipe.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            addPhotoButton.background = BitmapDrawable(resources, imageBitmap)
        }
        if (requestCode == EditRecipe.REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data!!))
            addPhotoButton.background = BitmapDrawable(resources, bitmap)
        }
        recipeImage = addPhotoButton.background
    }

    private fun showIngredient(
        ingredient: String,
        value: String,
        inflater: LayoutInflater,
        view: View
    ) {
        val newButton = Button(this)
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.setMargins(resources.getDimension(R.dimen.activity_vertical_margin).toInt())

        if (value == "") {
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
            for (text in textSplit) {
                if (i < textSplit.size) {
                    ingName += text + " "
                } else ingInfos += text
                i++
            }
            EditIngredient(inflater, view, ingredientsLayout, newButton, ingName, ingInfos)
        }
        ingredientsLayout.addView(newButton)
    }

    private fun showStep(
        step: String,
        value: HashMap<String, String>,
        inflater: LayoutInflater,
        view: View
    ) {
        val newButton = Button(this)
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.setMargins(resources.getDimension(R.dimen.activity_vertical_margin).toInt())

        newButton.text = step
        newButton.background = resources.getDrawable(R.drawable.light_button)
        newButton.setTextColor(resources.getColor(R.color.dark_green))
        newButton.setPadding(20)
        newButton.isAllCaps = false
        newButton.layoutParams = buttonParams
        newButton.setOnClickListener {
            EditStep(
                inflater,
                view,
                stepsLayout,
                newButton,
                newButton.text.toString(),
                recipeSteps[step]!!
            )
        }
        stepsLayout.addView(newButton)
    }

    private fun EditIngredient(
        inflater: LayoutInflater,
        view: View,
        container: GridLayout,
        parent: Button,
        ingredient: String,
        value: String
    ) {
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
        for (c in value) {
            if (c.toString().matches("\\d+(\\.\\d+)?".toRegex()) || c.toString() == ".") {
                quantity += c
            } else if (c.toString() == "x" && multiply) {
                multiply = false
            } else {
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
        deleteIngredientButton.setOnClickListener {
            recipeIngredients.remove(ingredient)
            container.removeView(parent)
            closePopupListener()
        }

    }

    private fun EditStep(
        inflater: LayoutInflater,
        view: View,
        container: GridLayout,
        parent: Button,
        step: String,
        value: HashMap<String, String>
    ) {
        var tmpStep = recipeSteps.toMap() as HashMap<String, HashMap<String, String>>

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

        val stepTitle: EditText = popupView.findViewById(R.id.addStepTitleEditText)
        stepTitle.setText(step)
        var oldTitle: String = step
        stepTitle.doBeforeTextChanged { text, _, _, _ ->
            oldTitle = text.toString()
        }
        stepTitle.doAfterTextChanged { text ->
            val tmp: HashMap<String, String> = tmpStep[oldTitle]!!
            tmpStep.remove(oldTitle)
            tmpStep[text.toString()] = tmp
        }

        val stepGrid: GridLayout = popupView.findViewById(R.id.addStepGrid)
        val buttons: MutableSet<String> =
            tmpStep[step]?.get("ingredients")?.split(", ")!!.toMutableSet()
        for ((k, v) in recipeIngredients) {
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

            if (buttons.contains(k)) {
                newButton.isChecked = true
                newButton.background = resources.getDrawable(R.drawable.plain_button)
                newButton.setTextColor(resources.getColor(R.color.white))
            }

            newButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    newButton.background = resources.getDrawable(R.drawable.plain_button)
                    newButton.setTextColor(resources.getColor(R.color.white))
                    buttons.add(newButton.text.toString())
                } else {
                    newButton.background = resources.getDrawable(R.drawable.light_button)
                    newButton.setTextColor(resources.getColor(R.color.dark_green))
                    buttons.remove(newButton.text.toString())
                }
            }
            stepGrid.addView(newButton)
        }

        val stepDescription: EditText = popupView.findViewById(R.id.addStepDescriptionEditText)
        if (value["description"] != "") {
            stepDescription.setText(value["description"])
        }

        val deleteIngredientButton: Button = popupView.findViewById(R.id.deleteNewStepPopup)
        deleteIngredientButton.setOnClickListener {
            recipeSteps.remove(step)
            container.removeView(parent)
            closePopupListener()
        }

        val confirmNewStepButton: Button = popupView.findViewById(R.id.confirmNewStepPopup)
        confirmNewStepButton.setOnClickListener {

            if (stepTitle.text.toString() != ""
                && stepDescription.text.toString() != ""
            ) {


                val stepInfos = mutableMapOf<String, String>() as HashMap<String, String>
                stepInfos["description"] = stepDescription.text.toString()
                var ing = ""
                if (buttons.size > 0) {
                    buttons.forEach { button ->
                        ing += button + ", "
                    }
                    stepInfos["ingredients"] = ing
                }

                tmpStep[stepTitle.text.toString()] = stepInfos

                recipeSteps = tmpStep

                stepsLayout.removeAllViews()
                for ((k, v) in recipeSteps) {
                    showStep(
                        k,
                        v,
                        inflater,
                        view
                    )
                }

                closePopupListener()
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Il manque des informations",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
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
                showIngredient(
                    addIngredientEditText.text.toString(),
                    addQuantityEditText.text.toString() + addUnityEditText.text.toString(),
                    inflater,
                    view
                )

                recipeIngredients[addIngredientEditText.text.toString()] =
                    addQuantityEditText.text.toString() + addUnityEditText.text.toString()
            }
            closePopupListener()
        }

        val deleteIngredientButton: Button = popupView.findViewById(R.id.deleteNewIngredientPopup)
        deleteIngredientButton.background = resources.getDrawable(R.drawable.disabled_button)
        deleteIngredientButton.setTextColor(Color.WHITE)
        deleteIngredientButton.isEnabled = false

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
        val addStepDescriptionEditText: EditText =
            popupView.findViewById(R.id.addStepDescriptionEditText)

        val stepGrid: GridLayout = popupView.findViewById(R.id.addStepGrid)
        val buttons = mutableSetOf<String>()
        for ((k, v) in recipeIngredients) {
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
                if (isChecked) {
                    newButton.background = resources.getDrawable(R.drawable.plain_button)
                    newButton.setTextColor(resources.getColor(R.color.white))
                    buttons.add(newButton.text.toString())
                } else {
                    newButton.background = resources.getDrawable(R.drawable.light_button)
                    newButton.setTextColor(resources.getColor(R.color.dark_green))
                    buttons.remove(newButton.text.toString())
                }
            }
            stepGrid.addView(newButton)
        }

        val confirmNewStepButton: Button = popupView.findViewById(R.id.confirmNewStepPopup)
        confirmNewStepButton.setOnClickListener {

            if (addStepTitleEditText.text.toString() != ""
                && addStepDescriptionEditText.text.toString() != ""
            ) {


                val stepInfos = mutableMapOf<String, String>() as HashMap<String, String>
                stepInfos["description"] = addStepDescriptionEditText.text.toString()
                var ing = ""
                if (buttons.size > 0) {
                    buttons.forEach { button ->
                        ing += button + ", "
                    }
                    stepInfos["ingredients"] = ing
                }

                recipeSteps.put(addStepTitleEditText.text.toString(), stepInfos)

                stepsLayout.removeAllViews()
                for ((k, v) in recipeSteps) {
                    showStep(
                        k,
                        v,
                        inflater,
                        view
                    )
                }

                closePopupListener()
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Il manque des informations",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }
}