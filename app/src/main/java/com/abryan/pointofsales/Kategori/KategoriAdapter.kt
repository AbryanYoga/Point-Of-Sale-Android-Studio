package com.abryan.pointofsales.Kategori

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelKategori

class KategoriAdapter(private val list: ArrayList<ModelKategori>) :
    RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val indicatorStatus: View = itemView.findViewById(R.id.indicatorStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvNamaKategori.text = data.namaKategori
        holder.tvStatus.text = data.statusKategori

        val isAktif = data.statusKategori == "Aktif"
        val color = if (isAktif) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.indicatorStatus.backgroundTintList = ColorStateList.valueOf(color)
        holder.tvStatus.setTextColor(color)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ModKategoriActivity::class.java)
            intent.putExtra("kategori", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}