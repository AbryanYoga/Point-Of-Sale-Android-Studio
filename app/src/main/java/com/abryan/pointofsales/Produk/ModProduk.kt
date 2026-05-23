package com.abryan.pointofsales.Produk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.abryan.pointofsales.model.ModelProduk
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class ModProduk : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")

    private lateinit var cardBack: CardView
    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaProduk: EditText
    private lateinit var etStock: EditText
    private lateinit var spinnerJenis: Spinner
    private lateinit var spinnerCabang: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var editProduk: ModelProduk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_produk)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupSpinner()
        setupFormatHarga()

        editProduk = intent.getSerializableExtra("produk") as? ModelProduk
        if (editProduk != null) {
            val data = editProduk!!
            val formatRupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))
            etNamaProduk.setText(data.nama)
            etHargaProduk.setText(formatRupiah.format(data.harga))
            etStock.setText(data.stok.toString())

            val jenisList = resources.getStringArray(R.array.SpinnerJenisProduk)
            spinnerJenis.setSelection(jenisList.indexOf(data.jenis).takeIf { it >= 0 } ?: 0)

            val cabangList = resources.getStringArray(R.array.SpinnerCabang)
            spinnerCabang.setSelection(cabangList.indexOf(data.cabang).takeIf { it >= 0 } ?: 0)

            val statusList = arrayOf("-- Pilih Status --", "Aktif", "Non Aktif")
            spinnerStatus.setSelection(statusList.indexOf(data.status).takeIf { it >= 0 } ?: 0)
            
            btnHapus.visibility = View.VISIBLE
        } else {
            btnHapus.visibility = View.GONE
        }

        cardBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpan() }
        btnHapus.setOnClickListener { hapus() }
    }
    
    private fun hapus() {
        val idHapus = editProduk?.id ?: return
        
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

    private fun init() {
        cardBack = findViewById(R.id.cardBack)
        etNamaProduk = findViewById(R.id.etNamaKategori)
        etHargaProduk = findViewById(R.id.etHargaProduk)
        etStock = findViewById(R.id.etStock)
        spinnerJenis = findViewById(R.id.spinnerJenis)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinner() {
        val jenisItems = resources.getStringArray(R.array.SpinnerJenisProduk)
        val jenisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jenisItems)
        jenisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenis.adapter = jenisAdapter
        spinnerJenis.setSelection(0)

        val cabangItems = resources.getStringArray(R.array.SpinnerCabang)
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cabangItems)
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = cabangAdapter
        spinnerCabang.setSelection(0)

        val statusItems = arrayOf("-- Pilih Status --", "Aktif", "Non Aktif")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
        spinnerStatus.setSelection(0)
    }

    private fun setupFormatHarga() {
        etHargaProduk.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace(".", "").replace(",", "")
                if (input.isNotEmpty()) {
                    val number = input.toLongOrNull() ?: 0L
                    val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
                    etHargaProduk.setText(formatted)
                    etHargaProduk.setSelection(formatted.length)
                }
                isEditing = false
            }
        })
    }

    private fun simpan() {
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHargaProduk.text.toString().replace(".", "").toLongOrNull() ?: 0L
        val stok = etStock.text.toString().trim()
        val jenis = spinnerJenis.selectedItem.toString()
        val cabang = spinnerCabang.selectedItem.toString()
        val status = spinnerStatus.selectedItem.toString()

        if (nama.isEmpty()) {
            etNamaProduk.error = "Nama produk tidak boleh kosong"
            etNamaProduk.requestFocus()
            return
        }
        if (harga == 0L) {
            etHargaProduk.error = "Harga tidak boleh kosong"
            etHargaProduk.requestFocus()
            return
        }
        if (stok.isEmpty()) {
            etStock.error = "Stok tidak boleh kosong"
            etStock.requestFocus()
            return
        }
        if (jenis == "Pilih Jenis Produk") {
            Toast.makeText(this, "Pilih jenis produk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (cabang == "Pilih Cabang") {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (status == "-- Pilih Status --") {
            Toast.makeText(this, "Pilih status terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        btnSimpan.isEnabled = false

        if (editProduk != null) {
            val produkId = editProduk!!.id
            val produkData = hashMapOf<String, Any>(
                "id" to produkId,
                "nama" to nama,
                "harga" to harga,
                "jenis" to jenis,
                "stok" to stok.toInt(),
                "cabang" to cabang,
                "status" to status
            )
            myRef.child(produkId).updateChildren(produkData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val produkBaru = myRef.push()
            val produkId = produkBaru.key ?: run {
                Toast.makeText(this, "Gagal generate ID", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                return
            }
            val produkData = hashMapOf<String, Any>(
                "id" to produkId,
                "nama" to nama,
                "harga" to harga,
                "jenis" to jenis,
                "stok" to stok.toInt(),
                "cabang" to cabang,
                "status" to status
            )
            produkBaru.setValue(produkData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSimpan.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}