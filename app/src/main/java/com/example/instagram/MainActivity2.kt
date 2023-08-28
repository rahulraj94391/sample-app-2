package com.example.instagram

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.instagram.databinding.ActivityMain2Binding
import java.io.IOException

private const val TAG = "MainActivity2_CommTag"

class MainActivity2 : AppCompatActivity() {
    private var _binding: ActivityMain2Binding? = null
    private val binding get() = _binding!!
    private var bitmap: Bitmap? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
    }
    
    override fun onStart() {
        super.onStart()
        binding.loadImageBtn.setOnClickListener { getImageURI.launch("image/*") }
        binding.checkSpecsBtn.setOnClickListener { checkImageSpecs() }
        binding.scaleImgBtn.setOnClickListener {
            binding.imageView.setImageBitmap(doScaling(bitmap))
            
        }
    }
    
    private fun doScaling(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        val imageViewRatio = 4.toDouble() / 5.toDouble()
        var w = 0.0
        var h = 0.0
        val desiredRatio = 0.8
        var newHeight = 720.0
        var newWidth = desiredRatio * newHeight
        
        bitmap.apply {
            w = this.width.toDouble()
            h = this.height.toDouble()
        }
        val ratio = w / h
        if (h <= newHeight && w <= newWidth) return bitmap
        if (ratio < imageViewRatio) {
            newWidth = ratio * newHeight
        } else if (ratio > imageViewRatio) {
            newHeight = newWidth / ratio
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)
        /*this.bitmap = Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)
        return this.bitmap*/
    }
    
    private fun checkImageSpecs(): String {
        var w = -1
        var h = -1
        bitmap!!.apply {
            w = this.width
            h = this.height
        }
        
        val aspRatio = (w.toDouble() / h.toDouble())
        val msg = "w = $w\nh = $h\naspRatio = $aspRatio\nbyteCount = ${(bitmap!!.byteCount) / 1e+6}"
        Log.d(TAG, msg)
        return msg
    }
    
    private val getImageURI = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            Toast.makeText(this, "result URI is Null", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        bitmap = getBitmapFromUri(uri)
        if (bitmap == null) {
            Toast.makeText(this, "error converting image uri to Bitmap.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        binding.imageView.setImageBitmap(getBitmapFromUri(uri))
    }
    
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            bitmap // convert the bitmap with the filter into the downscaled image
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}