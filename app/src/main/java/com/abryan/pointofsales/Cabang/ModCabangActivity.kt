package com.abryan.pointofsales.Cabang

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
import com.abryan.pointofsales.model.ModelCabang
import com.google.firebase.database.FirebaseDatabase

class ModCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var cardBack: CardView
    private lateinit var etNamaCabang: EditText
    private lateinit var etKodeCabang: EditText
    private lateinit var etPenanggungJawab: EditText
    private lateinit var etNomorCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button

    private var editCabang: ModelCabang? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)

        init()
        setupSpinner()

        // Cek mode edit
        editCabang = intent.getParcelableExtra("cabang")
        editCabang?.let { data ->
            etNamaCabang.setText(data.namaCabang)
            etKodeCabang.setText(data.kodeCabang)
            etPenanggungJawab.setText(data.penanggungJawab)
            etNomorCabang.setText(data.nomorCabang)
            etAlamatCabang.setText(data.alamatCabang)
            val statusList = arrayOf("Aktif", "Non Aktif")
            val index = statusList.indexOf(data.statusCabang)
            if (index >= 0) spinnerStatus.setSelection(index)
        }

        cardBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpan()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun simpan() {
        val namaCabang = etNamaCabang.text.toString().trim()
        val kodeCabang = etKodeCabang.text.toString().trim()
        val penanggungJawab = etPenanggungJawab.text.toString().trim()
        val nomorCabang = etNomorCabang.text.toString().trim()
        val alamatCabang = etAlamatCabang.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            etNamaCabang.requestFocus()
            return
        }
        if (kodeCabang.isEmpty()) {
            etKodeCabang.error = "Kode cabang tidak boleh kosong"
            etKodeCabang.requestFocus()
            return
        }
        if (penanggungJawab.isEmpty()) {
            etPenanggungJawab.error = "Penanggung jawab tidak boleh kosong"
            etPenanggungJawab.requestFocus()
            return
        }
        if (nomorCabang.isEmpty()) {
            etNomorCabang.error = "Nomor cabang tidak boleh kosong"
            etNomorCabang.requestFocus()
            return
        }
        if (alamatCabang.isEmpty()) {
            etAlamatCabang.error = "Alamat cabang tidak boleh kosong"
            etAlamatCabang.requestFocus()
            return
        }

        btnSimpan.isEnabled = false

        if (editCabang != null) {
            // Mode EDIT
            val cabangId = editCabang!!.idCabang ?: run {
                btnSimpan.isEnabled = true
                return
            }

            val cabangData = hashMapOf<String, Any>(
                "idCabang" to cabangId,
                "namaCabang" to namaCabang,
                "kodeCabang" to kodeCabang,
                "penanggungJawab" to penanggungJawab,
                "nomorCabang" to nomorCabang,
                "alamatCabang" to alamatCabang,
                "statusCabang" to status
            )

            myRef.child(cabangId).updateChildren(cabangData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cabang berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            // Mode TAMBAH
            val cabangBaru = myRef.push()
            val cabangId = cabangBaru.key ?: run {
                Toast.makeText(this, "Gagal generate ID, cek koneksi", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                return
            }

            val cabangData = hashMapOf<String, Any>(
                "idCabang" to cabangId,
                "namaCabang" to namaCabang,
                "kodeCabang" to kodeCabang,
                "penanggungJawab" to penanggungJawab,
                "nomorCabang" to nomorCabang,
                "alamatCabang" to alamatCabang,
                "statusCabang" to status
            )

            cabangBaru.setValue(cabangData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cabang berhasil disimpan", Toast.LENGTH_SHORT).show()
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
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etKodeCabang = findViewById(R.id.etKodeCabang)
        etPenanggungJawab = findViewById(R.id.etPenanggungJawab)
        etNomorCabang = findViewById(R.id.etNomorCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
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
