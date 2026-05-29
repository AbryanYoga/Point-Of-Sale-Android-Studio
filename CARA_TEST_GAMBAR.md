# Panduan Testing Preview Gambar Produk

## ⚠️ PENTING - Langkah Pertama

### 1. Sync Gradle
Setelah perubahan kode, **WAJIB** sync gradle terlebih dahulu:
- Klik **File** → **Sync Project with Gradle Files**
- Atau klik icon **Sync** di toolbar Android Studio
- Tunggu sampai proses sync selesai

### 2. Clean & Rebuild
Setelah sync, lakukan clean build:
```
Build → Clean Project
Build → Rebuild Project
```

### 3. Uninstall Aplikasi Lama
Hapus aplikasi lama dari device/emulator:
- Uninstall aplikasi PointOfSales dari device
- Atau jalankan: `adb uninstall com.abryan.pointofsales`

### 4. Install Ulang
- Run aplikasi dari Android Studio
- Tunggu sampai instalasi selesai

---

## 📱 Cara Testing

### Test 1: URL Imgur (Recommended)

1. **Upload gambar ke Imgur:**
   - Buka https://imgur.com
   - Klik "New post" atau drag & drop gambar
   - Setelah upload, klik kanan pada gambar
   - Pilih "Copy image address" atau "Copy image link"
   - URL akan seperti: `https://i.imgur.com/xxxxx.jpg`

2. **Test di aplikasi:**
   - Buka menu Produk → Tambah Produk
   - Paste URL di field "GAMBAR PRODUK (URL)"
   - Klik tombol "Preview Gambar"
   - **Perhatikan toast message yang muncul**

### Test 2: URL Test Sederhana

Gunakan URL test ini untuk memastikan fungsi preview bekerja:

```
https://picsum.photos/400/300
```

Atau:

```
https://via.placeholder.com/400x300.png
```

### Test 3: URL Gambar Langsung

Contoh URL yang valid:
```
https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/1200px-Cat03.jpg
```

---

## 🔍 Cara Melihat Error di Logcat

Jika gambar tidak muncul, periksa Logcat:

1. **Buka Logcat** di Android Studio (Alt+6)

2. **Filter dengan tag "ModProduk":**
   - Di search box Logcat, ketik: `tag:ModProduk`

3. **Perhatikan pesan error:**
   - `Started loading image` → Proses dimulai
   - `Image loaded successfully` → Berhasil ✓
   - `Failed to load image` → Gagal, lihat detail errornya

4. **Error umum dan solusinya:**

   **Error: "Unable to resolve host"**
   - ❌ Masalah: Tidak ada koneksi internet
   - ✅ Solusi: Periksa koneksi WiFi/data

   **Error: "Cleartext HTTP traffic not permitted"**
   - ❌ Masalah: URL menggunakan HTTP (bukan HTTPS)
   - ✅ Solusi: Sudah diperbaiki dengan `usesCleartextTraffic="true"`

   **Error: "404 Not Found"**
   - ❌ Masalah: URL tidak valid atau gambar sudah dihapus
   - ✅ Solusi: Gunakan URL yang valid

   **Error: "Failed to decode"**
   - ❌ Masalah: File bukan gambar atau corrupt
   - ✅ Solusi: Pastikan URL mengarah ke file gambar valid

---

## ✅ Checklist Troubleshooting

Jika gambar masih tidak muncul, cek satu per satu:

- [ ] Sudah sync gradle?
- [ ] Sudah clean & rebuild project?
- [ ] Sudah uninstall aplikasi lama?
- [ ] Sudah install ulang aplikasi?
- [ ] Device/emulator terhubung ke internet?
- [ ] URL dimulai dengan `http://` atau `https://`?
- [ ] URL bisa dibuka di browser?
- [ ] Sudah cek Logcat untuk error message?
- [ ] Toast message muncul saat klik "Preview Gambar"?

---

## 🎯 Perubahan yang Dilakukan

### 1. **Mengganti Library**
- ❌ Glide (kadang bermasalah dengan Kotlin)
- ✅ Coil (modern, dibuat khusus untuk Kotlin)

### 2. **Menambahkan Dependencies**
```kotlin
implementation("io.coil-kt:coil:2.5.0")
kapt("com.github.bumptech.glide:compiler:4.16.0")
```

### 3. **Kode Lebih Sederhana**
```kotlin
imgPreview.load(url) {
    crossfade(true)
    placeholder(R.drawable.produk)
    error(R.drawable.produk)
    listener(...)
}
```

### 4. **Error Handling Lebih Baik**
- Toast message untuk setiap status (loading, success, error)
- Log detail di Logcat
- Pesan error yang informatif

---

## 📞 Jika Masih Bermasalah

Kirimkan screenshot dari:
1. **Logcat** dengan filter `tag:ModProduk`
2. **Toast message** yang muncul
3. **URL** yang digunakan
4. **Screenshot** halaman preview

---

## 🧪 URL Test yang Bisa Digunakan

### Gambar Statis:
```
https://picsum.photos/400/300
https://via.placeholder.com/400x300.png
https://dummyimage.com/400x300/000/fff
```

### Gambar Real:
```
https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/400px-Cat03.jpg
```

### Imgur (setelah upload):
```
https://i.imgur.com/[kode].jpg
```

---

## ⚡ Tips

1. **Gunakan HTTPS** bukan HTTP untuk keamanan
2. **Imgur adalah pilihan terbaik** untuk hosting gambar
3. **Jangan gunakan** Google Drive link biasa
4. **Pastikan URL** langsung mengarah ke file gambar
5. **Test dengan URL sederhana** dulu sebelum URL kompleks
