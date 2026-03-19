package com.abryan.pointofsales.Kategori

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelKategori
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var cardBack: CardView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button

    private var editKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        init()
        setupSpinner()

        // Cek mode edit
        editKategori = intent.getParcelableExtra("kategori")
        editKategori?.let { data ->
            etNamaKategori.setText(data.namaKategori)
            val statusList = arrayOf("Aktif", "Non Aktif")
            val index = statusList.indexOf(data.statusKategori)
            if (index >= 0) spinnerStatus.setSelection(index)
        }

        cardBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            Log.d("DEBUG", "Tombol simpan ditekan")
            simpan()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun simpan() {
        val namaKategori = etNamaKategori.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        Log.d("DEBUG", "namaKategori: $namaKategori, status: $status")

        if (namaKategori.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            etNamaKategori.requestFocus()
            return
        }

        btnSimpan.isEnabled = false
        Log.d("DEBUG", "Mulai simpan ke Firebase...")

        if (editKategori != null) {
            // Mode EDIT
            val kategoriId = editKategori!!.idKategori ?: run {
                Log.e("DEBUG", "idKategori null!")
                btnSimpan.isEnabled = true
                return
            }

            val kategoriData = hashMapOf<String, Any>(
                "idKategori" to kategoriId,
                "namaKategori" to namaKategori,
                "statusKategori" to status
            )

            myRef.child(kategoriId).updateChildren(kategoriData)
                .addOnSuccessListener {
                    Log.d("DEBUG", "Update berhasil")
                    Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Log.e("DEBUG", "Update gagal: ${error.message}")
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            // Mode TAMBAH
            val kategoriBaru = myRef.push()
            val kategoriId = kategoriBaru.key ?: run {
                Log.e("DEBUG", "key null!")
                Toast.makeText(this, "Gagal generate ID, cek koneksi", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                return
            }

            Log.d("DEBUG", "kategoriId: $kategoriId")

            val kategoriData = hashMapOf<String, Any>(
                "idKategori" to kategoriId,
                "namaKategori" to namaKategori,
                "statusKategori" to status
            )

            kategoriBaru.setValue(kategoriData)
                .addOnSuccessListener {
                    Log.d("DEBUG", "Simpan berhasil")
                    Toast.makeText(this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Log.e("DEBUG", "Simpan gagal: ${error.message}")
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun init() {
        cardBack = findViewById(R.id.cardBack)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupSpinner() {
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }
}