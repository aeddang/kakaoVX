package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.kakaovx.homet.tv.page.player.model.FlagType
import com.kakaovx.homet.tv.page.player.model.PlayerUIEvent
import com.lib.page.PageComponentCoroutine
import com.lib.util.Log
import com.lib.util.animateAlpha
import kotlinx.android.synthetic.main.cp_video_progress.view.*

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
    private val moveTime = 1000L
    private var startTime:Int = 0
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


        lifecycleOwner?.let { owner->
            playerViewModel.player.uiEvent.observe(owner, Observer {evt->
                when(evt){
                    PlayerUIEvent.ListView -> onHidden()
                    PlayerUIEvent.ListHidden -> onView()
                    PlayerUIEvent.UIUse -> onView()
                    PlayerUIEvent.UIView -> onView()
                    PlayerUIEvent.UIHidden -> onHidden()
                    else -> {}
                }
            })
        }
    }


    override fun onExercise(exercise:Exercise){
        lifecycleOwner?.let { owner->
            exercise.changedFlagObservable.observe(owner, Observer { flag->
                if(flag.type == FlagType.Break) return@Observer
                duration = flag.duration
                progressBar.max = flag.duration.toInt()
                startTime = flag.movieStartTime.toInt()

                textTitle.text = flag.getFlagTitle()
                textStep.text = flag.getFlagStepSpan(exercise.totalStep)
                val res = when(flag.type){
                    FlagType.Action -> R.drawable.shape_exercise_action_progress
                    FlagType.Motion -> R.drawable.shape_exercise_progress
                    else ->  R.drawable.shape_player_progress
                }
                progressBar.progressDrawable = context.getDrawable(res)
            })
            exercise.movieObservable.observe(owner, Observer { movie->
                movie.currentTime.observe(owner,Observer {
                    progressBar.progress = it.toInt() - startTime
                })
            })
        }
    }

    private var isView = true
    private fun onHidden(){
        if(!isView) return
        isView = false
        this.animateAlpha(0.0f, false)

    }
    private fun onView(){
        if(isView) return
        isView = true
        this.animateAlpha(1.0f, false)
    }
}