package com.example.fireapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fireapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().reference.child("Users")

        // ----------- FIND VIEWS -------------
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvProfileName = findViewById(R.id.tvProfileName)
        imgProfile = findViewById(R.id.imgProfile)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)

        loadUserData()

        // ----------- EDIT PROFILE BUTTON -------------
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }


        // ----------- LOGOUT BUTTON -------------
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ------------------------------------------------------------------
    // 🔥 LOAD USER DATA FROM FIREBASE
    // ------------------------------------------------------------------
    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid = user.uid

        database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val email = snapshot.child("email").getValue(String::class.java) ?: user.email
                val phone = snapshot.child("phone").getValue(String::class.java) ?: "Not Set"

                // Set UI Text
                tvName.text = "Name: $name"
                tvEmail.text = "Email: $email"
                tvPhone.text = "Phone: $phone"

                tvProfileName.text = name
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

