package com.abryan.pointofsales.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Transaksi.DetailTransaksiActivity
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class RiwayatTransaksiAdapter(
    private val list: ArrayList<ModelTransaksi>
) : RecyclerView.Adapter<RiwayatTransaksiAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRiwayatId: TextView = itemView.findViewById(R.id.tvRiwayatId)
        val tvRiwayatStatus: TextView = itemView.findViewById(R.id.tvRiwayatStatus)
        val tvRiwayatTanggalWaktu: TextView = itemView.findViewById(R.id.tvRiwayatTanggalWaktu)
        val tvRiwayatTotalItem: TextView = itemView.findViewById(R.id.tvRiwayatTotalItem)
        val tvRiwayatTotalHarga: TextView = itemView.findViewById(R.id.tvRiwayatTotalHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvRiwayatId.text = "#${data.idTransaksi.takeLast(6).uppercase()}"
        holder.tvRiwayatStatus.text = data.statusTransaksi
        holder.tvRiwayatTanggalWaktu.text = "${data.tanggal} - ${data.waktu}"
        holder.tvRiwayatTotalItem.text = "Total Item: ${data.totalItem}"

        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvRiwayatTotalHarga.text = "Rp " + formatRp.format(data.totalHarga)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailTransaksiActivity::class.java)
            intent.putExtra("transaksi", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}
