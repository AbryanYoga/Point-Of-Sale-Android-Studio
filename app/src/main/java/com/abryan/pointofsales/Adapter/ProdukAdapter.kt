package com.abryan.pointofsales.Adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.Produk.ModProduk
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelProduk
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(private val list: ArrayList<ModelProduk>) :
    RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProduk: TextView = itemView.findViewById(R.id.namaProduk)
        val tvHarga: TextView = itemView.findViewById(R.id.hargaProduk)
        val tvJenis: TextView = itemView.findViewById(R.id.jenisProduk)
        val tvStok: TextView = itemView.findViewById(R.id.jumlahProduk)
        val tvCabang: TextView = itemView.findViewById(R.id.CabangProduk)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val indicatorStatus: View = itemView.findViewById(R.id.indicatorStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        val formatRupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))

        holder.tvNamaProduk.text = data.nama
        holder.tvHarga.text = "Rp ${formatRupiah.format(data.harga)}"
        holder.tvJenis.text = data.jenis
        holder.tvStok.text = "${data.stok} pcs"
        holder.tvCabang.text = data.cabang
        holder.tvStatus.text = data.status

        val isAktif = data.status == "Aktif"
        val color = if (isAktif) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.indicatorStatus.backgroundTintList = ColorStateList.valueOf(color)
        holder.tvStatus.setTextColor(color)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ModProduk::class.java)
            intent.putExtra("produk", data)
            holder.itemView.context.startActivity(intent)
        }
    }
}