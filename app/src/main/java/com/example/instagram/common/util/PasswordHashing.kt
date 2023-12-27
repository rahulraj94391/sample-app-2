package com.example.instagram.common.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object PasswordHashing {
    private fun generateSalt(): String {
        // Generate a random salt (you can customize the length)
        val secureRandom = SecureRandom()
        val salt = ByteArray(16) // 16 bytes is a good length for salts
        secureRandom.nextBytes(salt)
        
        // Convert the salt bytes to a hexadecimal string
        val saltString = StringBuilder()
        for (saltByte in salt) {
            val hex = Integer.toHexString(0xff and saltByte.toInt())
            if (hex.length == 1) {
                saltString.append('0')
            }
            saltString.append(hex)
        }
        
        return saltString.toString()
    }
    
    fun generateSHA256Hash(input: String): String? {
//        849f1575ccfbf3a4d6cf00e6c5641b7fd4da2ed3e212c2d79ba9161a5a432ff0
        return try {
            // Create a MessageDigest instance for SHA-256
            val digest = MessageDigest.getInstance("SHA-256")
            
            // Update the digest with the input bytes
            val hashBytes = digest.digest(input.toByteArray())
            
            // Convert the byte array to a hexadecimal string
            val hexString = StringBuilder()
            for (hashByte in hashBytes) {
                val hex = Integer.toHexString(0xff and hashByte.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            
            hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            // Handle the exception
            e.printStackTrace()
            null
        }
    }
}