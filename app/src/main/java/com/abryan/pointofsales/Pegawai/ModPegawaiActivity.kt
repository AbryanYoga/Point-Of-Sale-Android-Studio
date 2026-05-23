package com.abryan.pointofsales.Pegawai

import android.os.Bundle
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
import com.abryan.pointofsales.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ModPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val pegawaiRef = database.getReference("pegawai")
    private val cabangRef = database.getReference("cabang")

    private lateinit var cardBack: CardView
    private lateinit var etNamaPegawai: EditText
    private lateinit var spinnerJenisKelamin: Spinner
    private lateinit var spinnerCabang: Spinner
    private lateinit var etNomorHp: EditText
    private lateinit var etAlamatPegawai: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var editPegawai: ModelPegawai? = null
    private var listCabangStr: ArrayList<String> = ArrayList()
    private lateinit var cabangAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pegawai)

        init()
        setupSpinners()
        loadCabang()

        editPegawai = intent.getParcelableExtra("pegawai")
        if (editPegawai != null) {
            val data = editPegawai!!
            etNamaPegawai.setText(data.namaPegawai)
            etNomorHp.setText(data.nomorHp)
            etAlamatPegawai.setText(data.alamatPegawai)
            
            val jkList = arrayOf("Laki-laki", "Perempuan")
            val jkIndex = jkList.indexOf(data.jenisKelamin)
            if (jkIndex >= 0) spinnerJenisKelamin.setSelection(jkIndex)
            
            val statusList = arrayOf("Aktif", "Non Aktif")
            val statusIndex = statusList.indexOf(data.statusPegawai)
            if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)
            
            btnHapus.visibility = View.VISIBLE
        } else {
            btnHapus.visibility = View.GONE
        }

        cardBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpan() }
        btnHapus.setOnClickListener { hapus() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun hapus() {
        val idHapus = editPegawai?.idPegawai ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                pegawaiRef.child(idHapus).removeValue()
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

    private fun loadCabang() {
        cabangRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabangStr.clear()
                for (data in snapshot.children) {
                    val status = data.child("statusCabang").getValue(String::class.java)
                    val nama = data.child("namaCabang").getValue(String::class.java)
                    if (status == "Aktif" && nama != null) {
                        listCabangStr.add(nama)
                    }
                }
                
                editPegawai?.let { data ->
                    val cabangTersimpan = data.cabangPegawai
                    if (!listCabangStr.contains(cabangTersimpan.toString()) && cabangTersimpan != null) {
                        listCabangStr.add(cabangTersimpan)
                    }
                }

                cabangAdapter.notifyDataSetChanged()

                editPegawai?.let { data ->
                    val cabangIndex = listCabangStr.indexOf(data.cabangPegawai)
                    if (cabangIndex >= 0) spinnerCabang.setSelection(cabangIndex)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModPegawaiActivity, "Gagal memuat cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun simpan() {
        val namaPegawai = etNamaPegawai.text.toString().trim()
        val jk = spinnerJenisKelamin.selectedItem?.toString() ?: ""
        val cabang = spinnerCabang.selectedItem?.toString() ?: ""
        val nomorHp = etNomorHp.text.toString().trim()
        val alamat = etAlamatPegawai.text.toString().trim()
        val status = spinnerStatus.selectedItem?.toString() ?: ""

        if (namaPegawai.isEmpty()) {
            etNamaPegawai.error = "Nama tidak boleh kosong"
            etNamaPegawai.requestFocus()
            return
        }
        if (nomorHp.isEmpty()) {
            etNomorHp.error = "Nomor HP tidak boleh kosong"
            etNomorHp.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamatPegawai.error = "Alamat tidak boleh kosong"
            etAlamatPegawai.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            Toast.makeText(this, "Silakan pilih cabang", Toast.LENGTH_SHORT).show()
            return
        }

        btnSimpan.isEnabled = false

        if (editPegawai != null) {
            val pegawaiId = editPegawai!!.idPegawai ?: run {
                btnSimpan.isEnabled = true
                return
            }

            val pegawaiData = hashMapOf<String, Any>(
                "idPegawai" to pegawaiId,
                "namaPegawai" to namaPegawai,
                "jenisKelamin" to jk,
                "alamatPegawai" to alamat,
                "cabangPegawai" to cabang,
                "nomorHp" to nomorHp,
                "statusPegawai" to status
            )

            pegawaiRef.child(pegawaiId).updateChildren(pegawaiData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            val pegawaiBaru = pegawaiRef.push()
            val pegawaiId = pegawaiBaru.key ?: run {
                Toast.makeText(this, "Gagal generate ID", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                return
            }

            val pegawaiData = hashMapOf<String, Any>(
                "idPegawai" to pegawaiId,
                "namaPegawai" to namaPegawai,
                "jenisKelamin" to jk,
                "alamatPegawai" to alamat,
                "cabangPegawai" to cabang,
                "nomorHp" to nomorHp,
                "statusPegawai" to status
            )

            pegawaiBaru.setValue(pegawaiData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pegawai berhasil disimpan", Toast.LENGTH_SHORT).show()
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
        etNamaPegawai = findViewById(R.id.etNamaPegawai)
        spinnerJenisKelamin = findViewById(R.id.spinnerJenisKelamin)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        etNomorHp = findViewById(R.id.etNomorHp)
        etAlamatPegawai = findViewById(R.id.etAlamatPegawai)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinners() {
        val jkItems = arrayOf("Laki-laki", "Perempuan")
        val jkAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jkItems)
        jkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenisKelamin.adapter = jkAdapter

        val statusItems = arrayOf("Aktif", "Non Aktif")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listCabangStr)
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = cabangAdapter
    }
}
