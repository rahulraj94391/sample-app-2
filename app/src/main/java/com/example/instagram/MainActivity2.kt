package com.example.instagram

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 : AppCompatActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageUtil: ImageUtil

    private val URL1: String = "https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/2_0?alt=media&token=866d1edb-635c-4c64-bfcb-88024311d10a"
    private val URL2: String = "https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/2_1?alt=media&token=28a16edf-893e-46d0-8a25-6942fd9384fe"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        imageUtil = ImageUtil(this)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val bitmap1 = imageUtil.getBitmap(URL1)
            val bitmap2 = imageUtil.getBitmap(URL2)

            val cbm1 = imageUtil.resizePhoto(bitmap1)
            val cbm2 = imageUtil.resizePhoto(bitmap2)

            withContext(Dispatchers.Main) {
                imageView1.setImageBitmap(bitmap1)
                imageView2.setImageBitmap(bitmap2)

                imageView1.setImageBitmap(cbm1)
                imageView2.setImageBitmap(cbm2)
            }
        }
    }
}