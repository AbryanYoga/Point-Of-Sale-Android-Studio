package com.abryan.pointofsales.Transaksi

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abryan.pointofsales.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TarikTunaiActivity : AppCompatActivity() {

    private lateinit var cardBack: CardView
    private lateinit var tvSaldoSaatIni: TextView
    private lateinit var etNominalPenarikan: EditText
    private lateinit var etNomorTujuan: EditText
    private lateinit var btnProsesPenarikan: MaterialButton

    // Payment Cards
    private lateinit var cardQris: MaterialCardView
    private lateinit var cardGopay: MaterialCardView
    private lateinit var cardDana: MaterialCardView
    private lateinit var cardOvo: MaterialCardView
    private lateinit var cardShopeePay: MaterialCardView
    private lateinit var cardBri: MaterialCardView
    private lateinit var cardBca: MaterialCardView
    private lateinit var cardBni: MaterialCardView
    private lateinit var cardMandiri: MaterialCardView
    private lateinit var cardCimb: MaterialCardView
    private lateinit var cardBsi: MaterialCardView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var currentSaldo: Long = 0L
    private var selectedMetode: String = ""
    private var selectedMethodCard: MaterialCardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tarik_tunai)

        initViews()
        setupListeners()
        loadSaldo()
        setupFormatNominal()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        cardBack = findViewById(R.id.cardBack)
        tvSaldoSaatIni = findViewById(R.id.tvSaldoSaatIni)
        etNominalPenarikan = findViewById(R.id.etNominalPenarikan)
        etNomorTujuan = findViewById(R.id.etNomorTujuan)
        btnProsesPenarikan = findViewById(R.id.btnProsesPenarikan)

        // Cards mapping
        cardQris = findViewById(R.id.cardQris)
        cardGopay = findViewById(R.id.cardGopay)
        cardDana = findViewById(R.id.cardDana)
        cardOvo = findViewById(R.id.cardOvo)
        cardShopeePay = findViewById(R.id.cardShopeePay)
        cardBri = findViewById(R.id.cardBri)
        cardBca = findViewById(R.id.cardBca)
        cardBni = findViewById(R.id.cardBni)
        cardMandiri = findViewById(R.id.cardMandiri)
        cardCimb = findViewById(R.id.cardCimb)
        cardBsi = findViewById(R.id.cardBsi)
    }

    private fun setupListeners() {
        cardBack.setOnClickListener { finish() }

        // Grid single selection bindings
        val cardMap = mapOf(
            cardQris to "QRIS",
            cardGopay to "GoPay",
            cardDana to "Dana",
            cardOvo to "OVO",
            cardShopeePay to "ShopeePay",
            cardBri to "BRI",
            cardBca to "BCA",
            cardBni to "BNI",
            cardMandiri to "Mandiri",
            cardCimb to "CIMB Niaga",
            cardBsi to "BSI"
        )

        for ((card, method) in cardMap) {
            card.setOnClickListener {
                selectPaymentMethod(card, method)
            }
        }

        btnProsesPenarikan.setOnClickListener {
            prosesPenarikan()
        }
    }

    private fun selectPaymentMethod(card: MaterialCardView, method: String) {
        // Reset previous stroke
        selectedMethodCard?.let {
            it.strokeWidth = 0
            it.strokeColor = Color.TRANSPARENT
        }

        // Highlight selected card
        selectedMethodCard = card
        selectedMetode = method

        val borderDp = 3f
        val strokePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            borderDp,
            resources.displayMetrics
        ).toInt()

        card.strokeColor = Color.parseColor("#214EB1") // Highlight blue
        card.strokeWidth = strokePx
    }

    private fun loadSaldo() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("totalKeuntungan")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentSaldo = snapshot.getValue(Long::class.java) ?: 0L
                    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
                    tvSaldoSaatIni.text = "Rp " + formatRp.format(currentSaldo)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TarikTunaiActivity, "Gagal memuat saldo", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupFormatNominal() {
        etNominalPenarikan.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace(".", "").replace(",", "")
                if (input.isNotEmpty()) {
                    val number = input.toLongOrNull() ?: 0L
                    val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
                    etNominalPenarikan.setText(formatted)
                    etNominalPenarikan.setSelection(formatted.length)
                }
                isEditing = false
            }
        })
    }

    private fun prosesPenarikan() {
        val nominalStr = etNominalPenarikan.text.toString().replace(".", "").replace(",", "")
        val nominalVal = nominalStr.toLongOrNull() ?: 0L
        val nomorTujuan = etNomorTujuan.text.toString().trim()

        if (nominalVal <= 0L) {
            etNominalPenarikan.error = "Masukkan nominal penarikan yang valid"
            etNominalPenarikan.requestFocus()
            return
        }

        if (nominalVal > currentSaldo) {
            etNominalPenarikan.error = "Nominal melebihi saldo saat ini"
            etNominalPenarikan.requestFocus()
            return
        }

        if (selectedMetode.isEmpty()) {
            Toast.makeText(this, "Pilih metode penarikan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (nomorTujuan.isEmpty()) {
            etNomorTujuan.error = "Nomor tujuan tidak boleh kosong"
            etNomorTujuan.requestFocus()
            return
        }

        val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID"))
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Penarikan")
            .setMessage("Apakah Anda yakin ingin menarik uang sebesar Rp ${formatRp.format(nominalVal)} ke $selectedMetode ($nomorTujuan)?")
            .setPositiveButton("Ya") { _, _ ->
                eksekusiPenarikan(nominalVal, nomorTujuan)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun eksekusiPenarikan(nominal: Long, nomorTujuan: String) {
        val uid = auth.currentUser?.uid ?: return
        btnProsesPenarikan.isEnabled = false

        // Fetch balance once more to prevent race conditions
        val userRef = database.getReference("users").child(uid).child("totalKeuntungan")
        userRef.get().addOnSuccessListener { snapshot ->
            val freshSaldo = snapshot.getValue(Long::class.java) ?: 0L
            if (nominal > freshSaldo) {
                Toast.makeText(this, "Gagal: Saldo tidak mencukupi", Toast.LENGTH_LONG).show()
                btnProsesPenarikan.isEnabled = true
                return@addOnSuccessListener
            }

            // Substract balance
            userRef.setValue(freshSaldo - nominal).addOnSuccessListener {
                // Log the withdrawal
                val penarikanRef = database.getReference("penarikan").child(uid)
                val newWithdrawal = penarikanRef.push()
                val withdrawalId = newWithdrawal.key ?: ("WD_" + System.currentTimeMillis() / 1000)

                val sdfTanggal = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
                val sdfWaktu = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
                val now = Date()

                val dataPenarikan = hashMapOf<String, Any>(
                    "id" to withdrawalId,
                    "tanggal" to sdfTanggal.format(now),
                    "waktu" to sdfWaktu.format(now),
                    "nominal" to nominal,
                    "metode" to selectedMetode,
                    "nomorTujuan" to nomorTujuan,
                    "status" to "Berhasil"
                )

                newWithdrawal.setValue(dataPenarikan)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Penarikan berhasil", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        btnProsesPenarikan.isEnabled = true
                        Toast.makeText(this, "Gagal menyimpan riwayat: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                btnProsesPenarikan.isEnabled = true
                Toast.makeText(this, "Gagal mendebit saldo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            btnProsesPenarikan.isEnabled = true
            Toast.makeText(this, "Gagal memproses penarikan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
