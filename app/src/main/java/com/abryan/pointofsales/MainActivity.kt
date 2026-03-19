package com.abryan.pointofsales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.Kategori.DataKategoriActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        init()

        cardKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            intent.putExtra("menu", "Kategori")
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        cardMoney = findViewById(R.id.cardMoney)
        cardTransaksi = findViewById(R.id.cardTransaksi)
        cardPegawai = findViewById(R.id.cardPegawai)
        cardLaporan = findViewById(R.id.cardLaporan)
        cardProduk = findViewById(R.id.cardProduk)
        cardCabang = findViewById(R.id.cardCabang)
        cardPrinter = findViewById(R.id.cardPrinter)
        cardProfile = findViewById(R.id.cardProfile)
        cardKategori = findViewById(R.id.cardKategori)

    }
}