package com.example.instagram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.ActivityMain2Binding

private const val TAG = "MainActivity2_CommTag"

class MainActivity2 : AppCompatActivity() {
    private var _binding: ActivityMain2Binding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        db = AppDatabase.getDatabase(this)
    }
    
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
