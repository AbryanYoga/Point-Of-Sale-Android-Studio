package com.abryan.pointofsales.Transaksi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelTransaksi
import com.abryan.pointofsales.model.ModelItemTransaksi
import java.text.NumberFormat
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HasilTransaksiActivity : AppCompatActivity() {

    private lateinit var tvStrukToko: TextView
    private lateinit var tvNamaCabang: TextView
    private lateinit var tvAlamatToko: TextView
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvStrukTanggalWaktu: TextView
    private lateinit var tvNamaKasir: TextView
    private lateinit var tvHpKasir: TextView
    private lateinit var rvStrukItems: RecyclerView
    private lateinit var tvStrukSubtotal: TextView
    private lateinit var tvStrukPpn: TextView
    private lateinit var tvStrukTotal: TextView
    private lateinit var tvStrukBayar: TextView
    private lateinit var tvStrukKembalian: TextView
    private lateinit var tvStrukContact: TextView
    private lateinit var btnPrint: Button
    private lateinit var btnTransaksiBaru: Button

    private var transaksi: ModelTransaksi? = null

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var namaKasir = "Kasir Default"
    private var nomorHpKasir = ""
    private var namaCabang = ""
    private var alamatCabang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hasil_transaksi)

        init()

        val partialTransaksi = intent.getSerializableExtra("transaksi") as? ModelTransaksi

        if (partialTransaksi != null) {
            namaKasir = intent.getStringExtra("namaKasir") ?: "Kasir Default"
            nomorHpKasir = intent.getStringExtra("nomorKasir") ?: ""
            namaCabang = intent.getStringExtra("namaCabang") ?: ""
            alamatCabang = intent.getStringExtra("alamatCabang") ?: ""

            finalizeTransaction(partialTransaksi)
        } else {
            Toast.makeText(this, "Data transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnTransaksiBaru.setOnClickListener {
            val intent = Intent(this, TransaksiActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnPrint.setOnClickListener {
            cekPermissionDanPrint()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        tvStrukToko = findViewById(R.id.tvStrukToko)
        tvNamaCabang = findViewById(R.id.tvNamaCabang)
        tvAlamatToko = findViewById(R.id.tvAlamatToko)
        tvIdTransaksi = findViewById(R.id.tvIdTransaksi)
        tvStrukTanggalWaktu = findViewById(R.id.tvStrukTanggalWaktu)
        tvNamaKasir = findViewById(R.id.tvNamaKasir)
        tvHpKasir = findViewById(R.id.tvHpKasir)
        rvStrukItems = findViewById(R.id.rvStrukItems)
        tvStrukSubtotal = findViewById(R.id.tvStrukSubtotal)
        tvStrukPpn = findViewById(R.id.tvStrukPpn)
        tvStrukTotal = findViewById(R.id.tvStrukTotal)
        tvStrukBayar = findViewById(R.id.tvStrukBayar)
        tvStrukKembalian = findViewById(R.id.tvStrukKembalian)
        tvStrukContact = findViewById(R.id.tvStrukContact)
        btnPrint = findViewById(R.id.btnPrint)
        btnTransaksiBaru = findViewById(R.id.btnTransaksiBaru)
    }

    private fun finalizeTransaction(partial: ModelTransaksi) {
        val timestamp = System.currentTimeMillis() / 1000
        val finalId = "TRX_$timestamp"

        val finalTransaksi = partial.copy(
            idTransaksi = finalId,
            cabang = namaCabang,
            alamatCabang = alamatCabang,
            namaKasir = namaKasir,
            nomorKasir = nomorHpKasir
        )
        transaksi = finalTransaksi

        // Simpan ke Firebase "transaksi"
        database.getReference("transaksi").child(finalId).setValue(finalTransaksi)
            .addOnSuccessListener {
                Toast.makeText(this, "Transaksi Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                
                // Add transaction total to user's totalKeuntungan balance
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val userRef = database.getReference("users").child(uid).child("totalKeuntungan")
                    userRef.get().addOnSuccessListener { snapshot ->
                        val currentSaldo = snapshot.getValue(Long::class.java) ?: 0L
                        userRef.setValue(currentSaldo + finalTransaksi.totalHarga)
                    }
                }

                // Kurangi stok di Firebase untuk masing-masing item
                for (item in finalTransaksi.listItem) {
                    val produkId = item.idProduk
                    val qty = item.jumlah
                    val stokRef = database.getReference("Produk").child(produkId).child("stok")
                    stokRef.get().addOnSuccessListener { productSnapshot ->
                        val currentStok = productSnapshot.getValue(Int::class.java) ?: 0
                        stokRef.setValue((currentStok - qty).coerceAtLeast(0))
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_LONG).show()
            }

        tampilkanStruk()
    }

    private fun tampilkanStruk() {
        transaksi?.let { data ->
            tvIdTransaksi.text = "#TRX-${data.idTransaksi.takeLast(8)}"
            tvStrukTanggalWaktu.text = "${data.tanggal} | ${data.waktu}"
            tvNamaKasir.text = data.namaKasir
            tvHpKasir.text = if (data.nomorKasir.isNotEmpty()) data.nomorKasir else "-"
            tvStrukContact.text = "Contact: ${if (data.nomorKasir.isNotEmpty()) data.nomorKasir else "-"}"
            tvNamaCabang.text = data.cabang
            tvAlamatToko.text = if (data.alamatCabang.isNotEmpty()) data.alamatCabang else "Alamat Cabang"

            val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
            tvStrukSubtotal.text = "Rp " + formatRp.format(data.totalHarga)
            tvStrukPpn.text = "Rp " + formatRp.format(data.ppn)
            tvStrukTotal.text = "Rp " + formatRp.format(data.totalSetelahPpn)
            tvStrukBayar.text = "Rp " + formatRp.format(data.nominalBayar)
            tvStrukKembalian.text = "Rp " + formatRp.format(data.kembalian)

            // Setup RecyclerView
            rvStrukItems.layoutManager = LinearLayoutManager(this)
            rvStrukItems.adapter = StrukItemAdapter(data.listItem)
        }
    }

    private fun cekPermissionDanPrint() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val connectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            val scanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            
            if (connectPermission != PackageManager.PERMISSION_GRANTED || scanPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                    101
                )
                return
            }
        }
        
        doPrint()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doPrint()
            } else {
                Toast.makeText(this, "Akses Bluetooth dibutuhkan untuk nge-print", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doPrint() {
        transaksi?.let { data ->
            try {
                val connection = BluetoothPrintersConnections.selectFirstPaired()
                if (connection != null) {
                    val printer = EscPosPrinter(connection, 203, 48f, 32)
                    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
                    
                    val textToPrint = StringBuilder()
                    textToPrint.append("[C]<b>Point Of Sales</b>\n")
                    textToPrint.append("[C]${data.cabang}\n")
                    textToPrint.append("[C]${data.alamatCabang}\n")
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[L]ID: #TRX-${data.idTransaksi.takeLast(8)}\n")
                    textToPrint.append("[L]Tgl: ${data.tanggal} [R]Jam: ${data.waktu}\n")
                    textToPrint.append("[L]Kasir: ${data.namaKasir}\n")
                    if (data.nomorKasir.isNotEmpty()) {
                        textToPrint.append("[L]HP Kasir: ${data.nomorKasir}\n")
                    }
                    textToPrint.append("[C]================================\n")
                    
                    for (item in data.listItem) {
                        textToPrint.append("[L]<b>${item.namaProduk}</b>\n")
                        textToPrint.append("[L]${item.jumlah} x Rp ${formatRp.format(item.harga)} [R]Rp ${formatRp.format(item.subtotal)}\n")
                    }
                    
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[L]Subtotal [R]Rp ${formatRp.format(data.totalHarga)}\n")
                    textToPrint.append("[L]PPN (11%) [R]Rp ${formatRp.format(data.ppn)}\n")
                    textToPrint.append("[L]<b>TOTAL</b> [R]<b>Rp ${formatRp.format(data.totalSetelahPpn)}</b>\n")
                    textToPrint.append("[L]Bayar [R]Rp ${formatRp.format(data.nominalBayar)}\n")
                    textToPrint.append("[L]Kembalian [R]Rp ${formatRp.format(data.kembalian)}\n")
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[C]Terima kasih telah berbelanja di Point Of Sales!\n")
                    textToPrint.append("[C]Sampai jumpa kembali 😊\n")
                    
                    printer.printFormattedText(textToPrint.toString())
                    printer.disconnectPrinter()
                    Toast.makeText(this, "Berhasil mencetak struk", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Printer tidak ditemukan. Pastikan sudah di-pair.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal nge-print: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private class StrukItemAdapter(private val list: List<ModelItemTransaksi>) : RecyclerView.Adapter<StrukItemAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNamaItem: TextView = view.findViewById(R.id.tvNamaItem)
            val tvJumlahHarga: TextView = view.findViewById(R.id.tvJumlahHarga)
            val tvSubtotalItem: TextView = view.findViewById(R.id.tvSubtotalItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_struk_produk, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvNamaItem.text = item.namaProduk
            val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
            holder.tvJumlahHarga.text = "${item.jumlah} x Rp ${formatRp.format(item.harga)}"
            holder.tvSubtotalItem.text = "Rp ${formatRp.format(item.subtotal)}"
        }
    }
}
