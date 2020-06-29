package com.kakaovx.homet.tv.page.component.player

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView
import com.kakaovx.homet.tv.R

import com.skeleton.component.player.VideoViewPlayer
import kotlinx.android.synthetic.main.cp_custom_player.view.*

class CustomPlayer(context: Context, attrs: AttributeSet) : VideoViewPlayer(context, attrs) {
    override fun getPlayerView(): VideoView  = videoView
    override fun getLayoutResID(): Int = R.layout.cp_custom_player




}