package com.abryan.pointofsales.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.R
import com.abryan.pointofsales.Transaksi.DetailTransaksiActivity
import com.abryan.pointofsales.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class LaporanAdapter(
    private val list: List<ModelTransaksi>
) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLaporanTxId: TextView = itemView.findViewById(R.id.tvLaporanTxId)
        val tvLaporanTxCabang: TextView = itemView.findViewById(R.id.tvLaporanTxCabang)
        val tvLaporanTxTanggalWaktu: TextView = itemView.findViewById(R.id.tvLaporanTxTanggalWaktu)
        val tvLaporanTxTotal: TextView = itemView.findViewById(R.id.tvLaporanTxTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_laporan_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvLaporanTxId.text = "TX-${data.idTransaksi.takeLast(8).uppercase()}"
        holder.tvLaporanTxCabang.text = "Cabang: ${data.cabang.ifEmpty { "Pusat" }}"
        holder.tvLaporanTxTanggalWaktu.text = "${data.tanggal} ${data.waktu}"

        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvLaporanTxTotal.text = "+Rp " + formatRp.format(data.totalHarga)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailTransaksiActivity::class.java)
            intent.putExtra("transaksi", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}
