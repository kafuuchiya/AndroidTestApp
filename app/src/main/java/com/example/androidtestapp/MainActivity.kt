package com.example.androidtestapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.githang.statusbar.StatusBarCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    // api
    private val urlApi: String = "https://api.hkmovie6.com/hkm/movies?type=showing"

    // value for helping show mode change
    private var isListView: Boolean = true

    // value for input data to adapter
    private val list = ArrayList<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // To set the color for status bar of phone
        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.myC_topBar))

        // To set the tool bar
        setSupportActionBar(toolbar)

        // To get data from the api
        getJsonDataToShow(urlApi)

    }

    private fun getJsonDataToShow(urlApi: String) {

        doAsync {

            // object or array key value for the json
            val movThumbnail = getString(R.string.movThumbnail)
            val movRating = getString(R.string.movRating)
            val movName = getString(R.string.movName)
            val movFavCo = getString(R.string.movFavCo)
            val movComCo = getString(R.string.movComCo)
            val movOpenDate = getString(R.string.movOpenDate)
            val timeFormat = getString(R.string.time_format)


            // To get Json string by the api
            val requestStr = Request(urlApi).run()
            val movies = JSONArray(requestStr)

            for (i in 0 until movies.length()) {
                val itemList = HashMap<String, String>()
                val jsonO = JSONObject(movies.get(i).toString())

                // To get movie info by the api
                itemList.put("mId", jsonO.getString("id"))

                // To get movie thumbnail
                if (jsonO.optString(movThumbnail).isNotEmpty()) {
                    itemList.put("mThumbnail", jsonO.getString(movThumbnail))
                } else {
                    itemList.put("mThumbnail", "")
                }

                // To get movie rating
                if (jsonO.optString(movRating).isNotEmpty()) {
                    val rating =
                        String.format("%.1f", (jsonO.getString(movRating).toDouble() * 0.01))
                    itemList.put("mRating", rating)
                } else {
                    itemList.put("mRating", "- -")
                }

                // To get movie name
                itemList.put("mName", jsonO.getString(movName))

                // To get movie favourite count
                if (jsonO.optString(movFavCo).isNotEmpty()) {
                    itemList.put("mFavCount", jsonO.getString(movFavCo))
                } else {
                    itemList.put("mFavCount", "0")
                }

                // To get the movie comment count
                if (jsonO.optString(movComCo).isNotEmpty()) {
                    itemList.put("mCommentCount", jsonO.getString(movComCo))
                } else {
                    itemList.put("mCommentCount", "0")
                }

                // To get movie open date and format it
                if (isListView || jsonO.optString(movOpenDate).isNotEmpty()) {
                    val sdf = SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss", Locale.US)
                    val openDate: Date = sdf.parse(jsonO.getString(movOpenDate))
                    var openDateSdf:String
                    if (timeFormat.indexOf("年") != -1){
                        openDateSdf = SimpleDateFormat(timeFormat).format(openDate)
                    }else{
                        openDateSdf = SimpleDateFormat(timeFormat, Locale.US).format(openDate)
                    }
                    itemList.put("mOpenDate", openDateSdf)
                }

                list.add(itemList)
            }

            uiThread {
                // To create the adapter
                val viewAdapter = ViewAdapter(applicationContext, list, isListView)
                if (isListView) {
                    // To set the list view
                    listV.adapter = viewAdapter
                    listV.setOnItemClickListener { _, _, i, _ ->
                        val mInfoApi =
                            "https://api.hkmovie6.com/hkm/movies/" + list[i].get("mId").toString()
                        val intent = Intent(applicationContext, MovieInfoActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("mInfoApi", mInfoApi)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                } else {
                    // To set the grid view
                    gridV.adapter = viewAdapter
                    gridV.setOnItemClickListener { _, _, i, _ ->
                        val mInfoApi =
                            "https://api.hkmovie6.com/hkm/movies/" + list[i].get("mId").toString()
                        val intent = Intent(applicationContext, MovieInfoActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("mInfoApi", mInfoApi)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }

            }

        }
    }


    // Menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_mode -> showModeSelectionDialog()
            R.id.action_other -> showLangSelectionDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }


    // Show mode selection dialog
    private fun showModeSelectionDialog(): Boolean {
        // To set the dialog option
        val option_one = getString(R.string.list_mode)
        val option_two = getString(R.string.grid_mode)
        val dialog_title = getString(R.string.dialog_T1)
        val items = arrayOf(option_one, option_two)
        var myChoice: Int

        // To set the selected option (default option)
        if (listV.visibility == View.VISIBLE) myChoice = 0 else myChoice = 1

        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            .setTitle(dialog_title)
            .setSingleChoiceItems(
                items,
                myChoice,
                DialogInterface.OnClickListener { _, i ->
                    myChoice = i
                })
            .setPositiveButton(
                getString(R.string.dialog_ok_btn),
                DialogInterface.OnClickListener { _, _ ->
                    when (myChoice) {
                        0 -> {
                            // option one
                            if (listV.visibility == View.GONE) {
                                gridV.visibility = View.GONE
                                listV.visibility = View.VISIBLE
                                isListView = true

                                getJsonDataToShow(urlApi)
                                list.clear()
                            }
                        }
                        1 -> {
                            // option two
                            if (listV.visibility == View.VISIBLE) {
                                listV.visibility = View.GONE
                                gridV.visibility = View.VISIBLE
                                isListView = false

                                getJsonDataToShow(urlApi)
                                list.clear()
                            }
                        }
                        else -> {
                            toast("error")
                        }
                    }
                })
        dialog.show()

        return true
    }

    // Show language selection dialog
    private fun showLangSelectionDialog(): Boolean {
        // To set the dialog option
        val option_one = getString(R.string.english)
        val option_two = getString(R.string.chinese)
        val dialog_title = getString(R.string.dialog_T2)
        val items = arrayOf(option_one, option_two)
        var myChoice: Int
        val localeUtils = LocaleUtils()

        // To set the selected option (default option)
        if (localeUtils.getCurrentLocale(this).toString().indexOf("en") != -1){
            myChoice = 0
        } else {
            myChoice = 1
        }

        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            .setTitle(dialog_title)
            .setSingleChoiceItems(
                items,
                myChoice,
                DialogInterface.OnClickListener { _, i ->
                    myChoice = i
                })
            .setPositiveButton(
                getString(R.string.dialog_ok_btn),
                DialogInterface.OnClickListener { _, _ ->
                    when (myChoice) {
                        0 -> {
                            // option one
                            // To check if language needs to be updated
                            if (localeUtils.needUpdateLocale(this, localeUtils.LOCALE_ENGLISH))
                            // To update the language to english
                                localeUtils.updateLocale(this, localeUtils.LOCALE_ENGLISH)
                            restartAct()
                        }
                        1 -> {
                            // option two
                            if (localeUtils.needUpdateLocale(this, localeUtils.LOCALE_CHINESE))
                            // To update the language to chinese
                                localeUtils.updateLocale(this, localeUtils.LOCALE_CHINESE)
                            restartAct()
                        }
                        else -> {
                            toast("error")

                        }
                    }
                })
        dialog.show()

        return true
    }

    // Restart now activity
    private fun restartAct() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Clear the animation of the activity exit and enter
        overridePendingTransition(0, 0)
    }

}