package com.abryan.pointofsales

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.Kategori.DataKategoriActivity
import com.abryan.pointofsales.Cabang.DataCabangActivity
import com.abryan.pointofsales.Pegawai.DataPegawaiActivity
import com.abryan.pointofsales.Produk.DataProdukActivity
import com.abryan.pointofsales.Transaksi.RiwayatTransaksiActivity
import com.abryan.pointofsales.Transaksi.TransaksiActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var cardMoney: CardView
    private lateinit var cardTransaksi: CardView
    private lateinit var cardPegawai: CardView
    private lateinit var cardLaporan: CardView
    private lateinit var cardProduk: CardView
    private lateinit var cardKategori: CardView
    private lateinit var cardCabang: CardView
    private lateinit var cardPrinter: CardView
    private lateinit var cardProfile: CardView

    private lateinit var tvNamaHeader: TextView
    private lateinit var tvNamaCard: TextView
    private lateinit var tvRupiah: TextView
    private lateinit var fotoProfil: ImageView
    private lateinit var fotoProfilCard: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        init()
        setupListeners()
        loadUserData()
        loadTotalPendapatan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        cardMoney     = findViewById(R.id.cardMoney)
        cardTransaksi = findViewById(R.id.cardTransaksi)
        cardPegawai   = findViewById(R.id.cardPegawai)
        cardLaporan   = findViewById(R.id.cardLaporan)
        cardProduk    = findViewById(R.id.cardProduk)
        cardCabang    = findViewById(R.id.cardCabang)
        cardPrinter   = findViewById(R.id.cardPrinter)
        cardProfile   = findViewById(R.id.cardProfile)
        cardKategori  = findViewById(R.id.cardKategori)

        tvNamaHeader   = findViewById(R.id.tvNamaHeader)
        tvNamaCard     = findViewById(R.id.tvNamaCard)
        tvRupiah       = findViewById(R.id.Rupiah)
        fotoProfil     = findViewById(R.id.FotoProfil)
        fotoProfilCard = findViewById(R.id.FotoProfilCard)
    }

    private fun setupListeners() {
        cardKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }

        cardProduk.setOnClickListener {
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }

        cardCabang.setOnClickListener {
            val intent = Intent(this, DataCabangActivity::class.java)
            startActivity(intent)
        }

        cardPegawai.setOnClickListener {
            val intent = Intent(this, DataPegawaiActivity::class.java)
            startActivity(intent)
        }

        cardTransaksi.setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }

        cardPrinter.setOnClickListener {
            startActivity(Intent(this, RiwayatTransaksiActivity::class.java))
        }

        cardProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        findViewById<android.view.View>(R.id.btnTarikTunai).setOnClickListener {
            val intent = Intent(this, com.abryan.pointofsales.Transaksi.TarikTunaiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvNamaHeader.text = "Guest"
            tvNamaCard.text = "Guest"
            return
        }

        database.reference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama    = snapshot.child("nama").getValue(String::class.java) ?: "User"
                    val fotoUrl = snapshot.child("fotoProfil").getValue(String::class.java)

                    tvNamaHeader.text = nama
                    tvNamaCard.text = nama

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this@MainActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.profil)
                            .error(R.drawable.profil)
                            .circleCrop()
                            .into(fotoProfil)

                        Glide.with(this@MainActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.profil)
                            .error(R.drawable.profil)
                            .circleCrop()
                            .into(fotoProfilCard)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadTotalPendapatan() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            tvRupiah.text = "Rp 0"
            return
        }

        database.reference.child("users").child(uid).child("totalKeuntungan")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val total = snapshot.getValue(Long::class.java) ?: 0L
                        val formatRupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))
                        tvRupiah.text = "Rp ${formatRupiah.format(total)}"
                    } else {
                        migrateTotalFromTransaksi(uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat total pendapatan: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun migrateTotalFromTransaksi(uid: String) {
        database.reference.child("transaksi").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total: Long = 0
                for (transaksiSnapshot in snapshot.children) {
                    val totalHarga = transaksiSnapshot.child("totalHarga").getValue(Long::class.java) ?: 0L
                    total += totalHarga
                }
                database.reference.child("users").child(uid).child("totalKeuntungan").setValue(total)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        // loadTotalPendapatan() is already attached as a listener that updates automatically
        // but we can ensure user data refreshes if they edited their profile
    }
}