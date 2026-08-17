# TheWatcher

Aplikasi Android (tanpa root) untuk memantau penggunaan data internet dari
device yang terhubung ke hotspot HP kamu.

Fitur MVP (v0.1):
- Daftar device yang terhubung ke hotspot (MAC, IP, nama).
- Total konsumsi data hotspot (agregat) — diukur dari `TrafficStats`.
- Estimasi pemakaian **per-device** (dibagi proporsional durasi koneksi).
- **History per sesi hotspot**: tiap sesi mencatat total MB keluar + estimasi
  per-device, bisa dilihat kembali di layar History.
- Grafik 7 hari pemakaian harian.

## ⚠️ Batasan penting (jujur)
- **Per-device = ESTIMASI, bukan byte sesungguhnya.** Sejak Android 10, byte
  per-MAC tidak diekspos ke aplikasi non-root. Estimasi = total tethered ×
  (durasi client ÷ Σ durasi). Total agregat hotspot mendekati pasti.
- Aplikasi menampilkan banner peringatan di UI.

## Build & Test
Build APK dan unit test dijalankan otomatis di **GitHub Actions** (tidak ada
build lokal). Setiap push ke `main` memicu workflow `.github/workflows/build.yml`
yang menghasilkan artifact APK.

Cara menjalankan unit test / build secara lokal (bila punya toolchain):
```bash
./gradlew testDebugUnitTest   # unit test logika murni
./gradlew assembleDebug       # APK debug
```

## Testing on device
Butuh HP Android 12+ (API 31+):
1. Nyalakan hotspot.
2. Izinkan permission Location & Notifications saat diminta.
3. Buka TheWatcher, jalankan monitoring.
4. Hubungkan device lain ke hotspot → muncul di daftar.
5. Stop monitoring → sesi tersimpan di History.

## Tech
Kotlin + Jetpack Compose, Room, Coroutines/Flow, minSdk 26, targetSdk 34.
