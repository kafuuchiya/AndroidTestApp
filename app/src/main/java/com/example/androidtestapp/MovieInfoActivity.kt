package com.example.androidtestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.githang.statusbar.StatusBarCompat
import kotlinx.android.synthetic.main.activity_movie_info.*
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MovieInfoActivity : AppCompatActivity() {

    var viewList = ArrayList<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_info)

        // To set the color for status bar of phone
        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.myC_topBar))


        // To get the height of phone screen to set the viewpager height
        val phoneHeight: Int = (this.displayMetrics.heightPixels * 0.33).toInt()
        val vp_params =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, phoneHeight)
        viewPager.layoutParams = vp_params

        // To get the bundle data from intent
        val bundle:Bundle? = this.intent.extras
        val mInfoApi:String = bundle?.get("mInfoApi").toString()

        // To get data from the api
        getJsonDataToShow(mInfoApi)
    }

    fun getJsonDataToShow(urlApi: String) {

        doAsync {

            // To get Json string by the api
            val requestStr:String = Request(urlApi).run()
            val movieInfo = JSONObject(requestStr)

            // screenshots link.
            // Including the video link change to screenshots link by the youtube link
            val vp_img_linkList = ArrayList<String>()
            // youtube link and screenshots link
            val vp_linkList = ArrayList<String>()

            var jsonArr: JSONArray
            if (movieInfo.optString("multitrailers").isNotEmpty()) {
                jsonArr = JSONArray(movieInfo.get("multitrailers").toString())
                // To add youtube link change to screenshots
                for (i in 0 until jsonArr.length()) {
                    val position = jsonArr[i].toString().indexOf("?v=") + 3
                    val uToImgLink = "http://img.youtube.com/vi/" +
                            jsonArr[i].toString().substring(position) + "/hqdefault.jpg"
                    // To add screenshots link of youtube
                    vp_img_linkList.add(uToImgLink)
                    // To add youtube link
                    vp_linkList.add(jsonArr[i].toString())
                }
            }

            if (JSONArray(movieInfo.optString("screenShots")).length() > 0) {
                jsonArr = JSONArray(movieInfo.get("screenShots").toString())
                // Add screenshots
                for (i in 0 until jsonArr.length()) {
                    vp_img_linkList.add(jsonArr[i].toString())
                    vp_linkList.add(jsonArr[i].toString())
                }
            } else {
                if (vp_img_linkList.size == 0) {
                    // add an item to show default image
                    vp_img_linkList.add("")
                    vp_linkList.add("")
                }
            }


            uiThread {
                // To set info for the text view
                setTextViewInfo(movieInfo)

                // To generate the image view for the viewpager
                for (i in 0 until vp_img_linkList.size) {
                    viewList.add(generateImageView(vp_img_linkList[i]))
                }

                // To set the viewpager adapter
                viewPager.adapter = ViewPagerAdapter(applicationContext, viewList, vp_linkList)
                setViewPagerPoints()

                // To set the page change listener for the viewpager bottom point
                var previousPointPosition: Int = 0   // default positon is 0
                viewPager.onPageChangeListener {
                    onPageSelected {
                        view_point_gp.getChildAt(it).isEnabled = true   // now position
                        view_point_gp.getChildAt(previousPointPosition).isEnabled =
                            false // set previous point to false
                        previousPointPosition = it
                    }
                }

                // To set the clicked listener for the “show more” of synopsis
                mInfo_synopsis.setOnClickListener {

                    if (mInfo_showMoreBtn.isVisible) {
                        mInfo_synopsis.maxLines = Int.MAX_VALUE
                        mInfo_showMoreBtn.visibility = View.GONE
                        toast("Click again to collapse")
                    } else {
                        mInfo_synopsis.maxLines = 4
                        mInfo_showMoreBtn.visibility = View.VISIBLE
                    }

                }
            }
        }


    }

    fun setTextViewInfo(movieInfo: JSONObject) {
        // object or array key value for the json
        val movRating = getString(R.string.movRating)
        val movName = getString(R.string.movName)
        val movFavCo = getString(R.string.movFavCo)
        val movComCo = getString(R.string.movComCo)
        val movOpenDate = getString(R.string.movOpenDate)
        val movDuration = getString(R.string.movDuration)
        val movCategory = getString(R.string.movCategory)
        val movInfoDict = getString(R.string.movInfoDict)
        val movSynopsis = getString(R.string.movSynopsis)
        val movDirector = getString(R.string.movDirector)
        val movCast = getString(R.string.movCast)
        val movGenre = getString(R.string.movGenre)
        val movLanguage = getString(R.string.movLanguage)
        val timeFormat = getString(R.string.time_format)

        // To set movie rating
        if (movieInfo.optString(movRating).isNotEmpty()) {
            val rating = String.format("%.1f", (movieInfo.getString(movRating).toDouble() * 0.01))
            mInfo_rating.text = rating
            mInfo_ratingStar.rating = rating.toFloat()
        } else {
            mInfo_rating.text = "- -"
        }

        // To set the movie name
        if (movieInfo.optString(movName).isNotEmpty())
            mInfo_name.text = movieInfo.getString(movName)


        // To set the movie favourite count
        if (movieInfo.optString(movFavCo).isNotEmpty())
            mInfo_favCount.text = movieInfo.getString(movFavCo)

        // To set the movie comment count
        if (movieInfo.optString(movComCo).isNotEmpty())
            mInfo_commentCount.text = movieInfo.getString(movComCo)

        // To format open date and set it
        if (movieInfo.optString(movOpenDate).isNotEmpty()) {
            val sdf = SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss", Locale.US)
            val openDate: Date = sdf.parse(movieInfo.getString(movOpenDate))
            val openDateSdf:String
            if (timeFormat.indexOf("年") != -1){
                openDateSdf = SimpleDateFormat(timeFormat).format(openDate)
            }else{
                openDateSdf = SimpleDateFormat(timeFormat, Locale.US).format(openDate)
            }
            mInfo_openDate.text = openDateSdf
        }

        // To set the movie duration
        if (movieInfo.optString(movDuration).isNotEmpty())
            mInfo_duration.text = movieInfo.getString(movDuration) + " " + getString(R.string.mins)


        // To set the movie category
        if (JSONObject(movieInfo.getString(movInfoDict)).optString(movCategory).isNotEmpty()) {
            val jsObj = JSONObject(movieInfo.getString(movInfoDict))
            mInfo_category.text = jsObj.getString(movCategory) + " " + getString(R.string.category)
        }


        // To set the movie synopsis
        if (movieInfo.optString(movSynopsis).isNotEmpty()) {
            mInfo_synopsis.text = movieInfo.getString(movSynopsis)
            if (mInfo_synopsis.lineCount > 4) {
                mInfo_showMoreBtn.visibility = View.VISIBLE
            }
            mInfo_synopsis.maxLines = 4
        }

        // To set the movie director
        if (JSONObject(movieInfo.optString(movInfoDict)).optString(movDirector).isNotEmpty()) {
            mInfo_director.text =
                JSONObject(movieInfo.getString(movInfoDict)).getString(movDirector)
            mInfo_director.visibility = View.VISIBLE
            mInfo_directorT.visibility = View.VISIBLE
        }


        // To set the movie cast
        if (JSONObject(movieInfo.optString(movInfoDict)).optString(movCast).isNotEmpty()) {
            mInfo_cast.text = JSONObject(movieInfo.getString(movInfoDict)).getString(movCast)
            mInfo_cast.visibility = View.VISIBLE
            mInfo_castT.visibility = View.VISIBLE
        }

        // To set the movie genre
        if (JSONObject(movieInfo.optString(movInfoDict)).optString(movGenre).isNotEmpty()) {
            mInfo_genre.text = JSONObject(movieInfo.getString(movInfoDict)).getString(movGenre)
            mInfo_genre.visibility = View.VISIBLE
            mInfo_genreT.visibility = View.VISIBLE
        }

        // To set the movie language
        if (JSONObject(movieInfo.optString(movInfoDict)).optString(movLanguage).isNotEmpty()) {
            mInfo_language.text =
                JSONObject(movieInfo.getString(movInfoDict)).getString(movLanguage)
            mInfo_language.visibility = View.VISIBLE
            mInfo_languageT.visibility = View.VISIBLE
        }

    }


    fun generateImageView(link: String): RelativeLayout {
        val relativeLayout = RelativeLayout(this)

        val imageView = ImageView(this)
        val imageViewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = imageViewParams
        // To set the online image to image view if it is not null
        if (link.isNotEmpty()) {
            Glide.with(applicationContext).load(link).into(imageView)
        } else {
            Glide.with(applicationContext).load(R.drawable.default_movie_img).into(imageView)
        }

        imageView.scaleType = ImageView.ScaleType.FIT_XY
        relativeLayout.addView(imageView)

        // To pretend to be a video player if it is youtube link
        if (link.contains("img.youtube.com")) {
            val imgV_play_icon = ImageView(this)
            val icon_params =
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 240)
            icon_params.addRule(RelativeLayout.CENTER_VERTICAL)
            imgV_play_icon.isFocusable = false
            imgV_play_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_24))
            imgV_play_icon.isFocusable = false
            imgV_play_icon.layoutParams = icon_params
            relativeLayout.addView(imgV_play_icon)
        }

        return relativeLayout
    }


    fun setViewPagerPoints() {
        // To make the viewpager point
        for (i in 0 until viewList.size) {
            val viewPoint = ImageView(this)
            val piont_params = LinearLayout.LayoutParams(20, 20)
            piont_params.rightMargin = 10
            viewPoint.layoutParams = piont_params
            viewPoint.isFocusable = false
            viewPoint.setBackgroundResource(R.drawable.viewpoint)
            when (i) {
                // true for focus, false for no focus
                // focus, current point
                0 -> viewPoint.isEnabled = true
                else -> viewPoint.isEnabled = false
            }
            view_point_gp.addView(viewPoint)
        }
    }
}