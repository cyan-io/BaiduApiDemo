package com.sun.kikyobaiduapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Test

import java.lang.Exception

class ExampleUnitTest {
    @Test
    fun main() {

        val string = "{\n" +
                "  \"log_id\": 1036601312626046095,\n" +
                "  \"words_result_num\": 133,\n" +
                "  \"words_result\": [\n" +
                "    {\n" +
                "      \"location\": {\n" +
                "        \"width\": 85,\n" +
                "        \"top\": 519,\n" +
                "        \"left\": 524,\n" +
                "        \"height\": 382\n" +
                "      },\n" +
                "      \"words\": \"信息技术丛书\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"location\": {\n" +
                "        \"width\": 49,\n" +
                "        \"top\": 861,\n" +
                "        \"left\": 2900,\n" +
                "        \"height\": 42\n" +
                "      },\n" +
                "      \"words\": \"第\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        try {
            /*val gson = Gson()
            val typeOf = object : TypeToken<List<Words_result>>() {}.type
            val list = gson.fromJson<List<Words_result>>(string, typeOf)
            for (i in list) {
                println(i)
            }*/
            val d = Gson().fromJson<Baidu>(string, Baidu::class.java)
            print(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class Location(val width: Int, val top: Int, val left: Int, val height: Int)

data class Words_result(val location: Location, val words: String)

data class Baidu(val log_id: Long, val words_result_num: Int, val words_result: List<Words_result>)