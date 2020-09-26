package com.example.androidtestapp

import java.net.URL

class Request(val url: String) {

    // To get the json string on another thread
    fun run():String {
        var jsonString:String = URL(url).readText()
        return jsonString
    }

}