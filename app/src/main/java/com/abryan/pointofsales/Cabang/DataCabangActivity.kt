package com.abryan.pointofsales.Cabang

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.CabangAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelCabang

class DataCabangActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvCabang: RecyclerView

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var listCabang: ArrayList<ModelCabang>
    private lateinit var adapter: CabangAdapter
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabData = findViewById(R.id.fabDataCabang)
        rvCabang = findViewById(R.id.rvDataCabang)

        rvCabang.layoutManager = LinearLayoutManager(this)
        listCabang = ArrayList()
        adapter = CabangAdapter(listCabang)
        rvCabang.adapter = adapter

        fabData.setOnClickListener {
            startActivity(Intent(this, ModCabangActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    if (cabang != null) listCabang.add(cabang)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataCabangActivity,
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
