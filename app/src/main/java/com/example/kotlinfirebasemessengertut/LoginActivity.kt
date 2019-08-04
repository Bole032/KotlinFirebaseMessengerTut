package com.example.kotlinfirebasemessengertut

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        btLogin.setOnClickListener {
            val email = tbEmailLogin.text.toString()
            val password = tbPasswordLogin.text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter the information needed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Login", "Attempt to login with email/pw: $email/****")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    //else if successful;
                    Log.d("Main", "Successfully logged in user with uid: ${it.result?.user?.uid}")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to login user: ${it.message}")
                    Toast.makeText(this, "Failed to login user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        twBackToRegister.setOnClickListener {
            finish()
        }
    }
}