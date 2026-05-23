package com.abryan.pointofsales.Adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Cabang.ModCabangActivity
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelCabang

class CabangAdapter(private val list: ArrayList<ModelCabang>) :
    RecyclerView.Adapter<CabangAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNamaCabang)
        val tvKodeCabang: TextView = itemView.findViewById(R.id.tvKodeCabang)
        val tvAlamatCabang: TextView = itemView.findViewById(R.id.tvAlamatCabang)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val indicatorStatus: View = itemView.findViewById(R.id.indicatorStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvNamaCabang.text = data.namaCabang
        holder.tvKodeCabang.text = data.kodeCabang
        holder.tvAlamatCabang.text = data.alamatCabang
        holder.tvStatus.text = data.statusCabang

        val isAktif = data.statusCabang == "Aktif"
        val color = if (isAktif) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.indicatorStatus.backgroundTintList = ColorStateList.valueOf(color)
        holder.tvStatus.setTextColor(color)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ModCabangActivity::class.java)
            intent.putExtra("cabang", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}
