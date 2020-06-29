package com.kakaovx.homet.tv.page.component.player

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.PlayerView
import com.kakaovx.homet.tv.R
import com.skeleton.component.player.ExoVideoPlayer
import kotlinx.android.synthetic.main.cp_exo_player.view.*

class ExoPlayer(context: Context, attrs: AttributeSet) : ExoVideoPlayer(context, attrs) {
    override fun getAppName(): Int = R.string.app_name
    override fun getPlayerView(): PlayerView = playerView
    override fun getLayoutResID(): Int = R.layout.cp_exo_player
}