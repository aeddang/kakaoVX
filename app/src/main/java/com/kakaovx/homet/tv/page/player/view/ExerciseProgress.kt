package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.lib.page.PageComponentCoroutine
import kotlinx.android.synthetic.main.cp_exercise_progress.view.*


class ExerciseProgress : PageComponentCoroutine, PlayerChildComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    override fun getLayoutResID(): Int = R.layout.cp_exercise_progress

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){

    }

    override fun onExercise(exercise:Exercise){
        progressBar.max = exercise.duration.toInt()
        lifecycleOwner?.let { owner->
            exercise.progressTime.observe(owner, Observer {
                progressBar.progress = it.toInt()
            })
        }
    }

}