package com.example.androidtestapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.DisplayMetrics
import com.google.gson.Gson
import java.util.*


class LocaleUtils {

    // chinese
    val LOCALE_CHINESE: Locale = Locale.TRADITIONAL_CHINESE

    // english
    val LOCALE_ENGLISH: Locale = Locale.ENGLISH

    // SharedPreferences file name
    private val LOCALE_FILE: String = "LOCALE_FILE"

    // To save key of Locale
    private val LOCALE_KEY = "LOCALE_KEY"

    // To get user locale setting
    fun getUserLocale(pContext: Context): Locale {
        val _SpLocale:SharedPreferences = pContext.getSharedPreferences(LOCALE_FILE, Context.MODE_PRIVATE)
        val _LocaleJson:String = _SpLocale.getString(LOCALE_KEY, "").toString()
        return jsonToLocale(_LocaleJson)
    }

    // To get current locale
    fun getCurrentLocale(pContext: Context): Locale {
        val _Locale: Locale
        _Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pContext.resources.configuration.locales[0]
        } else {
            pContext.resources.configuration.locale
        }
        return _Locale
    }

    // To save settings of user locale
    fun saveUserLocale(
        pContext: Context,
        pUserLocale: Locale
    ) {
        val _SpLocal = pContext.getSharedPreferences(LOCALE_FILE, Context.MODE_PRIVATE)
        val _Edit = _SpLocal.edit()
        val _LocaleJson: String = localeToJson(pUserLocale)
        _Edit.putString(LOCALE_KEY, _LocaleJson)
        _Edit.apply()
    }

    // To transform the locale to the json
    private fun localeToJson(pUserLocale: Locale): String {
        val _Gson = Gson()
        return _Gson.toJson(pUserLocale)
    }

    // To transform the json to the locale
    private fun jsonToLocale(pLocaleJson: String): Locale {
        val _Gson = Gson()
        return _Gson.fromJson(pLocaleJson, Locale::class.java)
    }

    // To update locale
    fun updateLocale(
        pContext: Context,
        pNewUserLocale: Locale
    ) {
        if (needUpdateLocale(pContext, pNewUserLocale)) {
            val _Configuration =
                pContext.resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                _Configuration.setLocale(pNewUserLocale)
            } else {
                _Configuration.locale = pNewUserLocale
            }
            val _DisplayMetrics:DisplayMetrics = pContext.resources.displayMetrics
            pContext.resources.updateConfiguration(_Configuration, _DisplayMetrics)
            saveUserLocale(pContext, pNewUserLocale)
        }
    }

    // To judge update of locale
    fun needUpdateLocale(
        pContext: Context,
        pNewUserLocale: Locale
    ): Boolean {
        return pNewUserLocale != null && getCurrentLocale(pContext) != pNewUserLocale
    }

}