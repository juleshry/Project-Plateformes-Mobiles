package com.example.projectplateformesmobiles.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import com.example.projectplateformesmobiles.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Recipe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        val m_intent = intent

        val title = m_intent.extras?.get("Title")
        val m_title: TextView = findViewById(R.id.RecipeToolBarTitle)
        m_title.text = title.toString()

        val description = m_intent.extras?.get("Description")
        val m_description: TextView = findViewById(R.id.RecipeDescription)
        m_description.text = description.toString()


        val m_ingredients: GridLayout = findViewById(R.id.RecipeIngredients)
        val db = Firebase.firestore
        val recipeRef = db.collection("recipes").document(m_intent.extras?.get("ID").toString())
        recipeRef.get().addOnSuccessListener {  recipeDocument ->
            val recipeIngredients: HashMap<String, String> = recipeDocument.get("ingredients") as HashMap<String, String>

            for((k, v) in recipeIngredients){
                val CardViewLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                CardViewLayoutParams.setMargins(resources.getDimension(R.dimen.RecipeMargin).toInt())

                val ingredientsCardview = CardView(this)
                ingredientsCardview.layoutParams = CardViewLayoutParams
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
                textViewLayoutParams.setMargins(resources.getDimension(R.dimen.RecipeMargin).toInt())

                val newTextView = TextView(this)
                newTextView.text = k
                newTextView.setTextColor(Color.BLACK)
                newTextView.textSize = resources.getDimension(R.dimen.RecipeActivityIngredients)
                newTextView.layoutParams = textViewLayoutParams

                newLinearLayout.addView(newTextView)
                ingredientsCardview.addView(newLinearLayout)
                m_ingredients.addView(ingredientsCardview)

                if(v != "") {
                    val quantityLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    quantityLayoutParams.setMargins(
                        0,
                        resources.getDimension(R.dimen.RecipeMargin).toInt(),
                        resources.getDimension(R.dimen.RecipeMargin).toInt(),
                        resources.getDimension(R.dimen.RecipeMargin).toInt())

                    val newTextViewQuantity = TextView(this)
                    newTextViewQuantity.text = "x" + v
                    newTextViewQuantity.textSize =
                        resources.getDimension(R.dimen.RecipeActivityIngredients)
                    newTextViewQuantity.layoutParams = quantityLayoutParams
                    newLinearLayout.addView(newTextViewQuantity)
                }

            }

        }

    }
}