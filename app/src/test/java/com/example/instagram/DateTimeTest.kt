package com.example.instagram

import org.junit.jupiter.api.Test

private const val ONE_DAY = 24 * 60 * 60 * 1000

class DateTimeTest {
    
    @Test
    fun timeFormatter() {
        val `0days back` = System.currentTimeMillis() - 0 * ONE_DAY
        val `1days back` = System.currentTimeMillis() - 1 * ONE_DAY
        val `2days back` = System.currentTimeMillis() - 2 * ONE_DAY
        val `3days back` = System.currentTimeMillis() - 3 * ONE_DAY
        val `4days back` = System.currentTimeMillis() - 4 * ONE_DAY
        val `5days back` = System.currentTimeMillis() - 5 * ONE_DAY
        val `6days back` = System.currentTimeMillis() - 6 * ONE_DAY
        val `7days back` = System.currentTimeMillis() - 7 * ONE_DAY
        val `8days back` = System.currentTimeMillis() - 8 * ONE_DAY
        
//        assertThat()
        
        
    }
    
    
}