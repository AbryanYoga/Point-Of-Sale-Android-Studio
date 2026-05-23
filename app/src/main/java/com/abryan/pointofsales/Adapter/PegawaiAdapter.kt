package com.abryan.pointofsales.Adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Pegawai.ModPegawaiActivity
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelPegawai

class PegawaiAdapter(private val list: ArrayList<ModelPegawai>) :
    RecyclerView.Adapter<PegawaiAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPegawai: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvDetailPegawai: TextView = itemView.findViewById(R.id.tvDetailPegawai)
        val tvCabangPegawai: TextView = itemView.findViewById(R.id.tvCabangPegawai)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val indicatorStatus: View = itemView.findViewById(R.id.indicatorStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvNamaPegawai.text = data.namaPegawai
        holder.tvDetailPegawai.text = "${data.jenisKelamin} - ${data.nomorHp}"
        holder.tvCabangPegawai.text = data.cabangPegawai
        holder.tvStatus.text = data.statusPegawai

        val isAktif = data.statusPegawai == "Aktif"
        val color = if (isAktif) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.indicatorStatus.backgroundTintList = ColorStateList.valueOf(color)
        holder.tvStatus.setTextColor(color)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ModPegawaiActivity::class.java)
            intent.putExtra("pegawai", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}
