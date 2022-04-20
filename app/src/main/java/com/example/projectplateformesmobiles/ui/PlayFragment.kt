package com.example.projectplateformesmobiles.ui

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.example.projectplateformesmobiles.R
import com.example.projectplateformesmobiles.TimerService
import com.example.projectplateformesmobiles.databinding.ActivityPlayModeBinding
import kotlinx.android.synthetic.main.fragment_play.*
import kotlin.math.roundToInt


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

    private lateinit var binding: ActivityPlayModeBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0
    private var initTime = 0.0

    private lateinit var timePrecision : String
    private var isResetting = false
    private lateinit var startStopButton: Button
    private lateinit var resetButton: Button
    private lateinit var timeTV: TextView
    private lateinit var timerLayout: View
    private lateinit var timerParentLayout: ViewGroup
    private lateinit var ID: String
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
        if (bundle?.getSerializable("duration") != null)
            time = (bundle?.getSerializable("duration") as String).toDouble()
        if (bundle?.getSerializable("timePrecision") != null)
            timePrecision = bundle?.getSerializable("timePrecision") as String
        if (bundle?.getSerializable("ID") != null)
            ID = bundle?.getSerializable("ID") as String
        initTime = time

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_play, container, false)

        val closeButton: Button = view.findViewById(R.id.play_end)
        closeButton.setOnClickListener {
            activity?.finish()
            //val RecipeIntent = Intent(this.requireContext(), Recipe::class.java)
            //RecipeIntent.putExtra("ID", ID)
            //startActivity(RecipeIntent)
        }

        val previousStepButton: Button = view.findViewById(R.id.previousStep)
        timerLayout = view.findViewById(R.id.timerSettings)
        timerParentLayout = view.findViewById(R.id.stepInfos)
        if (step == 0) {
            previousStepButton.isEnabled = false
            previousStepButton.background = resources.getDrawable(R.drawable.disabled_button)
            previousStepButton.setTextColor(Color.DKGRAY)
        }
        if (time == null || time == 0.0){
            timerLayout.visibility = View.GONE
            //timerParentLayout.removeView(timerLayout)
        }else {
            timerLayout.visibility = View.VISIBLE
            //timerParentLayout.addView(timerLayout)
        }

        previousStepButton.setOnClickListener {
            (activity as Play_mode).previsousStep()
        }

        val nextStepButton: Button = view.findViewById(R.id.nextStep)
        if (step == stepsNumber) {
            nextStepButton.text = resources.getString(R.string.Finish)
            nextStepButton.setOnClickListener {
                activity?.finish()
                val RecipeIntent = Intent(this.requireContext(), Recipe::class.java)
                RecipeIntent.putExtra("ID", ID)
                startActivity(RecipeIntent)
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

        //binding = ActivityPlayModeBinding.inflate(layoutInflater)
        startStopButton = view.findViewById<Button>(R.id.startStopButton)
        resetButton = view.findViewById<Button>(R.id.resetButton)
        timeTV = view.findViewById<TextView>(R.id.timeTV)
        timeTV.text = makeTimeString(0, 0, time.toInt())
        startStopButton.setOnClickListener {
            if(timerStarted)
                stopTimer()
            else
                startTimer() }
        resetButton.setOnClickListener {
            isResetting = true
            stopTimer()
            timeTV.text = getTimeStringFromDouble(initTime)
        }

        serviceIntent = Intent(getActivity()?.getApplicationContext(), TimerService::class.java)
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        for ((k, v) in ingredients!!) {
            if (k != "" &&  k != " ") {
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
            descriptionSubTitle.textSize =
                resources.getDimension(R.dimen.RecipeActivitySubTitleProg)

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

    private fun startTimer()
    {
        if (isResetting){
            time = initTime
            isResetting = false
        }
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)

        getActivity()?.startService(serviceIntent)

        startStopButton.text = "Stop"
        //startStopButton.setCompoundDrawables(Drawable(R.drawable.ic_baseline_pause_24))
        timerStarted = true
    }

    private fun stopTimer()
    {
        getActivity()?.stopService(serviceIntent)
        startStopButton.text = "Start"
        //startStopButton.icon = getDrawable(R.drawable.ic_baseline_play_arrow_24)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {

            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            if (time <= 0.0){
                val intent = Intent(activity, Play_mode::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                var builder = NotificationCompat.Builder(requireActivity(), "timer Stopped")
                    .setSmallIcon(R.drawable.logo_app_green_blob_prog)
                    .setContentTitle("Fin d'étape")
                    .setContentText("Vous pouvez continuer la recette")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(requireActivity())) {
                    // notificationId is a unique int for each notification that you must define
                    notify(1, builder.build())
                }
            }
            timeTV.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String
    {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)


}