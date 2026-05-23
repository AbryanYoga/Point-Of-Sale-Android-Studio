package com.abryan.pointofsales.Transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.R
import com.abryan.pointofsales.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class HasilTransaksiActivity : AppCompatActivity() {

    private lateinit var tvStrukToko: TextView
    private lateinit var tvStrukTanggalWaktu: TextView
    private lateinit var tvStrukItems: TextView
    private lateinit var tvStrukTotal: TextView
    private lateinit var btnPrint: Button
    private lateinit var btnSelesai: Button
    private lateinit var btnTransaksiBaru: Button
    private lateinit var cardBack: CardView

    private var transaksi: ModelTransaksi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hasil_transaksi)

        init()

        transaksi = intent.getSerializableExtra("transaksi") as? ModelTransaksi

        if (transaksi != null) {
            tampilkanStruk()
        } else {
            Toast.makeText(this, "Data transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSelesai.setOnClickListener { finish() }

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
        tvStrukTanggalWaktu = findViewById(R.id.tvStrukTanggalWaktu)
        tvStrukItems = findViewById(R.id.tvStrukItems)
        tvStrukTotal = findViewById(R.id.tvStrukTotal)
        btnPrint = findViewById(R.id.btnPrint)
        btnSelesai = findViewById(R.id.btnSelesai)
        btnTransaksiBaru = findViewById(R.id.btnTransaksiBaru)
        cardBack = findViewById(R.id.cardBack)
        
        cardBack.setOnClickListener { finish() }
    }

    private fun tampilkanStruk() {
        transaksi?.let { data ->
            tvStrukTanggalWaktu.text = "Tanggal: ${data.tanggal} | Waktu: ${data.waktu}"

            val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
            val itemStr = StringBuilder()

            for (item in data.listItem) {
                itemStr.append("${item.namaProduk}\n")
                itemStr.append("${item.jumlah} x Rp ${formatRp.format(item.harga)} = Rp ${formatRp.format(item.subtotal)}\n\n")
            }
            tvStrukItems.text = itemStr.toString().trim()
            tvStrukTotal.text = "Rp " + formatRp.format(data.totalHarga)
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
                    textToPrint.append("[C]<b>TOKO ABRYAN</b>\n")
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[L]Tgl: ${data.tanggal} [R]Jam: ${data.waktu}\n")
                    textToPrint.append("[C]================================\n")
                    
                    for (item in data.listItem) {
                        textToPrint.append("[L]<b>${item.namaProduk}</b>\n")
                        textToPrint.append("[L]${item.jumlah} x Rp ${formatRp.format(item.harga)} [R]Rp ${formatRp.format(item.subtotal)}\n")
                    }
                    
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[L]<b>TOTAL</b> [R]<b>Rp ${formatRp.format(data.totalHarga)}</b>\n")
                    textToPrint.append("[C]================================\n")
                    textToPrint.append("[C]Terima Kasih\n")
                    
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
}
