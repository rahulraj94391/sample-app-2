package com.example.instagram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.adapters.TestAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.ActivityMain2Binding

private const val TAG = "MainActivity2_CommTag"

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TestAdapter
    private val urlOfImages: MutableList<String> = mutableListOf()
    
    init {
        urlOfImages.apply {
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_1?alt=media&token=5a1023b9-e0eb-4501-afa7-5093a03feb45")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_0?alt=media&token=a20af5ca-a74e-44f6-a573-958ad7646117")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_1?alt=media&token=755192f1-cf8f-4315-81e5-7a76ccfae377")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696495900505_0?alt=media&token=d11bfa7f-0ca2-46bb-ba05-a504fb38867a")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696565384992_0?alt=media&token=9f0ed31d-375d-4ab5-976c-5da4599bf5e2")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_1?alt=media&token=5a1023b9-e0eb-4501-afa7-5093a03feb45")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_0?alt=media&token=a20af5ca-a74e-44f6-a573-958ad7646117")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_1?alt=media&token=755192f1-cf8f-4315-81e5-7a76ccfae377")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696495900505_0?alt=media&token=d11bfa7f-0ca2-46bb-ba05-a504fb38867a")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696565384992_0?alt=media&token=9f0ed31d-375d-4ab5-976c-5da4599bf5e2")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_1?alt=media&token=5a1023b9-e0eb-4501-afa7-5093a03feb45")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_0?alt=media&token=a20af5ca-a74e-44f6-a573-958ad7646117")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_1?alt=media&token=755192f1-cf8f-4315-81e5-7a76ccfae377")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696495900505_0?alt=media&token=d11bfa7f-0ca2-46bb-ba05-a504fb38867a")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696565384992_0?alt=media&token=9f0ed31d-375d-4ab5-976c-5da4599bf5e2")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_1?alt=media&token=5a1023b9-e0eb-4501-afa7-5093a03feb45")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_0?alt=media&token=a20af5ca-a74e-44f6-a573-958ad7646117")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_1?alt=media&token=755192f1-cf8f-4315-81e5-7a76ccfae377")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696495900505_0?alt=media&token=d11bfa7f-0ca2-46bb-ba05-a504fb38867a")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696565384992_0?alt=media&token=9f0ed31d-375d-4ab5-976c-5da4599bf5e2")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_1?alt=media&token=5a1023b9-e0eb-4501-afa7-5093a03feb45")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_0?alt=media&token=a20af5ca-a74e-44f6-a573-958ad7646117")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696494893396_1?alt=media&token=755192f1-cf8f-4315-81e5-7a76ccfae377")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696495900505_0?alt=media&token=d11bfa7f-0ca2-46bb-ba05-a504fb38867a")
            add("https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696565384992_0?alt=media&token=9f0ed31d-375d-4ab5-976c-5da4599bf5e2")
            
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        
        adapter = TestAdapter(urlOfImages)
        binding.testScroll.adapter = adapter
        binding.testScroll.layoutManager = LinearLayoutManager(this)
        
    }
    
    
}
