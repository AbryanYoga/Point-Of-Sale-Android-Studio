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

    lateinit var tvNamaCard: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        init()
        loadUserData()

        cardKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            intent.putExtra("menu", "Kategori")
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

        // Klik profile → buka ProfileActivity, bukan langsung logout
        cardProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
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
        tvNamaCard    = findViewById(R.id.tvNamaCard)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvNamaCard.text = "Guest"
            return
        }

        database.reference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama    = snapshot.child("nama").getValue(String::class.java) ?: "User"
                    val fotoUrl = snapshot.child("fotoProfil").getValue(String::class.java)

                    tvNamaCard.text = nama

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this@MainActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.profil)
                            .error(R.drawable.profil)
                            .circleCrop()
                            .into(findViewById(R.id.FotoProfil))

                        Glide.with(this@MainActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.jokowi)
                            .error(R.drawable.jokowi)
                            .circleCrop()
                            .into(findViewById(R.id.FotoProfilCard))
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

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}