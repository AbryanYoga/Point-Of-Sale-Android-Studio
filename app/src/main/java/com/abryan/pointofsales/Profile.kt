package com.abryan.pointofsales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    lateinit var imgProfil: ImageView
    lateinit var tvNama: TextView
    lateinit var tvEmail: TextView
    lateinit var tvNamaDetail: TextView
    lateinit var tvEmailDetail: TextView
    lateinit var btnEditProfil: Button
    lateinit var btnLogin: Button
    lateinit var btnLogout: Button
    lateinit var btnBack: ImageView
    lateinit var btnEditFoto: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth     = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        init()

        val currentUser = auth.currentUser

        if (currentUser == null) {
            showGuestMode()
        } else {
            showLoginMode()
            loadUserData(currentUser.uid)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginProfile::class.java))
            finish()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnEditProfil.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        btnEditFoto.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }
    }

    fun init() {
        imgProfil     = findViewById(R.id.imgProfil)
        tvNama        = findViewById(R.id.tvNama)
        tvEmail       = findViewById(R.id.tvEmail)
        tvNamaDetail  = findViewById(R.id.tvNamaDetail)
        tvEmailDetail = findViewById(R.id.tvEmailDetail)
        btnEditProfil = findViewById(R.id.btnEditProfil)
        btnLogin      = findViewById(R.id.btnLogin)
        btnLogout     = findViewById(R.id.btnLogout)
        btnBack       = findViewById(R.id.btnBack)
        btnEditFoto   = findViewById(R.id.btnEditFoto)
    }

    private fun showGuestMode() {
        tvNama.text        = "Guest"
        tvEmail.text       = "-"
        tvNamaDetail.text  = "Guest"
        tvEmailDetail.text = "-"
        btnEditProfil.visibility = View.GONE
        btnEditFoto.visibility   = View.GONE
        btnLogout.visibility     = View.GONE
        btnLogin.visibility      = View.VISIBLE
    }

    private fun showLoginMode() {
        btnLogin.visibility      = View.GONE
        btnEditProfil.visibility = View.VISIBLE
        btnEditFoto.visibility   = View.VISIBLE
        btnLogout.visibility     = View.VISIBLE
    }

    private fun loadUserData(uid: String) {
        database.reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama    = snapshot.child("nama").getValue(String::class.java) ?: "User"
                    val email   = snapshot.child("email").getValue(String::class.java) ?: "-"
                    val fotoUrl = snapshot.child("fotoProfil").getValue(String::class.java)

                    tvNama.text        = nama
                    tvEmail.text       = email
                    tvNamaDetail.text  = nama
                    tvEmailDetail.text = email

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this@Profile)
                            .load(fotoUrl)
                            .placeholder(R.drawable.profil)
                            .error(R.drawable.profil)
                            .circleCrop()
                            .into(imgProfil)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserData(currentUser.uid)
        }
    }
}