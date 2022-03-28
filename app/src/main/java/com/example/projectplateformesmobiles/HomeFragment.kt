package com.example.projectplateformesmobiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.example.projectplateformesmobiles.ui.Account
import com.example.projectplateformesmobiles.ui.Recipe
import com.example.projectplateformesmobiles.ui.Settings
import com.example.projectplateformesmobiles.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    val ONE_MEGABYTE: Long = 1024 * 1024

    protected lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false)


        val newRecipeIntent = Intent(activity, NewRecipe::class.java)
        val newRecipeButton: Button = fragmentView.findViewById<Button>(R.id.newRecipeButton)
        newRecipeButton.setOnClickListener {
            startActivity(newRecipeIntent)
            activity?.fragmentManager?.popBackStack()
        }

        val userButton: Button = fragmentView.findViewById(R.id.userButton)
        userButton.setOnClickListener {
            userButtonOnClickListener(inflater, fragmentView)
        }

        return fragmentView
    }

    override fun onResume() {
        super.onResume()

        showRecipes()
    }

    private fun userButtonOnClickListener(inflater: LayoutInflater, view: View) {
        val popupView: View = inflater.inflate(R.layout.user_popup, null)

        val width: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val height: Int = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        this.requireActivity().window.statusBarColor =
            ContextCompat.getColor(this.requireActivity(), R.color.semiTransparent)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)


        fun closePopupListener() {
            this.requireActivity().window.statusBarColor =
                ContextCompat.getColor(this.requireActivity(), R.color.white)
            popupWindow.dismiss()
        }

        val closeButton: Button = popupView.findViewById(R.id.backPopup)
        closeButton.setOnClickListener { closePopupListener() }

        val accountButton: Button = popupView.findViewById(R.id.accountButton)
        accountButton.setOnClickListener {
            closePopupListener()
            val accountIntent = Intent(this.requireActivity(), Account::class.java)
            startActivity(accountIntent)
        }

        val settingsButton: Button = popupView.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            closePopupListener()
            val settingsIntent = Intent(this.requireActivity(), Settings::class.java)
            startActivity(settingsIntent)
        }

        val logoutButton: Button = popupView.findViewById(R.id.logout_button)
        var auth: FirebaseAuth = Firebase.auth
        logoutButton.setOnClickListener {
            Log.i("APP", "Logout")
            auth.signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client: GoogleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)
            client.signOut().addOnCompleteListener(this.requireActivity(), OnCompleteListener { })

            val logoutIntent = Intent(this.requireActivity(), LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            popupWindow.dismiss()
            startActivity(logoutIntent)
        }
    }


    private fun showRecipes() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val db = Firebase.firestore
        var userRecipes: ArrayList<String>? = arrayListOf<String>()
        val homeRecipesGridLayout: GridLayout =
            fragmentView.findViewById(R.id.homeRecipesGridLayout)
        homeRecipesGridLayout.removeAllViews()
        val user = Firebase.auth.currentUser?.let {
            var userRef = db.collection("users").document(it.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userRecipes = document.get("recipes") as ArrayList<String>?
                        if (userRecipes != null) {
                            for (r in userRecipes!!) {
                                val recipeRef = db.collection("recipes").document(r)
                                recipeRef.get()
                                    .addOnSuccessListener { recipeDocument ->
                                        val recipeTitle = recipeDocument.get("title")
                                        val recipeDescription = recipeDocument.get("description")

                                        val p = GridLayout.LayoutParams()
                                        val cardViewParams = GridLayout.LayoutParams()
                                        cardViewParams.columnSpec = GridLayout.spec(
                                            GridLayout.UNDEFINED,
                                            GridLayout.FILL, 1f
                                        )
                                        cardViewParams.width = 0
                                        cardViewParams.setMargins(20)
                                        val newCardView = CardView(this.requireContext())
                                        newCardView.layoutParams = cardViewParams
                                        newCardView.radius = 30f


                                        val newLinearLayout = LinearLayout(this.requireContext())
                                        newLinearLayout.orientation = LinearLayout.VERTICAL
                                        newLinearLayout.layoutParams = params
                                        newLinearLayout.setPadding(0, 0, 0, 30)
                                        newCardView.addView(newLinearLayout)

                                        val recipeImage = ImageView(this.requireContext())

                                        val imageRef =
                                            FirebaseStorage.getInstance().reference.child(r)
                                        imageRef.getBytes(ONE_MEGABYTE)
                                            .addOnSuccessListener { image ->
                                                recipeImage.setImageBitmap(
                                                    Bitmap.createScaledBitmap(
                                                        BitmapFactory.decodeByteArray(
                                                            image,
                                                            0,
                                                            image.size
                                                        ),
                                                        500,
                                                        500,
                                                        true
                                                    )
                                                )
                                            }

                                        newLinearLayout.addView(recipeImage)

                                        val newTitle = TextView(this.requireContext())
                                        newTitle.text = recipeTitle.toString()
                                        newTitle.setTextColor(Color.BLACK)
                                        newTitle.textSize =
                                            resources.getDimension(R.dimen.RecipetextSizeTitle)
                                        newTitle.setPadding(20, 0, 0, 0)

                                        val descriptionSubTitle = TextView(this.requireContext())
                                        descriptionSubTitle.text = "Description : "
                                        descriptionSubTitle.textSize =
                                            resources.getDimension(R.dimen.RecipetextSizeSubTitle)
                                        descriptionSubTitle.setPadding(20, 0, 0, 0)

                                        val newDescr = TextView(this.requireContext())
                                        newDescr.text = recipeDescription.toString()
                                        newDescr.setTextColor(Color.BLACK)
                                        newDescr.textSize =
                                            resources.getDimension(R.dimen.RecipetextSizeCorps)
                                        newDescr.setPadding(40, 0, 0, 0)

                                        newLinearLayout.addView(newTitle)
                                        newLinearLayout.addView(descriptionSubTitle)
                                        newLinearLayout.addView(newDescr)

                                        newCardView.setOnClickListener{
                                            val recipeIntent = Intent(this.requireContext(), Recipe::class.java)

                                            recipeIntent.putExtra("Title", newTitle.text.toString())
                                            recipeIntent.putExtra("Description", newDescr.text.toString())
                                            recipeIntent.putExtra("ID", r)

                                            startActivity(recipeIntent)
                                        }

                                        homeRecipesGridLayout.addView(newCardView)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("Error", "get failed with ", exception)
                                    }
                            }
                        }
                    } else
                        Log.d("Error", "Ce document n'existe pas")
                }
                .addOnFailureListener { exception ->
                    Log.d("Error", "get failed with ", exception)
                }
        }
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