package com.abryan.pointofsales.Transaksi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.TransaksiAdapter
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelCabang
import com.abryan.pointofsales.model.ModelItemTransaksi
import com.abryan.pointofsales.model.ModelKategori
import com.abryan.pointofsales.model.ModelPegawai
import com.abryan.pointofsales.model.ModelProduk
import com.abryan.pointofsales.model.ModelTransaksi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiActivity : AppCompatActivity() {

    private lateinit var rvProdukTransaksi: RecyclerView
    private lateinit var searchProduk: SearchView
    private lateinit var tvTotalItem: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var btnProsesTransaksi: Button
    private lateinit var imageBack: ImageView
    private lateinit var cgCabang: com.google.android.material.chip.ChipGroup
    private lateinit var cgKategori: com.google.android.material.chip.ChipGroup
    private lateinit var tvPilihCabangDulu: TextView
    private lateinit var tvLabelKategori: TextView

    private val database = FirebaseDatabase.getInstance()
    private val produkRef = database.getReference("Produk")
    private val cabangRef = database.getReference("cabang")
    private val kategoriRef = database.getReference("kategori")
    private val pegawaiRef = database.getReference("pegawai")

    private var listProduk = ArrayList<ModelProduk>()
    private var filteredList = ArrayList<ModelProduk>()
    private lateinit var adapter: TransaksiAdapter

    private var cabangDipilih = ""
    private var selectedAlamatCabang = ""
    private var selectedKategori = "Semua"
    private var kasirNama = "Kasir Default"
    private var kasirHp = ""

    private val activeCabangList = ArrayList<ModelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        init()

        val orientation = resources.configuration.orientation
        val columns = if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        rvProdukTransaksi.layoutManager = GridLayoutManager(this, columns)
        adapter = TransaksiAdapter(filteredList) { hitungTotal() }
        rvProdukTransaksi.adapter = adapter

        imageBack.setOnClickListener { finish() }

        loadCabang()
        loadKategori()

        val searchEditTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val searchEditText = searchProduk.findViewById<TextView>(searchEditTextId)
        searchEditText?.setTextColor(Color.WHITE)
        searchEditText?.setHintTextColor(Color.parseColor("#B0B0C0"))

        searchProduk.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterProdukLocal()
                return true
            }
        })

        btnProsesTransaksi.setOnClickListener { prosesTransaksi() }

        cgCabang.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                cabangDipilih = chip?.text?.toString() ?: ""
                val matched = activeCabangList.find { it.namaCabang == cabangDipilih }
                selectedAlamatCabang = matched?.alamatCabang ?: ""
                tvPilihCabangDulu.visibility = View.GONE
                tvLabelKategori.visibility = View.VISIBLE
                findViewById<View>(R.id.scrollKategori).visibility = View.VISIBLE
                rvProdukTransaksi.visibility = View.VISIBLE
                loadProduk(cabangDipilih)
                loadKasirCabang(cabangDipilih)
            } else {
                cabangDipilih = ""
                selectedAlamatCabang = ""
                tvPilihCabangDulu.visibility = View.VISIBLE
                tvLabelKategori.visibility = View.GONE
                findViewById<View>(R.id.scrollKategori).visibility = View.GONE
                rvProdukTransaksi.visibility = View.GONE
                listProduk.clear()
                filterProdukLocal()
            }
        }

        cgKategori.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedKategori = if (checkedIds.isNotEmpty()) {
                group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])?.text?.toString() ?: "Semua"
            } else "Semua"
            filterProdukLocal()
        }

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
                    Color.parseColor("#4F46E5"),
                    Color.parseColor("#2A2A3E")
                )
            )
            setTextColor(
                android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.WHITE,
                        Color.parseColor("#B0B0C0")
                    )
                )
            )
        }
    }

    private fun init() {
        rvProdukTransaksi = findViewById(R.id.rvProdukTransaksi)
        searchProduk = findViewById(R.id.SearchProduk)
        tvTotalItem = findViewById(R.id.tvTotalItem)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)
        btnProsesTransaksi = findViewById(R.id.btnProsesTransaksi)
        imageBack = findViewById(R.id.imageBack)
        cgCabang = findViewById(R.id.cgCabang)
        cgKategori = findViewById(R.id.cgKategori)
        tvPilihCabangDulu = findViewById(R.id.tvPilihCabangDulu)
        tvLabelKategori = findViewById(R.id.tvLabelKategori)
    }

    private fun loadCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgCabang.removeAllViews()
                activeCabangList.clear()
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    if (cabang != null && cabang.statusCabang == "Aktif") {
                        activeCabangList.add(cabang)
                        val name = cabang.namaCabang ?: continue
                        cgCabang.addView(buatChip(name))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal load cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadKategori() {
        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgKategori.removeAllViews()
                val allChip = buatChip("Semua").apply { isChecked = true }
                cgKategori.addView(allChip)
                for (data in snapshot.children) {
                    val kategori = data.getValue(ModelKategori::class.java)
                    if (kategori != null && kategori.statusKategori == "Aktif") {
                        val name = kategori.namaKategori ?: continue
                        cgKategori.addView(buatChip(name))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal load kategori: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadKasirCabang(cabangName: String) {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kasirNama = "Kasir Default"
                kasirHp = ""
                for (data in snapshot.children) {
                    val peg = data.getValue(ModelPegawai::class.java)
                    if (peg != null && peg.cabangPegawai == cabangName && peg.statusPegawai == "Aktif") {
                        kasirNama = peg.namaPegawai ?: "Kasir Default"
                        kasirHp = peg.nomorHp ?: ""
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadProduk(cabangName: String) {
        produkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (data in snapshot.children) {
                    val produk = data.getValue(ModelProduk::class.java)
                    if (produk != null && produk.status == "Aktif" && produk.cabang == cabangName && produk.stok > 0) {
                        listProduk.add(produk)
                    }
                }
                filterProdukLocal()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal load produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProdukLocal() {
        filteredList.clear()
        val query = searchProduk.query.toString().lowercase(Locale.getDefault())
        for (produk in listProduk) {
            val matchQuery = query.isEmpty() || produk.nama.lowercase(Locale.getDefault()).contains(query)
            val matchCategory = selectedKategori == "Semua" || selectedKategori.isEmpty() || produk.jenis.equals(selectedKategori, ignoreCase = true)
            if (matchQuery && matchCategory) filteredList.add(produk)
        }
        adapter.updateList(filteredList)
        hitungTotal()
    }

    private fun hitungTotal() {
        var totalQty = 0
        var totalHarga = 0L
        for ((produkId, jumlah) in adapter.selectedItems) {
            val produk = listProduk.find { it.id == produkId }
            if (produk != null) {
                totalQty += jumlah
                totalHarga += (produk.harga * jumlah)
            }
        }
        tvTotalItem.text = totalQty.toString()
        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        tvTotalHarga.text = "Rp " + formatRp.format(totalHarga)
    }

    private fun prosesTransaksi() {
        if (cabangDipilih.isEmpty()) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }
        if (adapter.selectedItems.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 produk!", Toast.LENGTH_SHORT).show()
            return
        }

        val sdfTanggal = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        val sdfWaktu = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        val currentDate = Date()
        val tanggal = sdfTanggal.format(currentDate)
        val waktu = sdfWaktu.format(currentDate)

        var totalQty = 0
        var totalHarga = 0L
        val listItem = ArrayList<ModelItemTransaksi>()

        for ((produkId, jumlah) in adapter.selectedItems) {
            val produk = listProduk.find { it.id == produkId }
            if (produk != null) {
                val subtotal = produk.harga * jumlah
                totalQty += jumlah
                totalHarga += subtotal
                listItem.add(
                    ModelItemTransaksi(
                        idProduk = produkId,
                        namaProduk = produk.nama,
                        harga = produk.harga,
                        jumlah = jumlah,
                        subtotal = subtotal,
                        imageUrl = produk.imageUrl
                    )
                )
            }
        }

        val ppn = (totalHarga * 0.11).toLong()
        val totalSetelahPpn = totalHarga + ppn

        showPaymentDialog(totalHarga, ppn, totalSetelahPpn, totalQty, tanggal, waktu, listItem)
    }

    private fun showPaymentDialog(
        totalHarga: Long,
        ppn: Long,
        totalSetelahPpn: Long,
        totalQty: Int,
        tanggal: String,
        waktu: String,
        listItem: List<ModelItemTransaksi>
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pembayaran, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvSubtotal = dialogView.findViewById<TextView>(R.id.tvSubtotal)
        val tvPpn = dialogView.findViewById<TextView>(R.id.tvPpn)
        val tvGrandTotal = dialogView.findViewById<TextView>(R.id.tvGrandTotal)
        val etNominalBayar = dialogView.findViewById<EditText>(R.id.etNominalBayar)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnBayar = dialogView.findViewById<Button>(R.id.btnBayar)

        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        tvSubtotal.text = "Rp " + formatRp.format(totalHarga)
        tvPpn.text = "Rp " + formatRp.format(ppn)
        tvGrandTotal.text = "Rp " + formatRp.format(totalSetelahPpn)

        etNominalBayar.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace(".", "").replace(",", "")
                if (input.isNotEmpty()) {
                    val number = input.toLongOrNull() ?: 0L
                    val formatted = formatRp.format(number)
                    etNominalBayar.setText(formatted)
                    etNominalBayar.setSelection(formatted.length)
                }
                isEditing = false
            }
        })

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnBayar.setOnClickListener {
            val nominalStr = etNominalBayar.text.toString().replace(".", "").replace(",", "")
            val nominalVal = nominalStr.toLongOrNull() ?: 0L

            if (nominalVal < totalSetelahPpn) {
                etNominalBayar.error = "Uang pembayaran kurang!"
                etNominalBayar.requestFocus()
                return@setOnClickListener
            }

            val kembalian = nominalVal - totalSetelahPpn

            val transaksi = ModelTransaksi(
                idTransaksi = "",
                tanggal = tanggal,
                waktu = waktu,
                totalHarga = totalHarga,
                ppn = ppn,
                totalSetelahPpn = totalSetelahPpn,
                nominalBayar = nominalVal,
                kembalian = kembalian,
                totalItem = totalQty,
                cabang = cabangDipilih,
                alamatCabang = selectedAlamatCabang,
                namaKasir = kasirNama,
                nomorKasir = kasirHp,
                listItem = listItem,
                statusTransaksi = "Selesai"
            )

            val intent = Intent(this@TransaksiActivity, HasilTransaksiActivity::class.java)
            intent.putExtra("transaksi", transaksi)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}/