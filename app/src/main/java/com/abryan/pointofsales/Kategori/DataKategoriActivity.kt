package com.abryan.pointofsales.Kategori

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelKategori

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvKategori: RecyclerView

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: KategoriAdapter
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabData = findViewById(R.id.fabDataKategori)
        rvKategori = findViewById(R.id.rvDataKategori)

        rvKategori.layoutManager = LinearLayoutManager(this)
        listKategori = ArrayList()
        adapter = KategoriAdapter(listKategori)
        rvKategori.adapter = adapter

        fabData.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                for (data in snapshot.children) {
                    val kategori = data.getValue(ModelKategori::class.java)
                    if (kategori != null) listKategori.add(kategori)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataKategoriActivity,
                    "Gagal mengambil data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        myRef.addValueEventListener(valueEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        myRef.removeEventListener(valueEventListener)
    }
}