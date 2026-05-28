package com.abryan.pointofsales.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelProduk
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter(
    private var list: ArrayList<ModelProduk>,
    private val onTotalChanged: () -> Unit
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    val selectedItems = HashMap<String, Int>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvHargaProduk: TextView = itemView.findViewById(R.id.tvHargaProduk)
        val tvStokProduk: TextView = itemView.findViewById(R.id.tvStokProduk)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val btnPlus: CardView = itemView.findViewById(R.id.btnPlus)
        val btnMinus: CardView = itemView.findViewById(R.id.btnMinus)
        val cardItemProduk: View = itemView.findViewById(R.id.cardItemProduk)
        val imgProdukTransaksi: android.widget.ImageView = itemView.findViewById(R.id.imgProdukTransaksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = list[position]
        
        holder.tvNamaProduk.text = produk.nama
        
        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvHargaProduk.text = "Rp " + formatRp.format(produk.harga)
        
        holder.tvStokProduk.text = "Stok: ${produk.stok}"

        Glide.with(holder.itemView.context)
            .load(produk.imageUrl.takeIf { it.isNotEmpty() })
            .placeholder(R.drawable.produk)
            .error(R.drawable.produk)
            .into(holder.imgProdukTransaksi)

        val produkId = produk.id
        var jumlah = selectedItems[produkId] ?: 0
        holder.tvJumlah.text = jumlah.toString()
        
        updateCardColor(holder.cardItemProduk, jumlah)

        holder.btnPlus.setOnClickListener {
            if (jumlah < produk.stok) {
                jumlah++
                selectedItems[produkId] = jumlah
                holder.tvJumlah.text = jumlah.toString()
                updateCardColor(holder.cardItemProduk, jumlah)
                onTotalChanged()
            }
        }

        holder.btnMinus.setOnClickListener {
            if (jumlah > 0) {
                jumlah--
                if (jumlah == 0) {
                    selectedItems.remove(produkId)
                } else {
                    selectedItems[produkId] = jumlah
                }
                holder.tvJumlah.text = jumlah.toString()
                updateCardColor(holder.cardItemProduk, jumlah)
                onTotalChanged()
            }
        }
    }

    private fun updateCardColor(card: View, jumlah: Int) {
        val cardView = card as? com.google.android.material.card.MaterialCardView
        if (jumlah > 0) {
            val selectedColor = androidx.core.content.ContextCompat.getColor(card.context, R.color.CardKategori)
            cardView?.setCardBackgroundColor(selectedColor) // highlight selection
        } else {
            val defaultColor = androidx.core.content.ContextCompat.getColor(card.context, R.color.bgCard)
            cardView?.setCardBackgroundColor(defaultColor) // default theme background
        }
    }

    fun updateList(newList: ArrayList<ModelProduk>) {
        list = newList
        notifyDataSetChanged()
    }
}
