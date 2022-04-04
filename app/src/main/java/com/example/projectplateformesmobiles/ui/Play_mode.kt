package com.example.projectplateformesmobiles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.projectplateformesmobiles.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Play_mode : AppCompatActivity() {

    private var step = 0;
    private lateinit var ingredients: HashMap<String, String>
    private lateinit var steps: HashMap<String, HashMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_mode)

        val db = Firebase.firestore
        val recipeRef = db.collection("recipes").document(intent.extras?.get("ID").toString())
        recipeRef.get().addOnSuccessListener { recipeDocument ->
            ingredients =
                recipeDocument.get("ingredients") as HashMap<String, String>

            steps =
                recipeDocument.get("steps") as HashMap<String, HashMap<String, String>>

            showFragment()
        }

    }

    fun showFragment() {
        var stepTitle = ""
        var stepIngredients = hashMapOf<String,String>()
        var stepDescription = ""

        if (step == 0) {
            stepTitle = "Préparation des ingrédients"
            stepIngredients = ingredients
        }

        val playModeFragment = PlayFragment()
        val bundle = Bundle()
        bundle.putSerializable("title", stepTitle)
        bundle.putSerializable("description", stepDescription)
        bundle.putSerializable("ingredients", stepIngredients)
        bundle.putSerializable("step", step)
        playModeFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .add(R.id.play_fragment_container, playModeFragment).commit()

    }

    fun test() {
        Log.d("debug", "test")
    }
}