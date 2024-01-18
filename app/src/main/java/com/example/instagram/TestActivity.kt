package com.example.instagram

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.instagram.databinding.ActivityTestBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

private const val TAG = "TestActivity_CommTag"

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private val vm: TestViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test)
        
        lifecycleScope.launch {
            val time = measureTimeMillis {
                producer()
                    .collect {
                        Log.e(TAG, it.toString())
                    }
            }
            Log.d(TAG, "Time = $time")
        }
        
        lifecycleScope.launch {
            val time = measureTimeMillis {
                delay(2000)
                producer()
                    .collect {
                        Log.d(TAG, it.toString())
                    }
            }
            Log.d(TAG, "Time = $time")
        }
        
    }
    
    private fun producer(): Flow<Int> {
        val mutableSharedFlow = MutableSharedFlow<Int>()
        val list = listOf(1, 2, 3, 4, 5)
        lifecycleScope.launch {
            list.forEach {
                delay(1000)
                mutableSharedFlow.emit(it)
            }
        }
        return mutableSharedFlow
    }
}