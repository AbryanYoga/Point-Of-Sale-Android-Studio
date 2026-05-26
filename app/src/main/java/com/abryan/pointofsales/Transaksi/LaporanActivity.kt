package com.abryan.pointofsales.Transaksi

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.LaporanAdapter
import com.abryan.pointofsales.Adapter.PenarikanAdapter
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelPenarikan
import com.abryan.pointofsales.model.ModelTransaksi
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var cardBack: CardView
    private lateinit var tvLaporanSaldo: TextView
    private lateinit var tvLaporanPemasukan: TextView
    private lateinit var tvLaporanPengeluaran: TextView

    private lateinit var btnTabTransaksi: MaterialCardView
    private lateinit var btnTabPenarikan: MaterialCardView
    private lateinit var tvTextTabTransaksi: TextView
    private lateinit var tvTextTabPenarikan: TextView

    private lateinit var rvLaporan: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val listTransaksi = ArrayList<ModelTransaksi>()
    private val listPenarikan = ArrayList<ModelPenarikan>()

    private var totalPemasukan: Long = 0
    private var totalPengeluaran: Long = 0

    private var activeTab = "transaksi" // "transaksi" or "penarikan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        initViews()
        setupListeners()
        setupRecyclerView()
        loadData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        cardBack = findViewById(R.id.cardBack)
        tvLaporanSaldo = findViewById(R.id.tvLaporanSaldo)
        tvLaporanPemasukan = findViewById(R.id.tvLaporanPemasukan)
        tvLaporanPengeluaran = findViewById(R.id.tvLaporanPengeluaran)

        btnTabTransaksi = findViewById(R.id.btnTabTransaksi)
        btnTabPenarikan = findViewById(R.id.btnTabPenarikan)
        tvTextTabTransaksi = findViewById(R.id.tvTextTabTransaksi)
        tvTextTabPenarikan = findViewById(R.id.tvTextTabPenarikan)

        rvLaporan = findViewById(R.id.rvLaporan)
    }

    private fun setupListeners() {
        cardBack.setOnClickListener { finish() }

        btnTabTransaksi.setOnClickListener {
            if (activeTab != "transaksi") {
                activeTab = "transaksi"
                updateTabUI()
            }
        }

        btnTabPenarikan.setOnClickListener {
            if (activeTab != "penarikan") {
                activeTab = "penarikan"
                updateTabUI()
            }
        }
    }

    private fun setupRecyclerView() {
        rvLaporan.layoutManager = LinearLayoutManager(this)
        updateTabUI() // sets adapter initially
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Load Pemasukan (Transaksi)
        database.getReference("transaksi")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listTransaksi.clear()
                    totalPemasukan = 0
                    for (child in snapshot.children) {
                        val trx = child.getValue(ModelTransaksi::class.java)
                        if (trx != null) {
                            listTransaksi.add(trx)
                            totalPemasukan += trx.totalHarga
                        }
                    }
                    listTransaksi.reverse() // show latest first
                    calculateAndDisplay()
                    if (activeTab == "transaksi") {
                        rvLaporan.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LaporanActivity, "Gagal memuat transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // 2. Load Pengeluaran (Penarikan)
        database.getReference("penarikan").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listPenarikan.clear()
                    totalPengeluaran = 0
                    for (child in snapshot.children) {
                        val penarikan = child.getValue(ModelPenarikan::class.java)
                        if (penarikan != null) {
                            listPenarikan.add(penarikan)
                            totalPengeluaran += penarikan.nominal
                        }
                    }
                    listPenarikan.reverse() // show latest first
                    calculateAndDisplay()
                    if (activeTab == "penarikan") {
                        rvLaporan.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LaporanActivity, "Gagal memuat penarikan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun calculateAndDisplay() {
        val saldoBersih = totalPemasukan - totalPengeluaran
        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))

        tvLaporanPemasukan.text = "Rp " + formatRp.format(totalPemasukan)
        tvLaporanPengeluaran.text = "Rp " + formatRp.format(totalPengeluaran)
        tvLaporanSaldo.text = "Rp " + formatRp.format(saldoBersih)
    }

    private fun updateTabUI() {
        if (activeTab == "transaksi") {
            btnTabTransaksi.setCardBackgroundColor(ContextCompat.getColor(this, R.color.CardDalam))
            btnTabPenarikan.setCardBackgroundColor(ContextCompat.getColor(this, R.color.backgroundModKategori))

            tvTextTabTransaksi.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTextTabPenarikan.setTypeface(null, android.graphics.Typeface.NORMAL)

            rvLaporan.adapter = LaporanAdapter(listTransaksi)
        } else {
            btnTabTransaksi.setCardBackgroundColor(ContextCompat.getColor(this, R.color.backgroundModKategori))
            btnTabPenarikan.setCardBackgroundColor(ContextCompat.getColor(this, R.color.CardDalam))

            tvTextTabTransaksi.setTypeface(null, android.graphics.Typeface.NORMAL)
            tvTextTabPenarikan.setTypeface(null, android.graphics.Typeface.BOLD)

            rvLaporan.adapter = PenarikanAdapter(listPenarikan)
        }
    }
}
