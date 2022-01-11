package com.example.android.messenger

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlin.coroutines.Continuation


class SignUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val database by lazy{
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImgView.setOnClickListener {
            checkPermissionForImage()
        }

        nextBtn.setOnClickListener {
            val name = nameEt.text.toString()
            if (!::downloadUrl.isInitialized) {
                Toast.makeText(this,"Photo cannot be empty",Toast.LENGTH_SHORT).show()
            } else if (name.isEmpty()) {
                Toast.makeText(this,"Name cannot be empty",Toast.LENGTH_SHORT).show()
            } else {
                val user = User(name, downloadUrl, downloadUrl/*Needs to thumbnai url*/, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    nextBtn.isEnabled = true
                }
            }
        }

    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        //We will use Implicit Intent here
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==1000){
            if (data != null) {
                data.data.let {
                    userImgView.setImageURI(it)
                    if (it != null) {
                        uploadImage(it)
                    }
                }
            }
        }
    }

    private fun uploadImage(it: Uri) {
        nextBtn.isEnabled=false

        val ref = storage.reference.child("uploads/"+auth.uid.toString())
        var uploadTask = it.let { it1 -> ref.putFile(it1) }

        uploadTask.continueWithTask Continuation@{ task ->
                if (!task.isSuccessful){
                    task.exception.let {
                        throw task.exception!!
                    }
                }
                return@Continuation ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUrl = task.result.toString()
                    nextBtn.isEnabled = true
                    Log.i("Url","downloadUrl: $downloadUrl")
                } else {
                    // Handle failures
                    // ...
                    nextBtn.isEnabled = true
                }
            }

    }

}