package com.abryan.pointofsales.Produk

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.ProdukAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelProduk

class DataProdukActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvProduk: RecyclerView

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")

    private lateinit var listProduk: ArrayList<ModelProduk>
    private lateinit var adapter: ProdukAdapter
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_produk)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabData = findViewById(R.id.fabDataProduk)
        rvProduk = findViewById(R.id.rvDataProduk)

        rvProduk.layoutManager = LinearLayoutManager(this)
        listProduk = ArrayList()
        adapter = ProdukAdapter(listProduk)
        rvProduk.adapter = adapter

        fabData.setOnClickListener {
            startActivity(Intent(this, ModProduk::class.java))
        }

        loadData()
    }

    private fun loadData() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (data in snapshot.children) {
                    val produk = data.getValue(ModelProduk::class.java)
                    if (produk != null) listProduk.add(produk)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataProdukActivity,
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