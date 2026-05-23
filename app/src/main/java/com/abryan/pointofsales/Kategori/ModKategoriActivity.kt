package com.abryan.pointofsales.Kategori

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private lateinit var btnHapus: Button

    private var editKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        init()
        setupSpinner()

        editKategori = intent.getParcelableExtra("kategori")
        if (editKategori != null) {
            val data = editKategori!!
            etNamaKategori.setText(data.namaKategori)
            val statusList = arrayOf("Aktif", "Non Aktif")
            val index = statusList.indexOf(data.statusKategori)
            if (index >= 0) spinnerStatus.setSelection(index)
            
            btnHapus.visibility = View.VISIBLE
        } else {
            btnHapus.visibility = View.GONE
        }

        cardBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpan()
        }
        
        btnHapus.setOnClickListener {
            hapus()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun hapus() {
        val idHapus = editKategori?.idKategori ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                myRef.child(idHapus).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun simpan() {
        val namaKategori = etNamaKategori.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (namaKategori.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            etNamaKategori.requestFocus()
            return
        }

        btnSimpan.isEnabled = false

        if (editKategori != null) {
            val kategoriId = editKategori!!.idKategori ?: run {
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
                    Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            val kategoriBaru = myRef.push()
            val kategoriId = kategoriBaru.key ?: run {
                Toast.makeText(this, "Gagal generate ID", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                return
            }

            val kategoriData = hashMapOf<String, Any>(
                "idKategori" to kategoriId,
                "namaKategori" to namaKategori,
                "statusKategori" to status
            )

            kategoriBaru.setValue(kategoriData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
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
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinner() {
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }
}