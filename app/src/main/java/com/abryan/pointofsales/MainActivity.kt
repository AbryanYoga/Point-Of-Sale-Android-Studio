package com.abryan.pointofsales

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.Kategori.DataKategoriActivity
import com.abryan.pointofsales.Produk.DataProdukActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var cardMoney: CardView
    lateinit var cardTransaksi: CardView
    lateinit var cardPegawai: CardView
    lateinit var cardLaporan: CardView
    lateinit var cardProduk: CardView
    lateinit var cardKategori: CardView
    lateinit var cardCabang: CardView
    lateinit var cardPrinter: CardView
    lateinit var cardProfile: CardView

    lateinit var tvNama: TextView
    lateinit var tvWelcome: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Jika belum login, redirect ke LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginProfile::class.java))
            finish()
            return
        }

        init()
        loadUserData() // ← fungsi ini dipanggil di sini

        cardKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            intent.putExtra("menu", "Kategori")
            startActivity(intent)
        }

        cardProduk.setOnClickListener {
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }

        cardProfile.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginProfile::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        cardMoney     = findViewById(R.id.cardMoney)
        cardTransaksi = findViewById(R.id.cardTransaksi)
        cardPegawai   = findViewById(R.id.cardPegawai)
        cardLaporan   = findViewById(R.id.cardLaporan)
        cardProduk    = findViewById(R.id.cardProduk)
        cardCabang    = findViewById(R.id.cardCabang)
        cardPrinter   = findViewById(R.id.cardPrinter)
        cardProfile   = findViewById(R.id.cardProfile)
        cardKategori  = findViewById(R.id.cardKategori)
        tvNama        = findViewById(R.id.tvnama)
        tvWelcome     = findViewById(R.id.tvwelcome)
    }

    // ← fungsi ini yang kurang, tambahkan di sini
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        database.reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama = snapshot.child("nama").getValue(String::class.java) ?: "User"
                    tvNama.text = nama
                    tvWelcome.text = "Selamat Datang, $nama!"
                }

                override fun onCancelled(error: DatabaseError) {                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}