package com.example.kotlinfirebasemessengertut

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        btRegisterRegister.setOnClickListener {
            performRegister()
        }

        twAlreadyHaveAccount.setOnClickListener {
            Log.d("RegisterActivity", "try to show login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btSelectPhoto.setOnClickListener {
            Log.d("RegisterActivity", "tr to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null)
        {
            // proceed and check what the selected image was...
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            //prikazuje sliku
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            iwSelectPhoto.setImageBitmap(bitmap)
            btSelectPhoto.alpha = 0f

        }
    }

    private fun performRegister(){
        val email = tbEmailRegister.text.toString()
        val password = tbPasswordRegister.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter the information needed", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is $email")
        Log.d("RegisterActivity", "Password is $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if succesfull
                Log.d("RegisterActivity", "Succesfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFirebaseStorage()
    {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener{
                    Log.d("RegisterActivity", "file location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String)
    {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, tbUsernameRegister.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //cisti activity stack
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "${it.message}")
            }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String)
{
    constructor() :this("", "", "")
}
