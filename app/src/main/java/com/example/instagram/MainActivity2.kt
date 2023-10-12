package com.example.instagram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.instagram.databinding.ActivityMain2Binding

private const val TAG = "MainActivity2_CommTag"

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        
    }
    
    
}
