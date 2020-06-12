package com.skeleton.component.item

import android.util.Log
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.kakaovx.homet.tv.store.api.homet.ProgramData


abstract class ItemPresenter : Presenter() {
    companion object {
        private val TAG = "ItemPresenter "
        private var CARD_WIDTH = 313
        private var CARD_HEIGHT = 176
    }


    abstract fun getItemView():ItemImageCardView
    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val cardView = getItemView()
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return Presenter.ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val itemView = viewHolder.view as ItemImageCardView
        itemView.onBind(item)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val itemView = viewHolder.view as ItemImageCardView
        itemView.onUnbind()
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val itemView = view as ItemImageCardView
        itemView.updateBackgroundColor(selected)
    }


}