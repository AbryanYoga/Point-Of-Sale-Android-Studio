package com.abryan.pointofsales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    lateinit var etNama: EditText
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    lateinit var etKonfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var tvGoLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_profile)

        auth     = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        etNama           = findViewById(R.id.etNama)
        etEmail          = findViewById(R.id.etEmail)
        etPassword       = findViewById(R.id.etPassword)
        etKonfirmPassword = findViewById(R.id.etKonfirmPassword)
        btnRegister      = findViewById(R.id.btnRegister)
        tvGoLogin        = findViewById(R.id.tvGoLogin)

        btnRegister.setOnClickListener {
            val nama     = etNama.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val konfirm  = etKonfirmPassword.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != konfirm) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    // Simpan nama ke Realtime Database: users/{uid}/nama
                    val userData = mapOf(
                        "nama"  to nama,
                        "email" to email,
                        "uid"   to uid
                    )

                    database.reference.child("users").child(uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvGoLogin.setOnClickListener {
            finish()
        }
    }
}