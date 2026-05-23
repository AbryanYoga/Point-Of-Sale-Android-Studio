package com.abryan.pointofsales

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var imgPreview: ImageView
    private lateinit var etFotoUrl: EditText
    private lateinit var etNama: EditText
    private lateinit var btnPreview: Button
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        init()
        setupListeners()
        loadCurrentData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        imgPreview = findViewById(R.id.imgPreview)
        etFotoUrl  = findViewById(R.id.etFotoUrl)
        etNama     = findViewById(R.id.etNama)
        btnPreview = findViewById(R.id.btnPreview)
        btnSimpan  = findViewById(R.id.btnSimpan)
        btnBack    = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPreview.setOnClickListener {
            val url = etFotoUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Masukkan URL foto terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.profil)
                .error(R.drawable.profil)
                .circleCrop()
                .into(imgPreview)
        }

        btnSimpan.setOnClickListener {
            simpanProfil()
        }
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return

        database.reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama    = snapshot.child("nama").getValue(String::class.java) ?: ""
                    val fotoUrl = snapshot.child("fotoProfil").getValue(String::class.java) ?: ""

                    etNama.setText(nama)
                    etFotoUrl.setText(fotoUrl)

                    if (fotoUrl.isNotEmpty()) {
                        Glide.with(this@EditProfile)
                            .load(fotoUrl)
                            .placeholder(R.drawable.profil)
                            .error(R.drawable.profil)
                            .circleCrop()
                            .into(imgPreview)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditProfile, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun simpanProfil() {
        val uid  = auth.currentUser?.uid ?: return
        val nama = etNama.text.toString().trim()
        val url  = etFotoUrl.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            etNama.requestFocus()
            return
        }

        btnSimpan.isEnabled = false

        val updates = mutableMapOf<String, Any>("nama" to nama)
        // Jika url kosong, kita simpan string kosong agar terhapus
        updates["fotoProfil"] = url

        database.reference.child("users").child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                btnSimpan.isEnabled = true
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}