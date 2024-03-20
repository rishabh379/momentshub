package com.pvsrishabh.momentshub.utils

import android.app.ProgressDialog
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

fun changeFollowersCount(num: Int, uid: String){

    val db = Firebase.firestore

    val docRef = db.collection(USER_NODE).document(uid)
    docRef.get().addOnSuccessListener { documentSnapshot ->
        if (documentSnapshot.exists()) {
            val currentFollowersCount = documentSnapshot.getLong("followersCount") ?: 0
            // Modify the followers count (for example, increment it by 1)
            var newFollowersCount = currentFollowersCount + num

            if(newFollowersCount<0){
                newFollowersCount = 0
            }

            val updates = hashMapOf<String, Any>(
                "followersCount" to newFollowersCount
            )
            docRef.update(updates)
        }
    }


}


fun changeFollowingCount(num: Int){

    val db = Firebase.firestore

    val docRef = db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
    docRef.get().addOnSuccessListener { documentSnapshot ->
        if (documentSnapshot.exists()) {
            val currentFollowingCount = documentSnapshot.getLong("followingCount") ?: 0
            // Modify the followers count (for example, increment it by 1)
            var newFollowingCount = currentFollowingCount + num

            if(newFollowingCount<0){
                newFollowingCount = 0
            }

            val updates = hashMapOf<String, Any>(
                "followingCount" to newFollowingCount
            )
            docRef.update(updates)
        }
    }


}


fun changePostCount(num: Int){

    val db = Firebase.firestore

    val docRef = db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
    docRef.get().addOnSuccessListener { documentSnapshot ->
        if (documentSnapshot.exists()) {
            val currentPostCount = documentSnapshot.getLong("postCount") ?: 0
            // Modify the followers count (for example, increment it by 1)
            var newPostCount = currentPostCount + num

            if(newPostCount<0){
                newPostCount = 0
            }

            val updates = hashMapOf<String, Any>(
                "postCount" to newPostCount
            )
            docRef.update(updates)
        }
    }


}


fun uploadImage(uri: Uri, folderName: String, progressDialog: ProgressDialog, callback:(String?) -> Unit) {
    var imageUrl: String? = null
    progressDialog.setTitle("Uploading . . .")
    progressDialog.show()
    FirebaseStorage.getInstance().getReference(folderName).child(UUID.randomUUID().toString())
        .putFile(uri)
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                imageUrl = it.toString()
                progressDialog.dismiss()
                callback(imageUrl)
            }
        }
        .addOnProgressListener {
            val uploadedValue: Long = (it.bytesTransferred / it.totalByteCount) * 100
            progressDialog.setMessage("Uploaded $uploadedValue %")
        }
}

fun uploadVideo(uri: Uri, folderName: String, progressDialog: ProgressDialog, callback:(String?) -> Unit){
    var videoUrl: String? = null
    progressDialog.setTitle("Uploading . . .")
    progressDialog.show()
    FirebaseStorage.getInstance().getReference(folderName).child(UUID.randomUUID().toString())
        .putFile(uri)
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                videoUrl = it.toString()
                progressDialog.dismiss()
                callback(videoUrl)
            }
        }
        .addOnProgressListener {
            val uploadedValue: Long = (it.bytesTransferred/it.totalByteCount) * 100
            progressDialog.setMessage("Uploaded $uploadedValue %")

        }
}