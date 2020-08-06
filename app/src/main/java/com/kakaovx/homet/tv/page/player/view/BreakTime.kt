package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.factory.BgmResource
import com.kakaovx.homet.tv.page.component.factory.StaticResource
import com.kakaovx.homet.tv.page.component.factory.TTSFactory
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.kakaovx.homet.tv.page.player.model.FlagType
import com.kakaovx.homet.tv.page.player.model.PlayerUIEvent
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.BreakTimeData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.lib.page.PageComponentCoroutine
import com.lib.util.animateAlpha
import com.skeleton.component.graph.GraphBuilder
import kotlinx.android.synthetic.main.cp_break_time.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap
import kotlin.math.roundToLong


class BreakTime : PageComponentCoroutine, PlayerChildComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    override fun getLayoutResID(): Int = R.layout.cp_break_time
    private var playerViewModel:PagePlayerViewModel? = null
    private var ttsFactory:TTSFactory? = null
    private var exercise:Exercise? = null
    private var graph:GraphBuilder? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        alpha = 0.0f
        visibility = View.GONE
        graph = GraphBuilder(graphCount).setColor(R.color.color_white).setStroke(7.0f * context.resources.displayMetrics.density)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ttsFactory = null
        exercise = null
        playerViewModel = null
        exitFocusView = null
        graph = null
        countJob = null
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        btnSkip.setOnClickListener {
            if(isActive) passive()
        }
    }

    override fun onTTSFactory(ttsFactory: TTSFactory) {
        super.onTTSFactory(ttsFactory)
        this.ttsFactory = ttsFactory
    }

    override fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){
        this.playerViewModel = playerViewModel
        lifecycleOwner?.let { owner ->
            playerViewModel.repo.hometManager.success.observe(owner, Observer {e->
                e ?: return@Observer
                val type = e.type as? HometApiType
                type ?: return@Observer
                if(type != HometApiType.BREAT_TIME) return@Observer
                val res = e.data as? HomeTResponse<*>
                val breakData:BreakTimeData? = res?.data as? BreakTimeData
                breakData ?: return@Observer
                startCount(breakData)
            })

            playerViewModel.repo.hometManager.error.observe(owner, Observer {e->
                e ?: return@Observer
                val type = e.type as? HometApiType
                type ?: return@Observer
                if(type != HometApiType.BREAT_TIME) return@Observer
                startCount()
            })
        }
    }

    override fun onExercise(exercise:Exercise){
        this.exercise = exercise
        lifecycleOwner?.let { owner->
            exercise.changeFlagObservable.observe(owner, Observer {flag->
                if(flag.type != FlagType.Break) return@Observer

                val period = 1000L
                duration = flag.duration
                totalCount  = (flag.duration.toDouble() / period).roundToLong()
                graph?.setRange(totalCount.toDouble())
                textCount.text = totalCount.toString()
                active(exercise.id)
                /*
                textNext.visibility=View.GONE
                exercise.nextFlag?.let {
                    textNext.visibility=View.VISIBLE
                    textNext.text = "${it.getFlagTitle()} ${it.getFlagStep()}/${exercise.totalStep}"
                }
                */

            })
        }
    }
    private fun startCount(breakData:BreakTimeData? = null){
        breakData?.let {
            textSubTitle.text = breakData.title
            Glide.with(context)
                .load(it.imgUrl)
                .centerCrop()
                .error( ContextCompat.getDrawable(context, R.drawable.ic_content_no_image) )
                .into(imageBg)
        }

        if(breakData == null){ }
        currentCount = 0
        run()
    }

    override fun onPagePause() {
        super.onPagePause()
        countJob?.cancel()
        ttsFactory?.onResume()
    }

    override fun onPageResume() {
        super.onPageResume()
        if(isActive) run()
        ttsFactory?.onPause()
    }

    private fun run(){
        countJob?.cancel()

        countJob = scope.launch(){
            delay(1000)
            while (isActive ){
                currentCount ++
                var r = totalCount - currentCount
                if( r < 0 ) r = 0
                textCount.text = (r).toString()
                if(r == 3L) textTitle.text = context.getString(R.string.page_player_breaktime_end_info)
                graph?.show(currentCount.toDouble())
                if(currentCount == totalCount + 1) passive()
                btnSkip.requestFocus()
                delay(1000)
            }
        }
    }

    private var countJob:Job? = null
    private var duration = 0L
    private var totalCount = 0L
    private var currentCount = 0L
    private var isActive =  false
    private fun active(exerciseID:String){
        ttsFactory?.effect(StaticResource.BREAK_TIME)
        playerViewModel?.apply {
            player.uiEvent.value = PlayerUIEvent.UIHidden
            player.uiEvent.value = PlayerUIEvent.ListHidden
        }
        ttsFactory?.playBgm(BgmResource.BREAK_BGM)
        graph?.set(0.0)
        textCount.text = ""
        textTitle.text = context.getString(R.string.page_player_breaktime_info)
        btnSkip.requestFocus()
        lifecycleOwner?.let {owner->
            val params = HashMap<String, String>()
            params[ApiField.EXERCISE_ID] = exerciseID
            playerViewModel?.repo?.hometManager?.loadApi(owner ,HometApiType.BREAT_TIME, params)
        }
        this.animateAlpha(1.0f)
        isActive =  true

    }

    var exitFocusView:View? = null
    private fun passive(){
        ttsFactory?.stopBgm()
        isActive =  false
        playerViewModel?.let {vm->
            vm.player.uiEvent.value = PlayerUIEvent.UIView
            vm.player.uiEvent.value = PlayerUIEvent.Resume
        }
        exitFocusView?.requestFocus()
        exercise?.changeVideoTime(duration)
        exercise?.completeBreakTime()
        this.animateAlpha(0.0f)
    }


}