package com.example.androidtestapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_view_item.view.*
import org.jetbrains.anko.layoutInflater


class ViewAdapter constructor(
    getContext: Context,
    getList: ArrayList<Map<String, String>>,
    isListView: Boolean
) : BaseAdapter() {

    private val list = getList      // list view data
    private val context = getContext
    private val isListView = isListView     // value for helping show mode change


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var viewItem: View

        if (isListView) {
            viewItem = context.layoutInflater.inflate(R.layout.list_view_item, null)

            // To set the rating bar
            if (!list.get(p0).getValue("mRating").equals("- -"))
                viewItem.item_mRatingStar.rating = list.get(p0).getValue("mRating").toFloat()

            // To set the movie open date
            viewItem.item_mOpenDate.setText(list.get(p0).getValue("mOpenDate"))

        } else {
            viewItem = context.layoutInflater.inflate(R.layout.grid_view_item, null)
        }

        // To set the thumbnail (if not null), otherwise set it as the default icon
        if (list.get(p0).getValue("mThumbnail").length > 0) {
            Glide.with(context).load(list.get(p0).getValue("mThumbnail"))
                .into(viewItem.item_mThumbnail)
        } else {
            Glide.with(context).load(R.drawable.default_movie_img).into(viewItem.item_mThumbnail)
        }

        // To set some movie data for the view showing
        viewItem.item_mRating.setText(list.get(p0).getValue("mRating"))
        viewItem.item_mName.setText(list.get(p0).getValue("mName"))
        viewItem.item_mFavCount.setText(list.get(p0).getValue("mFavCount"))
        viewItem.item_mCommentCount.setText(list.get(p0).getValue("mCommentCount"))

        return viewItem
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}