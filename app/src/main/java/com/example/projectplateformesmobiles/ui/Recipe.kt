package com.example.projectplateformesmobiles.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.projectplateformesmobiles.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class Recipe : AppCompatActivity() {

    private val ONE_MEGABYTE: Long = 1024 * 1024

    private var ID: String? = null

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_recipe)

        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data

        ID = appLinkData?.getQueryParameter("ID")

        if(intent.extras?.get("ID") != null){
            ID = intent.extras?.get("ID").toString()
        }

        val backButton: Button = findViewById(R.id.recipe_back_button)
        backButton.setOnClickListener {
            this.finish()
        }

        val playButton: Button = findViewById(R.id.play_button)
        playButton.setOnClickListener {
            val playIntent = Intent(this, Play_mode::class.java)
            playIntent.putExtra("ID", ID)
            startActivity(playIntent)
        }


        val imageView: ImageView = findViewById(R.id.RecipeImage)
        val imageRef =
            FirebaseStorage.getInstance().reference.child(ID!!)
        imageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { image ->
                imageView.setImageBitmap(
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

        val m_title: TextView = findViewById(R.id.RecipeToolBarTitle)

        val m_description: TextView = findViewById(R.id.RecipeDescription)

        val m_ingredients: GridLayout = findViewById(R.id.RecipeIngredients)
        val db = Firebase.firestore
        val recipeRef = db.collection("recipes").document(ID!!)
        recipeRef.get().addOnSuccessListener { recipeDocument ->
            val title: String = recipeDocument.get("title") as String
            m_title.text = title

            val description = recipeDocument.get("description") as String
            m_description.text = description

            val recipeIngredients: HashMap<String, String> =
                recipeDocument.get("ingredients") as HashMap<String, String>

            for ((k, v) in recipeIngredients) {
                val cardViewLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                cardViewLayoutParams.setMargins(
                    resources.getDimension(R.dimen.RecipeMargin).toInt()
                )

                val ingredientsCardview = CardView(this)
                ingredientsCardview.layoutParams = cardViewLayoutParams
                ingredientsCardview.radius = 30f


                val linearLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                val newLinearLayout = LinearLayout(this)
                newLinearLayout.layoutParams = linearLayoutParams
                newLinearLayout.orientation = LinearLayout.HORIZONTAL


                val textViewLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textViewLayoutParams.setMargins(
                    resources.getDimension(R.dimen.RecipeMargin).toInt()
                )

                val newTextView = TextView(this)
                newTextView.text = k
                newTextView.setTextColor(Color.BLACK)
                newTextView.textSize = resources.getDimension(R.dimen.RecipeActivityIngredients)
                newTextView.layoutParams = textViewLayoutParams

                newLinearLayout.addView(newTextView)
                ingredientsCardview.addView(newLinearLayout)
                m_ingredients.addView(ingredientsCardview)

                if (v != "") {
                    val quantityLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    quantityLayoutParams.setMargins(
                        0,
                        resources.getDimension(R.dimen.RecipeMargin).toInt(),
                        resources.getDimension(R.dimen.RecipeMargin).toInt(),
                        resources.getDimension(R.dimen.RecipeMargin).toInt()
                    )

                    val newTextViewQuantity = TextView(this)
                    newTextViewQuantity.text = "x" + v
                    newTextViewQuantity.textSize =
                        resources.getDimension(R.dimen.RecipeActivityIngredients)
                    newTextViewQuantity.layoutParams = quantityLayoutParams
                    newLinearLayout.addView(newTextViewQuantity)
                }

            }

            val recipeSteps: HashMap<String, HashMap<String, String>> =
                recipeDocument.get("steps") as HashMap<String, HashMap<String, String>>

            val m_recipeSteps: LinearLayout = findViewById(R.id.RecipesSteps)

            val stepParam = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            stepParam.setMargins(
                resources.getDimension(R.dimen.RecipeActivityStepSubTitle).toInt(), 0, 0, 0
            )

            var i = 1
            for ((step, infos) in recipeSteps) {
                val cardViewLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                cardViewLayoutParams.setMargins(
                    resources.getDimension(R.dimen.RecipeMargin).toInt()
                )

                val stepCardView = CardView(this)
                stepCardView.radius = 30f
                stepCardView.layoutParams = cardViewLayoutParams
                stepCardView.setPadding(20)

                val entireStep = LinearLayout(this)
                stepCardView.addView(entireStep)
                entireStep.orientation = LinearLayout.VERTICAL
                entireStep.layoutParams = stepParam
                m_recipeSteps.addView(stepCardView)

                val stepTitle = TextView(this)
                stepTitle.text = i.toString() + ". " + step
                stepTitle.textSize = resources.getDimension(R.dimen.RecipeActivityStepSubTitle)
                stepTitle.setTextColor(resources.getColor(R.color.dark_green))
                stepTitle.layoutParams = stepParam

                entireStep.addView(stepTitle)


                val stepInfosLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                stepInfosLayoutParams.setMargins(
                    resources.getDimension(R.dimen.RecipeActivityStepSubTitle).toInt(),
                    resources.getDimension(R.dimen.RecipeSmallMargin).toInt(),
                    0,
                    resources.getDimension(R.dimen.RecipeSmallMargin).toInt()
                )
                val stepinfos = LinearLayout(this)
                stepinfos.layoutParams = stepInfosLayoutParams
                stepinfos.orientation = LinearLayout.VERTICAL
                entireStep.addView(stepinfos)


                for ((k, v) in infos) {
                    if (k == "ingredients") {

                        val gridLayourParam = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        gridLayourParam.gravity = Gravity.FILL

                        val stepIngredientGridLayout = GridLayout(this)
                        stepIngredientGridLayout.columnCount = 2
                        stepIngredientGridLayout.layoutParams = gridLayourParam
                        stepinfos.addView(stepIngredientGridLayout)

                        val ings = v.split(",")
                        for (i in ings) {
                            if (i != "" && i != " ") {

                                val ingredientsCardview = CardView(this)
                                ingredientsCardview.layoutParams = stepInfosLayoutParams
                                ingredientsCardview.radius = 30f

                                val ingLayoutParam = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                ingLayoutParam.setMargins(
                                    resources.getDimension(R.dimen.RecipeMargin).toInt()
                                )

                                val newTextView = TextView(this)
                                newTextView.text = i
                                newTextView.setTextColor(Color.BLACK)
                                newTextView.textSize =
                                    resources.getDimension(R.dimen.RecipeActivityIngredients)
                                newTextView.layoutParams = ingLayoutParam

                                ingredientsCardview.addView(newTextView)
                                stepIngredientGridLayout.addView(ingredientsCardview)
                            }
                        }
                    } else if (k == "description") {
                        val descTextView = TextView(this)
                        descTextView.text = "Description:"
                        descTextView.textSize =
                            resources.getDimension(R.dimen.RecipeActivityIngredients)
                        descTextView.layoutParams = stepInfosLayoutParams

                        val stepDescription = TextView(this)
                        stepDescription.text = v
                        stepDescription.setTextColor(Color.BLACK)
                        stepDescription.textSize =
                            resources.getDimension(R.dimen.RecipeActivityIngredients)
                        stepDescription.layoutParams = stepInfosLayoutParams

                        stepinfos.addView(descTextView)
                        stepinfos.addView(stepDescription)
                    }
                }
                i++
            }
            val shareButton: Button = findViewById(R.id.shareButton)
            val shareUrl = "http://myRecipe.com/recipe?ID=$ID"
            val shareText = "$title sur l'application myRecipe: \n$shareUrl"
            shareButton.setOnClickListener{
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            val editButton: Button = findViewById(R.id.editButton)
            editButton.setOnClickListener{
                val editIntent = Intent(this, EditRecipe::class.java)
                editIntent.putExtra("ID", ID)
                startActivity(editIntent)
            }
        }
    }
}