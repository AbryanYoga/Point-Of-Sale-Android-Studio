package com.abryan.pointofsales.Produk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelCabang
import com.abryan.pointofsales.model.ModelKategori
import com.abryan.pointofsales.model.ModelProduk
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class ModProduk : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")
    private val kategoriRef = database.getReference("kategori")
    private val cabangRef = database.getReference("cabang")

    private lateinit var cardBack: CardView
    private lateinit var etImageUrl: EditText
    private lateinit var btnPreviewGambar: Button
    private lateinit var imgPreview: ImageView
    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaProduk: EditText
    private lateinit var etStock: EditText
    private lateinit var cgKategori: com.google.android.material.chip.ChipGroup
    private lateinit var cgCabang: com.google.android.material.chip.ChipGroup
    private lateinit var tvEmptyKategori: TextView
    private lateinit var tvEmptyCabang: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var editProduk: ModelProduk? = null
    private var selectedKategori: String = ""
    private var selectedCabang: String = ""

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
        loadKategori()
        loadCabang()

        editProduk = intent.getSerializableExtra("produk") as? ModelProduk
        if (editProduk != null) {
            val data = editProduk!!
            val formatRupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))
            etImageUrl.setText(data.imageUrl)
            if (data.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(data.imageUrl)
                    .placeholder(R.drawable.produk)
                    .error(R.drawable.produk)
                    .into(imgPreview)
            }
            etNamaProduk.setText(data.nama)
            etHargaProduk.setText(formatRupiah.format(data.harga))
            etStock.setText(data.stok.toString())
            val statusList = arrayOf("-- Pilih Status --", "Aktif", "Non Aktif")
            spinnerStatus.setSelection(statusList.indexOf(data.status).takeIf { it >= 0 } ?: 0)
            btnHapus.visibility = View.VISIBLE
        } else {
            btnHapus.visibility = View.GONE
        }

        cgKategori.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedKategori = if (checkedIds.isNotEmpty()) {
                group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])?.text?.toString() ?: ""
            } else ""
        }

        cgCabang.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedCabang = if (checkedIds.isNotEmpty()) {
                group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])?.text?.toString() ?: ""
            } else ""
        }

        cardBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpan() }
        btnHapus.setOnClickListener { hapus() }

        btnPreviewGambar.setOnClickListener {
            val url = etImageUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Masukkan URL gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.produk)
                    .error(R.drawable.produk)
                    .into(imgPreview)
            }
        }
    }

    private fun buatChip(teks: String): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(this).apply {
            text = teks
            isCheckable = true
            isClickable = true
            id = View.generateViewId()
            chipBackgroundColor = android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    android.graphics.Color.parseColor("#4F46E5"),
                    android.graphics.Color.parseColor("#2A2A3E")
                )
            )
            setTextColor(
                android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        android.graphics.Color.WHITE,
                        android.graphics.Color.parseColor("#B0B0C0")
                    )
                )
            )
        }
    }

    private fun loadKategori() {
        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgKategori.removeAllViews()
                var hasData = false
                for (data in snapshot.children) {
                    val kategori = data.getValue(ModelKategori::class.java)
                    if (kategori != null && kategori.statusKategori == "Aktif") {
                        val name = kategori.namaKategori ?: continue
                        hasData = true
                        val chip = buatChip(name).apply {
                            if (editProduk != null && editProduk!!.jenis == name) {
                                isChecked = true
                                selectedKategori = name
                            }
                        }
                        cgKategori.addView(chip)
                    }
                }
                if (hasData) {
                    tvEmptyKategori.visibility = View.GONE
                    findViewById<View>(R.id.scrollJenis).visibility = View.VISIBLE
                } else {
                    tvEmptyKategori.visibility = View.VISIBLE
                    findViewById<View>(R.id.scrollJenis).visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModProduk, "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgCabang.removeAllViews()
                var hasData = false
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    if (cabang != null && cabang.statusCabang == "Aktif") {
                        val name = cabang.namaCabang ?: continue
                        hasData = true
                        val chip = buatChip(name).apply {
                            if (editProduk != null && editProduk!!.cabang == name) {
                                isChecked = true
                                selectedCabang = name
                            }
                        }
                        cgCabang.addView(chip)
                    }
                }
                if (hasData) {
                    tvEmptyCabang.visibility = View.GONE
                    findViewById<View>(R.id.scrollCabang).visibility = View.VISIBLE
                } else {
                    tvEmptyCabang.visibility = View.VISIBLE
                    findViewById<View>(R.id.scrollCabang).visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModProduk, "Gagal memuat cabang", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapus() {
        val idHapus = editProduk?.id ?: return
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                myRef.child(idHapus).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun init() {
        cardBack = findViewById(R.id.cardBack)
        etImageUrl = findViewById(R.id.etImageUrl)
        btnPreviewGambar = findViewById(R.id.btnPreviewGambar)
        imgPreview = findViewById(R.id.imgPreview)
        etNamaProduk = findViewById(R.id.etNamaKategori)
        etHargaProduk = findViewById(R.id.etHargaProduk)
        etStock = findViewById(R.id.etStock)
        cgKategori = findViewById(R.id.cgKategori)
        cgCabang = findViewById(R.id.cgCabang)
        tvEmptyKategori = findViewById(R.id.tvEmptyKategori)
        tvEmptyCabang = findViewById(R.id.tvEmptyCabang)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinner() {
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
        val imageUrl = etImageUrl.text.toString().trim()
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHargaProduk.text.toString().replace(".", "").toLongOrNull() ?: 0L
        val stok = etStock.text.toString().trim()
        val jenis = selectedKategori
        val cabang = selectedCabang
        val status = spinnerStatus.selectedItem?.toString() ?: ""

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
        if (jenis.isEmpty()) {
            Toast.makeText(this, "Pilih kategori produk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (cabang.isEmpty()) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (status == "-- Pilih Status --" || status.isEmpty()) {
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
                "status" to status,
                "imageUrl" to imageUrl
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
                "status" to status,
                "imageUrl" to imageUrl
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