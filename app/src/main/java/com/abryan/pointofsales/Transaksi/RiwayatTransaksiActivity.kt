package com.abryan.pointofsales.Transaksi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.RiwayatTransaksiAdapter
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelTransaksi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var rvRiwayatTransaksi: RecyclerView
    private lateinit var cardBack: CardView

    private val database = FirebaseDatabase.getInstance()
    private val transaksiRef = database.getReference("transaksi")

    private var listRiwayat = ArrayList<ModelTransaksi>()
    private lateinit var adapter: RiwayatTransaksiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayat_transaksi)

        rvRiwayatTransaksi = findViewById(R.id.rvRiwayatTransaksi)
        cardBack = findViewById(R.id.cardBack)

        cardBack.setOnClickListener { finish() }

        rvRiwayatTransaksi.layoutManager = LinearLayoutManager(this)
        adapter = RiwayatTransaksiAdapter(listRiwayat)
        rvRiwayatTransaksi.adapter = adapter

        loadRiwayat()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadRiwayat() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listRiwayat.clear()
                for (data in snapshot.children) {
                    val trx = data.getValue(ModelTransaksi::class.java)
                    if (trx != null) {
                        listRiwayat.add(trx)
                    }
                }
                listRiwayat.reverse() // Tampilkan yang terbaru di atas
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RiwayatTransaksiActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
