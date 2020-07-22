package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.kakaovx.homet.tv.page.player.model.FlagType
import com.kakaovx.homet.tv.page.player.model.PlayerUIEvent
import com.kakaovx.homet.tv.util.millisecToTimeString
import com.lib.page.PageComponentCoroutine
import com.lib.util.Log
import com.lib.util.animateAlpha
import com.lib.util.animateFrame
import kotlinx.android.synthetic.main.cp_video_progress.view.*
import kotlin.math.roundToInt

class VideoProgress : PageComponentCoroutine, PlayerChildComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    override fun getLayoutResID(): Int = R.layout.cp_video_progress

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }
    private val moveTime = 5000L
    private var startTime:Long = 0
    private var duration:Long = 0
    override fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val seek = PlayerUIEvent.Seek
                seek.value = (seekBar?.progress ?: 0).toLong() + startTime
                playerViewModel.player.uiEvent.value = seek
            }
        })

        progressBar.setOnKeyListener { v, keyCode, event ->
            if(event.action != KeyEvent.ACTION_UP) return@setOnKeyListener false
            when(keyCode){
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if(progressBar.progress < moveTime) return@setOnKeyListener false
                    val seek = PlayerUIEvent.SeekMove
                    seek.value = -moveTime
                    playerViewModel.player.uiEvent.value = seek
                    true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT ->{
                    val willTime = progressBar.progress + moveTime
                    if(progressBar.max < willTime) return@setOnKeyListener false
                    val seek = PlayerUIEvent.SeekMove
                    seek.value = moveTime
                    playerViewModel.player.uiEvent.value = seek
                    true
                }

                else -> false
            }
        }
        progressBar.setOnFocusChangeListener { v, hasFocus ->
            val next  = v.findFocus()
            Log.i(appTag, "next ${next}")
            playerViewModel.player.uiEvent.value = if(hasFocus)  PlayerUIEvent.UIUse
            else PlayerUIEvent.UIView
        }

    }


    override fun onExercise(exercise:Exercise){
        lifecycleOwner?.let { owner->
            exercise.changedFlagObservable.observe(owner, Observer { flag->
                if(flag.type == FlagType.Break) return@Observer
                duration = flag.duration
                progressBar.max = flag.duration.toInt()
                startTime = flag.movieStartTime
                textDuration.text = duration.millisecToTimeString()
                textTime.text = 0L.millisecToTimeString()
                val progress = "${flag.getFlagStep()}"
                val total = "/${exercise.totalStep}"
                val text =  SpannableString("$progress/${exercise.totalStep} ${flag.getFlagTitle()}")
                text.setSpan(StyleSpan(Typeface.BOLD), progress.length, progress.length + total .length, 0)
                textTitle.text = text
                val res = when(flag.type){
                    FlagType.Action -> R.drawable.shape_exercise_progress
                    FlagType.Motion -> R.drawable.shape_exercise_progress
                    else ->  R.drawable.shape_player_progress
                }
                progressBar.progressDrawable = context.getDrawable(res)
            })
            exercise.movieObservable.observe(owner, Observer { movie->
                movie.currentTime.observe(owner,Observer {
                    val t = it - startTime
                    textTime.text = t.millisecToTimeString()
                    progressBar.progress = t.toInt()
                })
            })
        }
    }

}