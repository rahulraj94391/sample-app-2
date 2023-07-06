package com.example.instagram


/*
private fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>) {
    binding.progressBar.visibility = View.VISIBLE
    Log.d(TAG, "Uri Length = ${listOfImageUris.size}")
    for (i in listOfImageUris.indices) {
        val storageRef = storageRef.reference.child("${postId}_$i")
        val imageUri = listOfImageUris[i]
        imageUri.let { uri ->
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[postId.toString()] = uri2.toString()

                        firebaseFirestore.collection("postImages").add(map).addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(this, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                            }

                            // attach placeholder image when unsuccessful while adding path to firestore(DB)
                            binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                            binding.progressBar.visibility = View.INVISIBLE

                        }
                    }
                }
                else {
                    // when image upload to storage(drive like) fails
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}*/

/*private fun uploadProfileImage(profileId: Long, profilePicUri: Uri?) {
    binding.progressBar.visibility = View.VISIBLE
    val storageRef = storageRef.reference.child(profileId.toString())
    profilePicUri?.let { uri ->
        storageRef.putFile(uri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                    val map = HashMap<String, Any>()
                    map[profileId.toString()] = uri2.toString()
                    firebaseFirestore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
                        if (firestoreTask.isSuccessful) {
                            Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                        }

                        // attach placeholder image when unsuccessful while adding path to firestore(DB)
                        binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                        binding.progressBar.visibility = View.INVISIBLE

                    }
                }
            }
            else {
                // when image upload to storage(drive like) fails
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }
}*/



//resultLauncher.launch("image/*")
//private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
//    profilePicUri = it
//    binding.imageView.setImageURI(it)
//}
//
//
//resultLauncherForPost.launch("image/*")
//private val resultLauncherForPost = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
//    postPicUris = it
//}

//private suspend fun getPostImages(postId: Long): MutableList<String> {
//    val imgURLList = mutableListOf<String>()
//    val snapShot = firebaseFireStore.collection("postImages").get().await()
//    for (i in snapShot) {
//        imgURLList.add(i.data["$postId"].toString())
//    }
//    return imgURLList
//}
//
//private suspend fun getProfileImage(profileId: Long): MutableList<String> {
//    val imgURLList = mutableListOf<String>()
//    val snapShot = firebaseFireStore.collection("profileImages").get().await()
//    for (i in snapShot) {
//        imgURLList.add(i.data["$profileId"].toString())
//    }
//    return imgURLList
//}


//lifecycleScope.launch {
//    val list = CoroutineScope(Dispatchers.IO).async {
//        getPostImages(93384)
//    }
//
//    val list2 = CoroutineScope(Dispatchers.IO).async {
//        getProfileImage(12234)
//    }
//
//    val finalList = list.await()
//    Log.d(TAG, "postPic = $finalList")
//
//    val finalList2 = list2.await()
//    Log.d(TAG, "profilePic = $finalList2")
//
//
//}