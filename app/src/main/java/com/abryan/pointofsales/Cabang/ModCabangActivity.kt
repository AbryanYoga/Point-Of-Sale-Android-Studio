package com.abryan.pointofsales.Cabang

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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
import com.abryan.pointofsales.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ModCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")
    private val pegawaiRef = database.getReference("pegawai")

    private lateinit var cardBack: CardView
    private lateinit var etNamaCabang: EditText
    private lateinit var etKodeCabang: EditText
    private lateinit var cgPJ: com.google.android.material.chip.ChipGroup
    private lateinit var tvEmptyPJ: TextView
    private lateinit var etNomorCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var editCabang: ModelCabang? = null
    private var selectedPenanggungJawab: String = ""
    private var selectedNomorCabang: String = ""
    private val activePegawaiList = ArrayList<ModelPegawai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)

        init()
        setupSpinner()
        loadPegawai()

        editCabang = intent.getParcelableExtra("cabang")
        if (editCabang != null) {
            val data = editCabang!!
            etNamaCabang.setText(data.namaCabang)
            etKodeCabang.setText(data.kodeCabang)
            selectedPenanggungJawab = data.penanggungJawab ?: ""
            etNomorCabang.setText(data.nomorCabang)
            etAlamatCabang.setText(data.alamatCabang)
            val statusList = arrayOf("Aktif", "Non Aktif")
            val index = statusList.indexOf(data.statusCabang)
            if (index >= 0) spinnerStatus.setSelection(index)
            btnHapus.visibility = View.VISIBLE
        } else {
            btnHapus.visibility = View.GONE
        }

        cgPJ.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                selectedPenanggungJawab = chip?.text?.toString() ?: ""
                val matchedPegawai = activePegawaiList.find { it.namaPegawai == selectedPenanggungJawab }
                if (matchedPegawai != null) {
                    selectedNomorCabang = matchedPegawai.nomorHp ?: ""
                    etNomorCabang.setText(selectedNomorCabang)
                }
            } else {
                selectedPenanggungJawab = ""
                selectedNomorCabang = ""
                etNomorCabang.setText("")
            }
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

    private fun loadPegawai() {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgPJ.removeAllViews()
                activePegawaiList.clear()
                var hasData = false

                for (data in snapshot.children) {
                    val pegawai = data.getValue(ModelPegawai::class.java)
                    if (pegawai != null && pegawai.statusPegawai == "Aktif") {
                        val name = pegawai.namaPegawai ?: continue
                        hasData = true
                        activePegawaiList.add(pegawai)

                        val chip = buatChip(name).apply {
                            if (selectedPenanggungJawab == name) {
                                isChecked = true
                            }
                        }
                        cgPJ.addView(chip)
                    }
                }

                if (hasData) {
                    tvEmptyPJ.visibility = View.GONE
                    findViewById<View>(R.id.scrollPenanggungJawab).visibility = View.VISIBLE
                } else {
                    tvEmptyPJ.visibility = View.VISIBLE
                    findViewById<View>(R.id.scrollPenanggungJawab).visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModCabangActivity, "Gagal memuat pegawai", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapus() {
        val idHapus = editCabang?.idCabang ?: return
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

    private fun simpan() {
        val namaCabang = etNamaCabang.text.toString().trim()
        val kodeCabang = etKodeCabang.text.toString().trim()
        val penanggungJawab = selectedPenanggungJawab
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
            Toast.makeText(this, "Pilih penanggung jawab terlebih dahulu", Toast.LENGTH_SHORT).show()
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
            val cabangBaru = myRef.push()
            val cabangId = cabangBaru.key ?: run {
                Toast.makeText(this, "Gagal generate ID", Toast.LENGTH_SHORT).show()
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
        cgPJ = findViewById(R.id.cgPenanggungJawab)
        tvEmptyPJ = findViewById(R.id.tvEmptyPegawai)
        etNomorCabang = findViewById(R.id.etNomorCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
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