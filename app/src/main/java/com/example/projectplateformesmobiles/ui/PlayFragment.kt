package com.example.projectplateformesmobiles.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import com.example.projectplateformesmobiles.R
import kotlinx.android.synthetic.main.fragment_play.*


/**
 * A simple [Fragment] subclass.
 * Use the [PlayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayFragment : Fragment() {
    private var title: String? = null
    private var ingredients: HashMap<String, String>? = null
    private var description: String? = null
    private var step: Int = 0
    private var stepsNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle: Bundle? = arguments
        if (bundle?.getSerializable("title") != null)
            title = bundle?.getSerializable("title") as String
        if (bundle?.getSerializable("stepsNumber") != null)
            stepsNumber = bundle?.getSerializable("stepsNumber") as Int
        if (bundle?.getSerializable("step") != null)
            step = bundle?.getSerializable("step") as Int
        if (bundle?.getSerializable("description") != null)
            description = bundle?.getSerializable("description") as String
        if (bundle?.getSerializable("ingredients") != null)
            ingredients = bundle?.getSerializable("ingredients") as HashMap<String, String>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_play, container, false)

        val closeButton: Button = view.findViewById(R.id.play_end)
        closeButton.setOnClickListener {
            activity?.finish()
        }

        val previousStepButton: Button = view.findViewById(R.id.previousStep)
        if (step == 0) {
            previousStepButton.isEnabled = false
            previousStepButton.background = resources.getDrawable(R.drawable.disabled_button)
            previousStepButton.setTextColor(Color.DKGRAY)
        }
        previousStepButton.setOnClickListener {
            (activity as Play_mode).previsousStep()
        }

        val nextStepButton: Button = view.findViewById(R.id.nextStep)
        if (step == stepsNumber) {
            nextStepButton.text = resources.getString(R.string.Finish)
            nextStepButton.setOnClickListener {
                activity?.finish()
            }
        } else {
            nextStepButton.setOnClickListener {
                (activity as Play_mode).nextStep()
            }
        }

        val titleTextView: TextView = view.findViewById(R.id.play_title)
        titleTextView.text = title

        val stepInfosLayout: LinearLayout = view.findViewById(R.id.stepInfos)

        val ingredientsGridLayout: GridLayout = view.findViewById(R.id.play_mode_ingredients)
        for ((k, v) in ingredients!!) {
            val cardViewLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardViewLayoutParams.setMargins(
                resources.getDimension(R.dimen.RecipeMargin).toInt()
            )

            val ingredientsCardview = CardView(this.requireContext())
            ingredientsCardview.layoutParams = cardViewLayoutParams
            ingredientsCardview.radius = 30f


            val linearLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val newLinearLayout = LinearLayout(this.requireContext())
            newLinearLayout.layoutParams = linearLayoutParams
            newLinearLayout.orientation = LinearLayout.HORIZONTAL


            val textViewLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textViewLayoutParams.setMargins(
                resources.getDimension(R.dimen.RecipeMargin).toInt()
            )

            val newTextView = TextView(this.requireContext())
            newTextView.text = k
            newTextView.setTextColor(Color.BLACK)
            newTextView.textSize = resources.getDimension(R.dimen.RecipeActivityIngredients)
            newTextView.layoutParams = textViewLayoutParams

            newLinearLayout.addView(newTextView)
            ingredientsCardview.addView(newLinearLayout)
            ingredientsGridLayout.addView(ingredientsCardview)

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

                val newTextViewQuantity = TextView(this.requireContext())
                newTextViewQuantity.text = "x" + v
                newTextViewQuantity.textSize =
                    resources.getDimension(R.dimen.RecipeActivityIngredients)
                newTextViewQuantity.layoutParams = quantityLayoutParams
                newLinearLayout.addView(newTextViewQuantity)
            }
        }

        if (description != null) {
            val descriptionParam = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            descriptionParam.setMargins(resources.getDimension(R.dimen.horizontal_padding).toInt())

            val descriptionLayout = LinearLayout(this.requireContext())
            descriptionLayout.orientation = LinearLayout.VERTICAL
            descriptionLayout.layoutParams = descriptionParam

            stepInfosLayout.addView(descriptionLayout)

            val descriptionSubTitle = TextView(this.requireContext())
            descriptionSubTitle.text = "Description"
            descriptionSubTitle.setTextColor(Color.WHITE)
            descriptionSubTitle.textSize = resources.getDimension(R.dimen.RecipeActivitySubTitleProg)

            val descriptionTextView = TextView(this.requireContext())
            descriptionTextView.text = description
            descriptionTextView.setTextColor(Color.WHITE)
            descriptionTextView.textSize = resources.getDimension(R.dimen.RecipeActivityIngredients)
            descriptionTextView.layoutParams = descriptionParam

            descriptionLayout.addView(descriptionSubTitle)
            descriptionLayout.addView(descriptionTextView)
        }

        return view;
    }


}