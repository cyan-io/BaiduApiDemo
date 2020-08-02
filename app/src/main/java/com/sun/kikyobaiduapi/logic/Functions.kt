package com.sun.kikyobaiduapi.logic

import com.google.gson.Gson
import java.lang.Exception


data class Location(val width: Int, val top: Int, val left: Int, val height: Int)

data class Words_result(val location: Location, val words: String)

data class Baidu(val log_id: Long, val words_result_num: Int, val words_result: List<Words_result>)

fun toJsonObj(string: String):Baidu?{
    return try {
        Gson().fromJson<Baidu>(string, Baidu::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}