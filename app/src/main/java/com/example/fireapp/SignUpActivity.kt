package com.example.fireapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val nameField = findViewById<EditText>(R.id.etFullName)
        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        signUpButton.setOnClickListener {

            val fullName = nameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CREATE USER IN FIREBASE AUTHENTICATION
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val uid = auth.currentUser?.uid ?: ""

                        // DATA MAP FOR REALTIME DATABASE
                        val userData = mapOf(
                            "uid" to uid,
                            "fullName" to fullName,
                            "email" to email,
                            "password" to password,
                            "createdAt" to System.currentTimeMillis()
                        )

                        // SAVE USER IN REALTIME DATABASE
                        FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child(uid)
                            .setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Database Error!", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}

