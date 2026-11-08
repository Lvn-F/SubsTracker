# Subscription Tracker

Tugas P10 Pemrograman Mobile (SharedPreferences) - Prodi STI, Universitas Muhammadiyah Kotabumi.
Aplikasi Android (Java) untuk mencatat langganan digital, total pengeluaran, dan pengingat jatuh tempo.

## Cara menjalankan
1. Buka folder ini di Android Studio (Hedgehog atau lebih baru, JDK 17).
2. Tunggu Gradle sync selesai.
3. Jalankan di emulator atau HP (minSdk 24).

## Pembagian data
- **SharedPreferences** (`PrefsManager`): mata uang, hari pengingat, mode gelap, saklar notifikasi, urutan daftar.
- **SQLite** (`DatabaseHelper`): tabel `subscriptions` dan `payment_history`.

## Alur utama
- `SettingsActivity` menyimpan pengaturan lewat `PrefsManager` (`apply()`).
- `MainActivity` membaca ulang pengaturan di `onResume()`, jadi tema, mata uang, dan urutan langsung berubah.
- Tekan lama item di daftar: tandai sudah dibayar (tanggal tagih maju satu siklus) atau hapus.
- `ReminderWorker` (WorkManager) mengirim notifikasi sekali sehari untuk tagihan yang jatuh tempo.
