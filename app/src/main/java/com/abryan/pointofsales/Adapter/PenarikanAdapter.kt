package com.abryan.pointofsales.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelPenarikan
import java.text.NumberFormat
import java.util.Locale

class PenarikanAdapter(
    private val list: List<ModelPenarikan>
) : RecyclerView.Adapter<PenarikanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPenarikanMetodeTujuan: TextView = itemView.findViewById(R.id.tvPenarikanMetodeTujuan)
        val tvPenarikanTanggalWaktu: TextView = itemView.findViewById(R.id.tvPenarikanTanggalWaktu)
        val tvPenarikanStatus: TextView = itemView.findViewById(R.id.tvPenarikanStatus)
        val tvPenarikanNominal: TextView = itemView.findViewById(R.id.tvPenarikanNominal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_laporan_penarikan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvPenarikanMetodeTujuan.text = "${data.metode} - ${data.nomorTujuan}"
        holder.tvPenarikanTanggalWaktu.text = "${data.tanggal} ${data.waktu}"
        holder.tvPenarikanStatus.text = data.status.ifEmpty { "Berhasil" }

        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvPenarikanNominal.text = "-Rp " + formatRp.format(data.nominal)
    }
}
