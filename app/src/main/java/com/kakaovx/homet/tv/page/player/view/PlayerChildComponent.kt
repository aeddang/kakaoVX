package com.kakaovx.homet.tv.page.player.view

import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise

interface PlayerChildComponent {
    fun onPlayerViewModel(playerViewModel:PagePlayerViewModel)
    fun onExercise(exercise: Exercise)
}