package com.example.androidtestapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter

class ViewPagerAdapter constructor(
    content: Context,
    viewLists: ArrayList<View>,
    vp_linkList: ArrayList<String>
) : PagerAdapter() {

    var content = content
    var viewLists = viewLists       // list of view
    var vp_linkList = vp_linkList        // list of viewpager item link

    override fun getCount(): Int {
        return viewLists.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        // To set the onclick listener for each image view of the viewpager
        var imageView = viewLists.get(position)
        imageView.setOnClickListener {

            // if the link is the youtube link, intent it to browser
            if (vp_linkList[position].contains("youtube.com")) {

                var intent = Intent()
                intent.setData(Uri.parse(vp_linkList[position]))
                    .setAction(Intent.ACTION_VIEW)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                content.startActivity(intent)

            } else {
                Toast.makeText(
                    content,
                    "Screenshot is clicked. View " + (position + 1).toString(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        // To set the image view to viewpager
        container.addView(viewLists.get(position))

        return viewLists.get(position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewLists.get(position))
    }

}