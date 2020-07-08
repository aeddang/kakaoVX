package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.kakaovx.homet.tv.page.player.model.FlagType
import com.kakaovx.homet.tv.page.player.model.PlayerStatus
import com.kakaovx.homet.tv.page.player.model.PlayerUIEvent
import com.lib.page.PageComponentCoroutine
import com.lib.util.Log
import com.lib.util.animateAlpha
import kotlinx.android.synthetic.main.cp_exercise_info.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ExerciseInfo : PageComponentCoroutine, PlayerChildComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    override fun getLayoutResID(): Int = R.layout.cp_exercise_info
    private var playerViewModel:PagePlayerViewModel? = null
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        alpha = 0.0f
        visibility = View.GONE
        btnHidden.alpha = 0.0f
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        playerViewModel = null
        exitFocusView = null

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()

    }

    override fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){
        this.playerViewModel = playerViewModel
    }

    override fun onExercise(exercise:Exercise){
        lifecycleOwner?.let { owner->
            exercise.changeFlagObservable.observe(owner, Observer {flag->
                if(flag.type == FlagType.Break) return@Observer
                textInfo.text = flag.getFlagTitle()
                textStep.text = flag.getFlagStepSpan(exercise.totalStep)
                if(flag.type == FlagType.Action && !flag.hasMotion){

                }else if(flag.type == FlagType.Motion){

                }
                active()
            })
        }
    }

    private fun active(){
        playerViewModel?.apply {
            player.playerStatus = PlayerStatus.Stop
            player.uiEvent.value = PlayerUIEvent.Pause
            player.uiEvent.value = PlayerUIEvent.UIHidden
        }
        btnHidden.requestFocus()
        this.animateAlpha(1.0f)
        scope.launch {
            delay(2000L)
            passive()
        }
    }

    var exitFocusView:View? = null
    private fun passive(){
        playerViewModel?.apply {
            player.playerStatus = PlayerStatus.Pause
            player.uiEvent.value = PlayerUIEvent.Resume
        }

        exitFocusView?.requestFocus()
        this.animateAlpha(0.0f)
    }


}