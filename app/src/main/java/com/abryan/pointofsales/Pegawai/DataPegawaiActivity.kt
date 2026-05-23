package com.abryan.pointofsales.Pegawai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Adapter.PegawaiAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelPegawai

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvPegawai: RecyclerView

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var listPegawai: ArrayList<ModelPegawai>
    private lateinit var adapter: PegawaiAdapter
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabData = findViewById(R.id.fabDataPegawai)
        rvPegawai = findViewById(R.id.rvDataPegawai)

        rvPegawai.layoutManager = LinearLayoutManager(this)
        listPegawai = ArrayList()
        adapter = PegawaiAdapter(listPegawai)
        rvPegawai.adapter = adapter

        fabData.setOnClickListener {
            startActivity(Intent(this, ModPegawaiActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()
                for (data in snapshot.children) {
                    val pegawai = data.getValue(ModelPegawai::class.java)
                    if (pegawai != null) listPegawai.add(pegawai)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataPegawaiActivity,
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
