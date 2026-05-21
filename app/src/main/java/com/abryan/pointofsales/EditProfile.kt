package com.abryan.pointofsales

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    lateinit var imgPreview: ImageView
    lateinit var etFotoUrl: EditText
    lateinit var etNama: EditText
    lateinit var btnPreview: Button
    lateinit var btnSimpan: Button
    lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth     = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        imgPreview = findViewById(R.id.imgPreview)
        etFotoUrl  = findViewById(R.id.etFotoUrl)
        etNama     = findViewById(R.id.etNama)
        btnPreview = findViewById(R.id.btnPreview)
        btnSimpan  = findViewById(R.id.btnSimpan)
        btnBack    = findViewById(R.id.btnBack)

        loadCurrentData()

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
            val uid  = auth.currentUser?.uid ?: return@setOnClickListener
            val nama = etNama.text.toString().trim()
            val url  = etFotoUrl.text.toString().trim()

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mutableMapOf<String, Any>("nama" to nama)
            if (url.isNotEmpty()) updates["fotoProfil"] = url

            database.reference.child("users").child(uid)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show()
                }
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
                            .circleCrop()
                            .into(imgPreview)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}