/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kakaovx.homet.tv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnActionClickedListener
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import com.skeleton.page.PageDetailsSupportFragment
import com.kakaovx.homet.tv.page.MainActivity
import com.kakaovx.homet.tv.page.home.component.CardPresenter
import com.lib.util.Log


import java.util.Collections
import kotlin.math.round

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class VideoDetailsFragment : PageDetailsSupportFragment() {

    private var mSelectedMovie: Movie? = null

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate DetailsFragment")
        super.onCreate(savedInstanceState)

        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)
        if (mSelectedMovie != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupRelatedMovieListRow()
            adapter = mAdapter
            initializeBackground(mSelectedMovie)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPageParams(params: Map<String, Any?>) {
        mSelectedMovie = params[SHARE_IMAGE_KEY] as Movie
    }

    private fun initializeBackground(movie: Movie?) {
        mDetailsBackground.enableParallax()
        /*
        Glide.with(activity)
                .load(movie?.backgroundImageUrl)
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap,
                                                 glideAnimation: GlideAnimation<in Bitmap>) {
                        mDetailsBackground.coverBitmap = bitmap
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })

         */
    }

    private fun setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie?.toString())
        val row = DetailsOverviewRow(mSelectedMovie)
        activity ?: return
        row.imageDrawable = ContextCompat.getDrawable(activity!!, R.drawable.default_background)
        val width = convertDpToPixel(activity!!, DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(activity!!, DETAIL_THUMB_HEIGHT)
        /*
        Glide.with(activity)
                .load(mSelectedMovie?.cardImageUrl)
                .centerCrop()
                .error(R.drawable.default_background)
                .into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(width, height) {
                    override fun onResourceReady(resource: GlideDrawable,
                                                 glideAnimation: GlideAnimation<in GlideDrawable>) {
                        Log.d(TAG, "details overview card image url ready: " + resource)
                        row.imageDrawable = resource
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })

         */
        val actionAdapter = ArrayObjectAdapter()

        actionAdapter.add(
                Action(
                        ACTION_WATCH_TRAILER,
                        resources.getString(R.string.watch_trailer_1),
                        resources.getString(R.string.watch_trailer_2)))
        actionAdapter.add(
                Action(
                        ACTION_RENT,
                        resources.getString(R.string.rent_1),
                        resources.getString(R.string.rent_2)))
        actionAdapter.add(
                Action(
                        ACTION_BUY,
                        resources.getString(R.string.buy_1),
                        resources.getString(R.string.buy_2)))
        row.actionsAdapter = actionAdapter

        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        activity?.let {  detailsPresenter.backgroundColor = ContextCompat.getColor(it, R.color.selected_background) }


        // Hook up transition element.
        sharedElementEnterTransition = ChangeBounds()
        this.view?.transitionName = SHARE_IMAGE_NAME



        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            /*
            if (action.id == ACTION_WATCH_TRAILER) {
                val intent = Intent(activity, PlaybackActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)
                startActivity(intent)
            } else {
                Toast.makeText(activity, action.toString(), Toast.LENGTH_SHORT).show()
            }

             */
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupRelatedMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list

        Collections.shuffle(list)
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0 until NUM_COLS) {
            listRowAdapter.add(list[j % 5])
        }

        val header = HeaderItem(0, subcategories[0])
        mAdapter.add(ListRow(header, listRowAdapter))
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return round(dp.toFloat() * density).toInt()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder?,
                item: Any?,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {
            if (item is Movie) {
                Log.d(TAG, "Item: " + item.toString())
                /*
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra(resources.getString(R.string.movie), mSelectedMovie)

                val bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity,
                                (itemViewHolder?.view as ImageCardView).mainImageView,
                                DetailsActivity.SHARED_ELEMENT_NAME)
                                .toBundle()
                activity.startActivity(intent, bundle)

                 */
            }
        }
    }

    companion object {
        private const val TAG = "VideoDetailsFragment"

        private val ACTION_WATCH_TRAILER = 1L
        private val ACTION_RENT = 2L
        private val ACTION_BUY = 3L

        private val DETAIL_THUMB_WIDTH = 274
        private val DETAIL_THUMB_HEIGHT = 274

        private val NUM_COLS = 10

        const val SHARE_IMAGE_KEY = "${TAG}shareKey"
        const val SHARE_IMAGE_NAME = "${TAG}shareName"
    }
}