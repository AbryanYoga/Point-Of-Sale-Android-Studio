package com.abryan.pointofsales.Transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.TransaksiAdapter
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelItemTransaksi
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

    private val database = FirebaseDatabase.getInstance()
    private val produkRef = database.getReference("Produk")
    private val transaksiRef = database.getReference("transaksi")

    private var listProduk = ArrayList<ModelProduk>()
    private var filteredList = ArrayList<ModelProduk>()
    private lateinit var adapter: TransaksiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        init()

        rvProdukTransaksi.layoutManager = LinearLayoutManager(this)
        adapter = TransaksiAdapter(filteredList) {
            hitungTotal()
        }
        rvProdukTransaksi.adapter = adapter

        imageBack.setOnClickListener { finish() }

        loadProduk()

        searchProduk.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterProduk(newText ?: "")
                return true
            }
        })

        btnProsesTransaksi.setOnClickListener { prosesTransaksi() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        rvProdukTransaksi = findViewById(R.id.rvProdukTransaksi)
        searchProduk = findViewById(R.id.SearchProduk)
        tvTotalItem = findViewById(R.id.tvTotalItem)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)
        btnProsesTransaksi = findViewById(R.id.btnProsesTransaksi)
        imageBack = findViewById(R.id.imageBack)
    }

    private fun loadProduk() {
        produkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (data in snapshot.children) {
                    val produk = data.getValue(ModelProduk::class.java)
                    if (produk != null && produk.status == "Aktif" && produk.stok > 0) {
                        listProduk.add(produk)
                    }
                }
                filterProduk(searchProduk.query.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal meload produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProduk(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(listProduk)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (produk in listProduk) {
                if (produk.nama.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(produk)
                }
            }
        }
        adapter.updateList(filteredList)
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
        if (adapter.selectedItems.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 produk!", Toast.LENGTH_SHORT).show()
            return
        }

        btnProsesTransaksi.isEnabled = false

        val transaksiId = transaksiRef.push().key ?: return

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
                        subtotal = subtotal
                    )
                )
                
                // Kurangi stok
                val newStok = produk.stok - jumlah
                produkRef.child(produkId).child("stok").setValue(newStok)
            }
        }

        val transaksi = ModelTransaksi(
            idTransaksi = transaksiId,
            tanggal = tanggal,
            waktu = waktu,
            totalHarga = totalHarga,
            totalItem = totalQty,
            cabang = "Pusat", // Default cabang if not specified
            listItem = listItem,
            statusTransaksi = "Selesai"
        )

        transaksiRef.child(transaksiId).setValue(transaksi)
            .addOnSuccessListener {
                Toast.makeText(this, "Transaksi Berhasil", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HasilTransaksiActivity::class.java)
                intent.putExtra("transaksi", transaksi)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                btnProsesTransaksi.isEnabled = true
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
