package com.sun.kikyobaiduapi

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sun.kikyobaiduapi.baidu.Base64Util
import com.sun.kikyobaiduapi.logic.toJsonObj
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_CHOOSE = 1
        //ACCESS TOKEN
        const val ACCESS_TOKEN = Config.ACCESS_TOKEN
    }

    lateinit var imageView: ImageView
    lateinit var processBar: ProgressBar
    lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        processBar = findViewById(R.id.processBar)
        imageView = findViewById(R.id.imageView)
        fab = findViewById(R.id.fab)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ),
                2
            )
        }
        /*try {
            draw()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        fab.setOnClickListener {
            Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(false)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(GlideEngine())
                .capture(true)
                .captureStrategy(
                    CaptureStrategy(
                        true,
                        "com.zhihu.matisse.sample.fileprovider",
                        "kikyobaiduapi"
                    )
                )
                .showPreview(false) // Default is `true`
                .forResult(REQUEST_CODE_CHOOSE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                REQUEST_CODE_CHOOSE -> {
                    var uri: Uri? = null
                    Matisse.obtainResult(data)?.let {
                        uri = it[0]
                    }
                    processBar.visibility = View.VISIBLE
                    thread {
                        uri?.let {
                            val bitmap =
                                BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                                    .copy(Bitmap.Config.ARGB_8888, true)
                            //Log.i("__canvas", "canvas ${bitmap.width}X${bitmap.height}")


                            val baos = ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            baos.flush();
                            baos.close();

                            val bitmapBytes = baos.toByteArray();

                            val img = Base64Util.encode(bitmapBytes)
                            Log.i("__base64", img)


                            val client = OkHttpClient.Builder().connectionSpecs(
                                listOf(
                                    ConnectionSpec.COMPATIBLE_TLS,
                                    ConnectionSpec.CLEARTEXT,
                                    ConnectionSpec.MODERN_TLS,
                                    ConnectionSpec.RESTRICTED_TLS
                                )
                            ).build()
                            val requestBody = FormBody.Builder().add("image", img).build()
                            val request = Request.Builder()
                                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate?access_token=${ACCESS_TOKEN}")
                                .addHeader("content-type", "application/x-www-form-urlencoded")
                                .post(requestBody)
                                .build()
                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    e.printStackTrace()
                                    Looper.prepare()
                                    Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT)
                                        .show()
                                    Looper.loop()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseBody = response.body?.string()
                                    Log.i("__post", responseBody + "null")
                                    responseBody?.let {
                                        val baidu = toJsonObj(responseBody)
                                        val canvas = Canvas(bitmap)
                                        val paint = Paint()
                                        paint.strokeWidth = bitmap.width / 300f
                                        paint.setARGB(127, 255, 0, 0)
                                        paint.style = Paint.Style.STROKE
                                        for (i in baidu!!.words_result) {
                                            val location = i.location
                                            canvas.drawRect(
                                                location.left.toFloat(),
                                                location.top.toFloat(),
                                                location.left + location.width.toFloat(),
                                                location.height + location.top.toFloat(),
                                                paint
                                            )
                                        }
                                        runOnUiThread {
                                            processBar.visibility = View.GONE
                                            Glide.with(this@MainActivity).load(bitmap)
                                                .into(imageView)
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
    }
}