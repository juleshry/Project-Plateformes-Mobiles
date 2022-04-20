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
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
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
    private var recipeImage: Drawable? = null

    private var ingredients = mutableMapOf<String, String>()
    private var steps = HashMap<String, HashMap<String, String>>()
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)
        storageReference = FirebaseStorage.getInstance().reference


        val inflater: LayoutInflater = this.layoutInflater
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
                        val bitmap = recipeImage!!.toBitmap()
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

    private fun showIngredient(
        ingredient: String,
        value: String,
        inflater: LayoutInflater,
        view: View
    ) {
        val newButton = Button(this)
        newButton.isAllCaps = false
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        buttonParams.setMargins(
            resources.getDimension(R.dimen.activity_horizontal_margin).toInt() + 50,
            resources.getDimension(R.dimen.activity_vertical_margin).toInt(),
            0,
            resources.getDimension(R.dimen.activity_vertical_margin).toInt()
        )

        if (value == "") {
            newButton.text = ingredient
        } else newButton.text = ingredient + " x" + value
        newButton.background = resources.getDrawable(R.drawable.light_button)
        newButton.setTextColor(resources.getColor(R.color.dark_green))
        newButton.setPadding(20)
        newButton.layoutParams = buttonParams
        newButton.setOnClickListener {
            EditIngredient(inflater, view, addIngredientLinearLayout, newButton, ingredient, value)
        }
        addIngredientLinearLayout.addView(newButton)
    }

    private fun EditIngredient(
        inflater: LayoutInflater,
        view: View,
        container: LinearLayout,
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
        for (c in value) {
            if (c.toString().matches("\\d+(\\.\\d+)?".toRegex()) || c.toString() == ".") {
                quantity += c
            } else unity += c
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
            ingredients.remove(ingredient)
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
                showIngredient(
                    addIngredientEditText.text.toString(),
                    addQuantityEditText.text.toString() + addUnityEditText.text.toString(),
                    inflater,
                    view
                )

                ingredients[addIngredientEditText.text.toString()] =
                    addQuantityEditText.text.toString() + addUnityEditText.text.toString()
            }
            closePopupListener()
        }

        val deleteIngredientButton: Button = popupView.findViewById(R.id.deleteNewIngredientPopup)
        deleteIngredientButton.background = resources.getDrawable(R.drawable.disabled_button)
        deleteIngredientButton.setTextColor(Color.WHITE)
        deleteIngredientButton.isEnabled = false

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

        buttonParams.setMargins(
            resources.getDimension(R.dimen.activity_horizontal_margin).toInt() + 50,
            resources.getDimension(R.dimen.activity_vertical_margin).toInt(),
            0,
            resources.getDimension(R.dimen.activity_vertical_margin).toInt()
        )

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
                addStepLinearLayout,
                newButton,
                newButton.text.toString(),
                steps[step]!! as HashMap<String, String>
            )
        }
        addStepLinearLayout.addView(newButton)
    }

    private fun EditStep(
        inflater: LayoutInflater,
        view: View,
        container: LinearLayout,
        parent: Button,
        step: String,
        value: HashMap<String, String>
    ) {
        var tmpStep: HashMap<String, HashMap<String, String>> = steps.clone() as HashMap<String, HashMap<String, String>>

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
            steps.remove(step)
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

                steps = tmpStep

                addStepLinearLayout.removeAllViews()
                for ((k, v) in steps) {
                    showStep(
                        k,
                        v as HashMap<String, String>,
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

        val timeSet: EditText = popupView.findViewById(R.id.timeSet)
        val spinner : Spinner = popupView.findViewById(R.id.timer_spinner)
        spinner.setSelection(0)

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
                if (isChecked) {
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

            if (addStepTitleEditText.text.toString() != ""
                && addStepDescriptionEditText.text.toString() != ""
            ) {
                var saveIngredient = ""
                for (b in buttons) {
                    saveIngredient += b.text.toString() + ", "
                }

                if (timeSet.text.toString() != null){
                    steps.set(
                        addStepTitleEditText.text.toString(),
                        mutableMapOf(
                            "ingredients" to saveIngredient,
                            "description" to addStepDescriptionEditText.text.toString(),
                            "timePrecision" to spinner.selectedItem.toString(),
                            "duration" to timeSet.text.toString()
                        ) as HashMap<String, String>
                    )
                val stepInfos = mutableMapOf(
                    "ingredients" to saveIngredient,
                    "description" to addStepDescriptionEditText.text.toString()
                ) as HashMap<String, String>
                steps[addStepTitleEditText.text.toString()] = stepInfos


                showStep(addStepTitleEditText.text.toString(), stepInfos, inflater, view)



                }else{
                    steps.set(
                        addStepTitleEditText.text.toString(),
                        mutableMapOf(
                            "ingredients" to saveIngredient,
                            "description" to addStepDescriptionEditText.text.toString()
                        ) as HashMap<String, String>
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