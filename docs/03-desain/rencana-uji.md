[Kembali ke daftar isi](../README.md)

# Rencana Uji

Kasus uji yang dapat langsung dijalankan untuk Sistem Peminjaman Fasilitas Olahraga Kampus. Setiap kasus ditelusuri ke kriteria penerimaan di [02-use-case](../02-use-case/), aturan di [model-data.md](model-data.md), [state-diagram.md](state-diagram.md), [arsitektur.md](arsitektur.md), dan pesan di [pesan-sistem.md](pesan-sistem.md). Kolom **Hasil aktual** sengaja kosong dan diisi saat uji dijalankan.

## 1. Tujuan dan strategi

| Hal | Keputusan |
|---|---|
| Tujuan | Membuktikan bahwa setiap alur normal dan alur alternatif use case, aturan integritas 1-13, dan invarian status 1-6 berjalan sesuai rancangan |
| Jenis uji | **M** = manual lewat browser; **O** = otomatis dengan fitur uji bawaan Laravel (PHPUnit), tanpa paket tambahan |
| Mengapa sebagian otomatis | Kasus konflik serentak, perintah terjadwal, dan tanggal lampau tidak dapat direproduksi dengan klik; kasus lain cukup manual |
| Database uji | **MySQL**, bukan SQLite: SQLite tidak menjalankan penguncian baris, sehingga kasus serentak (UC07-08, KON-08, KON-09) tidak bermakna di sana |
| Kasus serentak | Dua proses terpisah memanggil layanan bersamaan; skenario diulang 50 kali bila tertulis |
| Waktu | Uji otomatis mengatur waktu dengan `Carbon::setTestNow` pada zona `Asia/Jakarta` |
| Prioritas | **1** = alur inti (UC02, UC07, UC03, UC06), konflik, tanggal, dan akses; **2** = sisanya |
| Kriteria lulus | Semua kasus prioritas 1 lulus; kasus prioritas 2 yang gagal dicatat beserta alasannya |
| Acuan sementara | Sebelum view Blade selesai, perilaku di [prototipe](../prototipe/index.html) dipakai sebagai acuan; hasil aktual untuk aplikasi Laravel dicatat setelah aplikasi dibangun |

Setiap pola EARS (dasar, When atau While, If-Then) dan setiap alur alternatif UC01-UC09 punya minimal satu kasus; ketelusurannya ada di bagian 4.

## 2. Lingkungan dan data uji

Setiap kasus dijalankan dari data awal di bawah (seeder dijalankan ulang), kecuali kasus yang tertulis "lanjut" dari kasus sebelumnya. Tanggal sistem uji **H = 25 Okt 2026**; seeder memakai selisih terhadap H (H+3 = 28 Okt), sehingga rencana ini berlaku untuk tanggal berapa pun.

**Akun** (password uji ditentukan seeder):

| Akun | Email | Peran | Identitas |
|---|---|---|---|
| Alice Johnson | `alice@example.com` | Pengguna | 5025251101 |
| Bob Smith | `bob@example.com` | Pengguna | 5025251102 |
| Carol Davis | `carol@example.com` | Pengguna | Example Student Association |
| Facility Manager | `manager@example.com` | Pengelola | (kosong) |

**Fasilitas dan sesi:** Lapangan Futsal A, Lapangan Basket A, Lapangan Badminton 1, Lapangan Tenis 1. Setiap fasilitas punya empat sesi per hari (08.00-10.00, 10.00-12.00, 13.00-15.00, 15.00-17.00) untuk 35 hari mulai H-13. Sesi berstatus Ditutup: Futsal A 28 Okt 13.00-15.00 dan 15.00-17.00; Basket A 1 Nov semua sesi.

**Pengajuan awal** (status sebelum perintah terjadwal pertama; sesi Terisi dikunci oleh pengajuan Disetujui atau Menunggu Konfirmasi yang sebelumnya Disetujui):

| Kode | Pemohon | Fasilitas, tanggal, sesi | Status | Peran dalam uji |
|---|---|---|---|---|
| P1 | Ani | Futsal A, H-5, 08.00-10.00 | Disetujui | Peminjaman lampau yang tidak boleh berubah |
| P2 | Ani | Basket A, H+3, 08.00-10.00 dan 15.00-17.00 | Ditolak (alasan P16) | Detail alasan penolakan |
| P3 | Ani | Futsal A, H+4, 10.00-12.00 | Menunggu Verifikasi | Konflik dengan P5 |
| P4 | Ani | Tenis 1, H+5, 13.00-15.00 | Disetujui | Pembatalan Disetujui, laporan |
| P5 | Budi | Futsal A, H+4, 10.00-12.00 | Menunggu Verifikasi | Konflik dengan P3 |
| P6 | Budi | Badminton 1, H+4, 13.00-15.00 dan 15.00-17.00 | Menunggu Verifikasi | Pengajuan dua sesi |
| P7 | Citra | Tenis 1, H+6, 08.00-10.00 | Menunggu Konfirmasi (sebelumnya Disetujui) | UC08 melepas sesi |
| P8 | Citra | Futsal A, H+8, 10.00-12.00 | Menunggu Konfirmasi (sebelumnya Menunggu) | UC08 tanpa melepas sesi |
| P9 | Citra | Basket A, H-3, 13.00-15.00 | Dibatalkan | Tidak menguasai sesi |
| P10 | Citra | Basket A, H-4, 08.00-10.00 | Ditolak | Tidak menguasai sesi |
| P11 | Budi | Basket A, H+9, empat sesi | Disetujui | Tanggal Penuh, laporan |
| P12 | Budi | Futsal A, H-3, 10.00-12.00 | Menunggu Verifikasi (kedaluwarsa) | Ditolak otomatis (P26) |
| P13 | Citra | Tenis 1, H-2, 10.00-12.00 | Menunggu Konfirmasi (sebelumnya Disetujui, kedaluwarsa) | Dibatalkan otomatis, sesi dilepas |

Kasus NF memakai tambahan 100 pengajuan acak yang valid (dibuat lewat seeder khusus uji).


## 3. Kasus uji

Kolom: **Pri** prioritas (1 atau 2), **J** jenis (M atau O).

### 3.1 Autentikasi (S01, S02)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| AUT-01 | 1 | M | Login pengguna berhasil | Buka `/login`, masuk sebagai Ani | Masuk ke S03 Jadwal dan Peminjaman dengan kerangka bar atas |  |
| AUT-02 | 1 | M | Login pengelola berhasil | Masuk sebagai pengelola | Masuk ke S07 Kelola Jadwal dengan kerangka menu samping |  |
| AUT-03 | 1 | M | Login gagal | Isi email benar dan password salah, tekan Masuk; lalu ubah isian email | P21 tampil; pesan hilang begitu isian diubah; masuk lagi dengan password salah memunculkan P21 kembali |  |
| AUT-04 | 2 | M | Registrasi berhasil | Isi semua kolom (termasuk identitas) dengan email baru, tekan Daftar | P24 tampil, diarahkan ke login; akun baru berperan pengguna |  |
| AUT-05 | 2 | M | Registrasi email sudah ada | Daftar dengan email `alice@example.com` | P22 tampil, akun tidak bertambah |  |
| AUT-06 | 2 | M | Registrasi dengan kolom kosong | Kosongkan kolom identitas (atau kolom lain), tekan Daftar | P23 tampil, akun tidak dibuat |  |
| AUT-07 | 1 | M | Halaman terlindungi tanpa login | Tanpa login, buka `/schedule`, `/history`, `/verification` | Selalu dialihkan ke `/login` |  |
| AUT-08 | 2 | M | Logout | Tekan Keluar, lalu tekan tombol Kembali browser | Kembali ke login; halaman terlindungi tidak dapat dibuka tanpa login ulang |  |

### 3.2 UC01 Melihat Informasi Fasilitas dan Jadwal (S03)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC01-01 | 1 | M | Halaman awal dan status tanggal | Login sebagai Ani, buka S03. Pilih Futsal A (bulan berjalan), lalu Basket A | Fasilitas pertama dan bulan berjalan tampil otomatis. Futsal A: 28 Okt Sebagian, 29 Okt Tersedia. Basket A: 1 Nov Ditutup, 3 Nov Penuh. Warna selalu disertai legenda |  |
| UC01-02 | 1 | M | Memilih tanggal menampilkan sesi | Klik 28 Okt pada Futsal A | Panel menampilkan empat sesi: 08.00-10.00 dan 10.00-12.00 Tersedia, 13.00-15.00 dan 15.00-17.00 Ditutup |  |
| UC01-03 | 2 | M | Mengganti fasilitas dan bulan | Pilih tanggal, lalu ganti fasilitas; kemudian ganti bulan | Kalender dan panel menyesuaikan; tanggal dan sesi terpilih direset saat fasilitas diganti |  |
| UC01-04 | 1 | M | Tanggal lampau dan tanpa sesi tidak dapat dipilih | Klik tanggal sebelum 25 Okt dan tanggal di luar rentang sesi | Tanggal tampil pudar dan klik tidak memilih apa pun |  |
| UC01-05 | 2 | M | Belum memilih tanggal | Buka S03 tanpa mengklik tanggal | Panel sesi menampilkan P25 |  |
| UC01-06 | 2 | M | Bulan tanpa sesi | Pindah ke bulan di luar rentang sesi (lebih dari H+34, setelah `slots:generate`) | P01 tampil; tidak ada tanggal yang dapat dipilih |  |

### 3.3 UC02 Mengajukan Peminjaman (S03)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC02-01 | 1 | M | Alur sampai dialog ketentuan | Pilih Futsal A, 30 Okt, satu sesi; isi data pemohon; tekan Ajukan Peminjaman | Ringkasan pilihan tampil dan tombol aktif; dialog ketentuan tampil; Setuju & Kirim nonaktif sampai kotak dicentang |  |
| UC02-02 | 1 | M | Alur normal kirim pengajuan | Lanjut UC02-01: centang persetujuan, tekan Setuju & Kirim | P02 (toast hijau) tampil; pengajuan tersimpan berstatus Menunggu Verifikasi; sesi tetap Tersedia bagi pengguna lain |  |
| UC02-03 | 1 | M | Batas empat sesi | Pilih empat sesi pada satu tanggal | Setelah empat sesi terpilih, sesi lain tidak dapat dipilih; membatalkan satu pilihan membuka kembali pilihan lain |  |
| UC02-04 | 1 | M | Sesi Terisi atau Ditutup tidak dapat dipilih | Klik sesi Terisi (Tenis 30 Okt, 13.00-15.00) dan sesi Ditutup (Futsal A 28 Okt, 13.00-15.00) | Kedua sesi tidak dapat dipilih |  |
| UC02-05 | 1 | M | Data pemohon tidak lengkap | Kosongkan unit/organisasi, tekan Ajukan Peminjaman; lalu isi unit | P03 tampil, dialog ketentuan tidak muncul; pesan hilang begitu isian diubah |  |
| UC02-06 | 2 | M | Aturan isian | Coba jumlah peserta 0, 1,5, dan teks; unit berisi spasi saja; ketik melebihi batas karakter | Semuanya P03 untuk nilai tidak valid; batas karakter (unit 50, kontak 20, keperluan 200) mencegah ketikan berlebih |  |
| UC02-07 | 1 | O | Sesi menjadi tidak tersedia saat kirim | Pilih sesi Futsal A 30 Okt 10.00-12.00; sebelum kirim, ubah sesi itu menjadi Terisi atau Ditutup; kirim | P04 tampil; tidak ada pengajuan tersimpan; status sesi diperbarui di tampilan |  |
| UC02-08 | 1 | O | Kirim untuk sesi bertanggal lewat | Atur tanggal sistem ke H+1 setelah pengguna memilih sesi bertanggal H; kirim | P04 tampil; tidak ada pengajuan tersimpan |  |
| UC02-09 | 1 | O | Pengajuan di luar aturan langsung ke server | Kirim permintaan POST dengan lima sesi, atau sesi dari fasilitas atau tanggal berbeda | Ditolak server; tidak ada baris `bookings` atau `booking_time_slot` tersimpan |  |
| UC02-10 | 2 | M | Penutupan dialog | Buka dialog ketentuan, tekan ikon silang | Dialog tertutup tanpa mengirim; tidak ada tombol Batal |  |
| UC02-11 | 1 | M | Dua pengguna mengajukan sesi yang sama | Sebagai Budi dan Ani, ajukan Futsal A 29 Okt 10.00-12.00 (data P3 dan P5) | Keduanya tersimpan berstatus Menunggu Verifikasi; sesi tetap Tersedia |  |

### 3.4 UC03 Memantau Status Pengajuan (S04)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC03-01 | 1 | M | Isi dan urutan daftar | Login sebagai Ani, buka S04 | Hanya pengajuan Ani bertanggal H atau mendatang (P2, P3, P4), terbaru di atas; P1 (H-5) tidak tampil; setiap entri menampilkan fasilitas, tanggal, sesi, status |  |
| UC03-02 | 1 | M | Lima status | Periksa status P2, P3, P4 dan, setelah uji lain, status Menunggu Konfirmasi dan Dibatalkan | Status hanya salah satu dari lima nilai dan sesuai data tersimpan |  |
| UC03-03 | 1 | M | Alasan penolakan | Pilih P2 (Ditolak), lalu pilih P3 (Menunggu) | Detail P2 menampilkan alasan penolakan; detail P3 tidak menampilkan alasan; daftar tidak menampilkan alasan |  |
| UC03-04 | 1 | M | Belum ada pengajuan | Login dengan akun baru tanpa pengajuan | P05 tampil |  |
| UC03-05 | 1 | M | Tidak melihat milik pengguna lain | Bandingkan daftar Ani dengan Budi | Pengajuan Budi (P5, P6, P11, P12) tidak tampil di daftar Ani |  |
| UC03-06 | 2 | M | Pengajuan lewat tanggal tidak tampil | Setelah perintah terjadwal, lihat S04 dan S06 untuk Budi | P12 (ditolak otomatis) tidak tampil di S04 dan terlihat di S06 |  |

### 3.5 UC04 Mengajukan Pembatalan (S05)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC04-01 | 2 | M | Isi daftar pembatalan | Login sebagai Ani, buka S05 | Hanya P3 (Menunggu) dan P4 (Disetujui) bertanggal H atau mendatang; P2 (Ditolak) dan P1 (lampau) tidak tampil |  |
| UC04-02 | 2 | M | Permintaan pembatalan berhasil | Pilih P4, isi alasan, tekan Ajukan Pembatalan | P06 tampil; status menjadi Menunggu Konfirmasi Pembatalan; `previous_status` = 2; sesi tetap Terisi |  |
| UC04-03 | 2 | M | Alasan kosong | Pilih P3, kosongkan atau isi spasi saja, tekan Ajukan Pembatalan; lalu isi alasan | P07 tampil dan status tidak berubah; pesan hilang saat isian diubah |  |
| UC04-04 | 2 | M | Tidak ada yang dapat dibatalkan | Login sebagai akun tanpa pengajuan status 1 atau 2 bertanggal H atau mendatang | P27 tampil |  |
| UC04-05 | 2 | O | Pengajuan sudah berubah saat dibatalkan | Pengguna memilih P3; pengelola menolak P3; pengguna menekan Ajukan Pembatalan | P31 tampil; status tetap Ditolak; daftar diperbarui |  |
| UC04-06 | 2 | O | Pengajuan bertanggal lewat | Kirim permintaan pembatalan langsung untuk P1 (Disetujui, H-5) | P31; status tetap Disetujui; riwayat dan laporan tidak berubah |  |
| UC04-07 | 2 | O | Pengajuan milik pengguna lain | Sebagai Ani, kirim POST `/cancellations/{id P5}` (milik Budi) | 404; status P5 tidak berubah |  |

### 3.6 UC05 Melihat Riwayat Peminjaman (S06)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC05-01 | 2 | M | Riwayat pengguna | Login sebagai Ani, buka S06 | Semua pengajuan Ani termasuk P1 (lampau), terbaru di atas; entri menampilkan fasilitas, tanggal, sesi, status |  |
| UC05-02 | 2 | M | Detail dan alasan penolakan | Pilih P2 (alasan P16), lalu setelah perintah terjadwal pilih P12 milik Budi (akun Budi) | Alasan penolakan tampil hanya di detail; P12 menampilkan alasan baku P26 |  |
| UC05-03 | 2 | M | Riwayat pengelola | Login sebagai pengelola, buka S06 | Riwayat semua pengguna dengan identitas peminjam, terbaru di atas |  |
| UC05-04 | 2 | M | Belum ada riwayat | Login dengan akun baru, buka S06 | P08 tampil |  |
| UC05-05 | 2 | O | Gagal memuat | Paksa kegagalan pengambilan data (matikan koneksi database atau lempar pengecualian pada kueri), buka S06; tekan Coba lagi saat masih gagal, lalu setelah pulih | P09 dan tombol Coba lagi tampil; gagal lagi menampilkan pesan yang sama; setelah pulih daftar tampil; data tidak berubah |  |
| UC05-06 | 2 | M | Sesi tidak valid | Hapus cookie sesi, buka `/history` | Dialihkan ke `/login` |  |
| UC05-07 | 2 | O | Riwayat pengguna lain | Sebagai Ani, buka `/history` dan coba parameter yang menunjuk pengguna lain | Hanya riwayat Ani yang tampil |  |
| UC05-08 | 2 | M | Filter fasilitas dan status (pengelola) | Login sebagai pengelola, buka S06, pilih Fasilitas Tenis 1 dan Status Disetujui, tekan Terapkan; lalu tekan Atur Ulang | Hanya P4 tampil (Tenis 1, Disetujui); setelah Atur Ulang semua riwayat tampil kembali |  |
| UC05-09 | 2 | O | Filter rentang tanggal peminjaman | Sebagai pengelola isi tanggal awal H+3 dan tanggal akhir H+5, tekan Terapkan | Hanya P2, P3, P5, P6, dan P4 (tanggal H+3 sampai H+5, batas termasuk) tampil, urut terbaru; P7 (H+6) dan P1 (H-5) tidak tampil |  |
| UC05-10 | 2 | M | Periode filter tidak valid (A5) | Isi tanggal awal H+5 dan tanggal akhir H+3, tekan Terapkan; lalu ubah tanggal awal | P19 tampil dan daftar tidak berubah; P19 hilang begitu isian diubah |  |
| UC05-11 | 2 | O | Tidak ada riwayat yang sesuai filter (A6) | Login sebagai Ani, buka S06, pilih Status Dibatalkan, tekan Terapkan | P33 tampil (bukan P08) karena Ani punya riwayat tetapi tidak ada yang Dibatalkan; riwayat Budi dan Citra tidak pernah tampil |  |

### 3.7 UC06 Mengelola Jadwal Fasilitas (S07)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC06-01 | 1 | M | Tampilan sesi dan tanggal | Login sebagai pengelola, pilih Futsal A, klik 29 Okt, lalu klik Tenis 30 Okt | Status sesi tampil dengan warna dan label teks; sesi Terisi tidak dapat diklik; tanggal lampau dan tanpa sesi tidak dapat dipilih |  |
| UC06-02 | 1 | M | Belum memilih tanggal | Buka S07 tanpa memilih tanggal | Panel sesi menampilkan P25 |  |
| UC06-03 | 1 | M | Tombol Simpan | Awal: periksa Simpan; balik satu sesi; balik sesi yang sama lagi | Simpan nonaktif di awal, aktif setelah satu sesi dibalik, nonaktif lagi setelah dibalik ke status awal |  |
| UC06-04 | 1 | M | Simpan perubahan lintas tanggal | Tutup satu sesi di 29 Okt, buka kembali sesi Ditutup di 28 Okt, lalu tekan Simpan sekali | P10 tampil; kedua perubahan tersimpan; sesi yang ditutup tidak dapat dipilih pengguna; sesi yang dibuka dapat dipilih |  |
| UC06-05 | 1 | O | Sesi baru terisi saat simpan | Pengelola menandai sesi Futsal A 29 Okt 10.00-12.00 untuk ditutup dan satu sesi lain untuk dibuka; sebelum Simpan, setujui P3 | P11 (toast kuning) tampil; sesi 10.00-12.00 tetap Terisi (merah); perubahan lain tersimpan |  |
| UC06-06 | 1 | O | Perubahan di luar aturan langsung ke server | Kirim PUT yang menutup sesi Terisi atau mengubah sesi bertanggal lewat | Sesi Terisi tidak berubah; sesi lampau tidak berubah |  |
| UC06-07 | 2 | M | Ganti fasilitas membuang perubahan | Balik satu sesi, ganti fasilitas | Perubahan belum disimpan dibuang; Simpan nonaktif |  |
| UC06-08 | 2 | M | Pengajuan menunggu pada sesi yang ditutup | Tutup sesi Futsal A 29 Okt 10.00-12.00 (P3 menunggu), buka S08 | P3 tetap di antrian (ditangani A3 UC07) |  |

### 3.8 UC07 dan UC07.1 Memverifikasi dan Menolak Pengajuan (S08)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC07-01 | 1 | M | Isi dan urutan antrian | Login sebagai pengelola, buka S08 | Hanya status Menunggu Verifikasi, waktu pengajuan terlama di atas; pengajuan bertanggal lewat tidak tampil |  |
| UC07-02 | 1 | M | Detail pengajuan | Pilih P6 (Budi, Badminton 29 Okt, dua sesi) | Detail menampilkan pemohon, fasilitas, tanggal, sesi, keperluan; tombol Setujui dan Tolak tampil |  |
| UC07-03 | 1 | M | Setujui | Pilih P3, tekan Setujui | P12 (toast hijau); P3 berstatus Disetujui; sesi 10.00-12.00 Futsal A 29 Okt Terisi; P3 hilang dari antrian |  |
| UC07-04 | 1 | O | Sesi sudah terisi (A3) | Setelah UC07-03, pilih P5 (sesi sama), tekan Setujui | P15 tampil, tombol Setujui nonaktif, P5 tetap Menunggu; sesi tidak terkunci ganda |  |
| UC07-05 | 1 | M | Tolak dengan alasan otomatis | Lanjut UC07-04: tekan Tolak; ubah teks alasan; tekan Konfirmasi Penolakan | Kolom alasan terisi P16 dan dapat diedit; P13 (toast merah) tampil; P5 berstatus Ditolak dengan alasan yang diedit; alasan tampil di S04 pengguna (Budi) |  |
| UC07-06 | 1 | M | Alasan penolakan kosong | Pilih P6, tekan Tolak, kosongkan alasan, tekan Konfirmasi Penolakan; lalu isi alasan | P14 tampil dan status tidak berubah; pesan hilang saat isian diubah |  |
| UC07-07 | 1 | M | Persetujuan berlaku untuk semua sesi | Tutup satu dari dua sesi P6 lewat S07, lalu setujui P6 | P15; P6 tidak dapat disetujui dan sesi satunya lagi tidak ikut terkunci |  |
| UC07-08 | 1 | O | Dua persetujuan serentak | Jalankan dua proses yang menyetujui P3 dan P5 bersamaan (MySQL) | Tepat satu berhasil dan satu P15; sesi Terisi oleh tepat satu pengajuan |  |
| UC07-09 | 1 | O | Pengajuan berubah saat diputuskan (A4) | Pengelola membuka P3; pengguna mengajukan pembatalan P3; pengelola menekan Setujui | P30 tampil; status P3 tetap Menunggu Konfirmasi Pembatalan; sesi tidak terkunci |  |
| UC07-10 | 1 | O | Penolakan saat pengajuan berubah | Seperti UC07-09 tetapi pengelola menekan Konfirmasi Penolakan | P30; status tidak tertimpa Ditolak |  |
| UC07-11 | 1 | O | Pengajuan bertanggal lewat sebelum perintah berjalan | Pada data awal (P12 masih Menunggu), panggil Setujui untuk P12 | P15 (sesi lampau tidak tersedia); tidak ada sesi terkunci |  |
| UC07-12 | 2 | M | Antrian kosong | Proses semua pengajuan, buka S08 | P28 tampil |  |
| UC07-13 | 2 | M | Aturan isian alasan | Isi alasan spasi saja; ketik melebihi 255 karakter | Spasi saja menjadi P14; batas 255 karakter mencegah ketikan berlebih |  |
| UC07-14 | 2 | M | Filter fasilitas pada antrian (A5) | Login sebagai pengelola, buka S08, pilih Fasilitas Futsal A | Hanya P3 dan P5 tampil, terlama di atas; P6 (Badminton 1) tidak tampil; detail yang terbuka ditutup; lencana menu samping tetap 3 |  |
| UC07-15 | 2 | M | Filter bertahan setelah keputusan | Dengan filter Futsal A, pilih P3, tekan Setujui | P12 tampil; filter Futsal A tetap aktif dan antrian menampilkan P5 saja; lencana menjadi 2 (P5 dan P6) |  |
| UC07-16 | 2 | M | Hasil kosong karena filter (A6) | Pada antrian berisi P3, P5, dan P6, pilih Fasilitas Tenis 1; lalu pilih Semua Fasilitas | P33 tampil (bukan P28); setelah Semua Fasilitas antrian tampil kembali |  |

### 3.9 UC08 Menangani Pembatalan (S09)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC08-01 | 2 | M | Isi dan urutan daftar | Login sebagai pengelola, buka S09 | Hanya status Menunggu Konfirmasi Pembatalan, waktu permintaan terlama di atas; permintaan bertanggal lewat tidak tampil |  |
| UC08-02 | 2 | M | Detail permintaan | Pilih P7 | Detail menampilkan alasan pembatalan dan status sebelum pembatalan (Disetujui) |  |
| UC08-03 | 2 | M | Konfirmasi pembatalan pengajuan yang sebelumnya Disetujui | Pilih P7, tekan Konfirmasi Pembatalan | P17 tampil; P7 berstatus Dibatalkan; sesi Tenis 31 Okt 08.00-10.00 kembali Tersedia |  |
| UC08-04 | 2 | M | Konfirmasi pengajuan yang sebelumnya Menunggu | Buat dan setujui satu pengajuan lain untuk Futsal A 2 Nov 10.00-12.00 (sesi P8); pilih P8, tekan Konfirmasi Pembatalan | P17; P8 Dibatalkan; sesi tetap Terisi karena dikunci pengajuan lain dan tidak ikut dilepas |  |
| UC08-05 | 2 | O | Pembatalan gagal diproses | Paksa pengecualian setelah status pengajuan diubah dan sebelum sesi dilepas, tekan Konfirmasi; lalu tekan lagi tanpa kegagalan | P18 tampil; status tetap Menunggu Konfirmasi dan sesi tidak berubah sebagian; percobaan kedua berhasil |  |
| UC08-06 | 2 | O | Permintaan sudah diproses | Pengelola membuka P7; perintah terjadwal membatalkan P7 otomatis; pengelola menekan Konfirmasi | P32 tampil; sesi tidak dilepas dua kali; daftar diperbarui |  |
| UC08-07 | 2 | M | Daftar kosong | Proses semua permintaan, buka S09 | P29 tampil |  |
| UC08-08 | 2 | M | Sesi yang dilepas dapat dipesan lagi | Setelah UC08-03, sebagai pengguna buka Tenis 31 Okt | Sesi 08.00-10.00 Tersedia dan dapat dipilih |  |
| UC08-09 | 2 | M | Filter fasilitas pada daftar (A4) | Login sebagai pengelola, buka S09 (P7 Tenis 1 dan P8 Futsal A), pilih Fasilitas Tenis 1 | Hanya P7 tampil; detail yang terbuka ditutup; lencana menu samping tetap 2 |  |
| UC08-10 | 2 | M | Filter bertahan setelah konfirmasi | Dengan filter Futsal A, pilih P8, tekan Konfirmasi Pembatalan | P17; P8 Dibatalkan; filter Futsal A tetap aktif dan P33 tampil karena P7 masih menunggu di fasilitas lain; lencana menjadi 1 |  |
| UC08-11 | 2 | M | Hasil kosong karena filter (A5) | Pada daftar berisi P7 dan P8, pilih Fasilitas Basket A | P33 tampil (bukan P29); setelah Semua Fasilitas daftar tampil kembali |  |

### 3.10 UC09 Melihat Laporan Penggunaan (S10)

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UC09-01 | 2 | M | Pembatasan akses | Sebagai Ani, cari menu Laporan; buka `/reports` langsung | Menu tidak tampil; akses ditolak (403) |  |
| UC09-02 | 2 | M | Hitungan laporan | Pengelola: periode 29-31 Okt, Semua Fasilitas | Hanya Tenis 1 berbaris tidak nol: 1 peminjaman (P4), 1 sesi terisi, penggunaan 8% (1 dari 12 sesi tidak Ditutup); P7 (Menunggu Konfirmasi) tidak dihitung walau sesinya Terisi |  |
| UC09-03 | 2 | M | Sesi Ditutup tidak dihitung | Periode 1-3 Nov, Lapangan Basket A | 1 peminjaman (P11), 4 sesi terisi, penggunaan 50% (4 dari 8; sesi 1 Nov Ditutup tidak dihitung) |  |
| UC09-04 | 2 | M | Periode tidak valid atau kosong | Tanggal awal setelah tanggal akhir; lalu kosongkan tanggal awal | P19 tampil dan laporan tidak disusun |  |
| UC09-05 | 2 | M | Tidak ada data | Periode 1-5 Des | P20 tampil |  |

### 3.11 Perintah terjadwal, konflik, dan konsistensi data

Kasus di bagian ini memakai data awal sebelum perintah terjadwal pernah berjalan, tanggal sistem H = 25 Okt 2026, dan MySQL (SQLite tidak menjalankan penguncian baris).

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| KON-01 | 1 | O | Penolakan otomatis | Jalankan `bookings:expire-overdue` pada H | P12 (Menunggu, H-3) menjadi Ditolak dengan alasan P26; hilang dari antrian S08; terlihat di S06; tidak tampil di S04 |  |
| KON-02 | 1 | O | Pembatalan otomatis (sebelumnya Disetujui) | Setelah KON-01, periksa P13 (Menunggu Konfirmasi, sebelumnya Disetujui, H-2) dan sesinya | P13 Dibatalkan dan sesi Tenis 23 Okt 10.00-12.00 dilepas |  |
| KON-03 | 1 | O | Pembatalan otomatis (sebelumnya Menunggu) | Buat pengajuan status 4 dengan `previous_status` = 1 dan tanggal H-1; jalankan perintah | Status Dibatalkan; tidak ada sesi yang berubah |  |
| KON-04 | 1 | O | Pengajuan Disetujui yang lewat tidak berubah | Setelah perintah berjalan, periksa P1 (Disetujui, H-5) | P1 tetap Disetujui; sesinya tetap Terisi |  |
| KON-05 | 1 | O | Idempoten dan serentak | Jalankan perintah dua kali berurutan, lalu dua proses bersamaan | Hasil sama dengan satu kali; sesi tidak dilepas dua kali; tidak ada pengecualian yang tertinggal |  |
| KON-06 | 1 | O | Sesi lampau tanpa perintah terjadwal | Sebelum perintah berjalan, coba pilih (UC02), setujui (UC07), dan ubah (UC06) sesi bertanggal H-1 | Semua ditolak sebagai tidak tersedia atau tidak dapat dipilih |  |
| KON-07 | 1 | O | Pergantian hari zona Asia/Jakarta | Set waktu 23.59 pada H lalu 00.05 pada H+1; jalankan perintah di keduanya dengan pengajuan Menunggu bertanggal H | Pukul 23.59 H: pengajuan tetap Menunggu; setelah 00.05 H+1: Ditolak |  |
| KON-08 | 1 | O | Tanpa deadlock | Jalankan serentak: UC07 menyetujui P3 dan UC06 menyimpan penutupan sesi yang sama; ulangi 50 kali | Tidak ada deadlock; di tiap putaran hasilnya konsisten (Terisi dan P11, atau Ditutup dan P15) |  |
| KON-09 | 1 | O | UC04 dan UC07 serentak | Jalankan serentak: pengguna membatalkan P3 dan pengelola menyetujui P3; ulangi 50 kali | Tiap putaran: salah satu P30 (batal lebih dulu), atau pembatalan berjalan dengan `previous_status` = 2 (setuju lebih dulu); invarian 1 tetap |  |
| KON-10 | 1 | O | Invarian setelah rangkaian operasi | Jalankan rangkaian acak operasi UC02, UC04, UC06, UC07, UC08, dan perintah terjadwal; periksa data | Tiap sesi Terisi dikunci tepat satu pengajuan (Disetujui atau Menunggu Konfirmasi yang sebelumnya Disetujui); tidak ada pengajuan Menunggu atau Menunggu Konfirmasi bertanggal lewat |  |
| KON-11 | 2 | O | Pengajuan Ditolak atau Dibatalkan tidak menguasai sesi | Periksa sesi dari pengajuan Ditolak (P2, P10) dan Dibatalkan (P9) | Sesi tidak Terisi karena pengajuan itu |  |
| KON-12 | 2 | O | Batas basis data | Coba menyisipkan email ganda, sesi ganda (fasilitas, tanggal, jam sama), dan status di luar rentang langsung ke tabel | Semuanya ditolak oleh batas unik atau CHECK |  |
| KON-13 | 2 | O | Pembuatan sesi otomatis | Atur waktu ke H; jalankan `slots:generate` | Setiap fasilitas punya sesi untuk H sampai H+34; sesi baru (H+22 sampai H+34) berstatus Tersedia, empat sesi per hari sesuai `config/slots.php` |  |
| KON-14 | 2 | O | Pembuatan sesi idempoten | Jalankan `slots:generate` dua kali berturut-turut | Jumlah sesi setelah run kedua sama dengan setelah run pertama; tanpa sesi ganda dan tanpa galat |  |
| KON-15 | 2 | O | Pembuatan sesi tidak menimpa | Sebelum run, pastikan ada sesi Ditutup (Basket A 1 Nov) dan sesi Terisi (Tenis 30 Okt 13.00-15.00); jalankan `slots:generate` | Status Ditutup dan Terisi tidak berubah; hanya sesi yang belum ada ditambahkan |  |

### 3.12 Akses dan keamanan

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| KEM-01 | 1 | O | Pengguna mengakses halaman pengelola | Sebagai Ani, buka `/verification`, `/manage-schedule`, `/reports`, `/cancellation-requests` dan kirim POST ke route pengelola | Semuanya 403 |  |
| KEM-02 | 2 | O | Pengelola mengakses halaman pengguna | Sebagai pengelola, buka `/schedule`, `/booking-status`, `/cancellations`; kirim POST `/bookings` | Semuanya 403 |  |
| KEM-03 | 1 | O | Kepemilikan data | Lihat UC04-07 dan UC05-07 | Pengguna tidak dapat membaca atau mengubah data pengguna lain |  |
| KEM-04 | 2 | O | CSRF | Kirim POST tanpa token | Ditolak (403) |  |
| KEM-05 | 2 | O | Password dan peran | Daftar dengan menyisipkan `role=2` di permintaan; periksa kolom password | Akun tetap berperan pengguna; password tersimpan sebagai hash |  |
| KEM-06 | 2 | M | Keluaran di-escape | Isi keperluan dengan `<script>alert(1)</script>`, buka detail di S08 | Tampil sebagai teks, tidak dijalankan |  |

### 3.13 Antarmuka dan pesan

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| UI-01 | 2 | O | Katalog pesan identik | Bandingkan setiap kunci `p01`-`p33` di `lang/en/messages.php` dengan kolom Teks (en) di `pesan-sistem.md` | Semua teks sama persis |  |
| UI-02 | 2 | M | Kerangka dan lencana pengelola | Login sebagai pengelola; proses satu pengajuan dan satu pembatalan | Menu samping dengan lima menu; lencana Verifikasi dan Pembatalan sama dengan isi antrian dan daftar, berkurang setelah proses, dan hilang saat 0; pengguna memakai bar atas |  |
| UI-03 | 2 | M | Jenis pesan | Periksa warna P02, P06, P10, P12, P17, P24 (hijau), P13 (merah), P11 (kuning) | Warna sesuai jenis di katalog |  |

### 3.14 Non-fungsional

Lingkungan ukur: aplikasi yang sudah dideploy di Railway, data uji sesuai bagian 2 ditambah 100 pengajuan, browser Chrome versi terbaru, koneksi internet biasa (Wi-Fi atau data seluler 4G) tanpa pembatasan kecepatan.

| ID | Pri | J | Yang diuji | Langkah | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|---|---|---|
| NF-01 | 2 | M | Usability | Tiga pengguna uji yang belum pernah memakai sistem menyelesaikan satu pengajuan (UC02) dari login | Setiap pengguna selesai dalam kurang dari 5 menit |  |
| NF-02 | 2 | M | Performance halaman jadwal dan formulir | Ukur 10 kali waktu muat S03 sampai dapat dipakai (event `load` di DevTools) | Semua pengukuran kurang dari 3 detik |  |
| NF-03 | 2 | M | Performance riwayat | Ukur 10 kali waktu muat S06 (100 pengajuan) sebagai Ani dan pengelola | Semua pengukuran kurang dari 3 detik |  |
| NF-04 | 2 | M | Reliability setelah uji konflik | Setelah semua kasus KON, periksa basis data | Tidak ada dua pengajuan menguasai satu sesi |  |
| NF-05 | 2 | M | Deployment dari nol | Di database kosong jalankan migrasi dan seeder; buka aplikasi di Railway; tunggu service cron berjalan satu kali | Data awal sesuai bagian 2; aplikasi dapat dibuka; cron berjalan dan keluar sendiri tanpa kesalahan; data kedaluwarsa berubah sesuai KON-01 dan KON-02 |  |

## 4. Keterlacakan

Tag di kolom referensi memakai: `P..` pesan, `AI..` aturan integritas ([model-data.md](model-data.md)), `INV..` invarian ([state-diagram.md](state-diagram.md)), `UCxx-A..` alur alternatif, `K..` keputusan antarmuka ([wireframe.md](wireframe.md), bagian 1.6), `D1` dan `D3` keputusan pemeriksaan kriteria.

### 4.1 Pesan P01-P33

| Tag | Kasus uji |
|---|---|
| P01 | UC01-06 |
| P02 | UC02-02 |
| P03 | UC02-05, UC02-06 |
| P04 | UC02-07, UC02-08 |
| P05 | UC03-04 |
| P06 | UC04-02 |
| P07 | UC04-03 |
| P08 | UC05-04 |
| P09 | UC05-05 |
| P10 | UC06-04 |
| P11 | UC06-05 |
| P12 | UC07-03 |
| P13 | UC07-05 |
| P14 | UC07-06, UC07-13 |
| P15 | UC07-04, UC07-07, UC07-11 |
| P16 | UC07-05 |
| P17 | UC08-03, UC08-04 |
| P18 | UC08-05 |
| P19 | UC09-04, UC05-10 |
| P20 | UC09-05 |
| P21 | AUT-03 |
| P22 | AUT-05 |
| P23 | AUT-06 |
| P24 | AUT-04 |
| P25 | UC01-05, UC06-02 |
| P26 | UC05-02, KON-01 |
| P27 | UC04-04 |
| P28 | UC07-12 |
| P29 | UC08-07 |
| P30 | UC07-09, UC07-10, KON-09 |
| P31 | UC04-05, UC04-06 |
| P32 | UC08-06 |
| P33 | UC05-11, UC07-16, UC08-11 |

### 4.2 Aturan integritas 1-13

| Tag | Kasus uji |
|---|---|
| AI1 (nilai terbatas) | KON-12 |
| AI2 (email unik) | AUT-05, KON-12 |
| AI3 (sesi tidak dobel) | KON-12 |
| AI4 (alasan wajib) | UC04-02, UC07-05 |
| AI5 (pengajuan menunggu tidak mengunci) | UC02-02, UC07-03 |
| AI6 (tidak dua pengajuan menguasai satu sesi) | UC06-05, UC07-04, UC07-08 |
| AI7 (pembatalan melepas sesi) | UC08-03, KON-02 |
| AI8 (Terisi tidak dapat ditutup) | UC06-05, UC06-06 |
| AI9 (identitas wajib) | AUT-04, AUT-06 |
| AI10 (1 sampai 4 sesi) | UC02-09 |
| AI11 (penolakan dan pembatalan otomatis) | KON-01, KON-02, KON-03, KON-04, KON-05, KON-07 |
| AI12 (sesi lampau tidak tersedia) | UC01-04, UC02-08, UC06-01, UC06-06, UC07-11, KON-06 |
| AI13 (status diperiksa saat diproses) | UC04-05, UC04-06, UC07-09, UC07-10, UC08-06, KON-09 |

### 4.3 Invarian 1-6

| Tag | Kasus uji |
|---|---|
| INV1 (Terisi dikunci tepat satu pengajuan) | UC07-03, UC08-03, KON-09, KON-10 |
| INV2 (Menunggu tidak mengunci) | UC02-02, UC02-11 |
| INV3 (tidak dua pengajuan menguasai satu sesi) | UC07-04, UC07-07, UC07-08, KON-08, NF-04 |
| INV4 (Terisi tidak dapat ditutup) | UC06-06 |
| INV5 (Ditolak dan Dibatalkan tidak menguasai sesi) | KON-11 |
| INV6 (tidak ada pengajuan kedaluwarsa) | KON-01, KON-02, KON-10 |

### 4.4 Alur alternatif

| Tag | Kasus uji |
|---|---|
| UC01-A1 | UC01-06 |
| UC01-A2 | UC01-05 |
| UC02-A1 | UC02-05 |
| UC02-A2 | UC02-07 |
| UC03-A1 | UC03-04 |
| UC03-A2 | UC03-03 |
| UC04-A1 | UC04-03 |
| UC04-A2 | UC04-05 |
| UC05-A1 | UC05-04 |
| UC05-A2 | UC05-05 |
| UC05-A3 | AUT-07, UC05-06 |
| UC05-A4 | UC05-08, UC05-09 |
| UC05-A5 | UC05-10 |
| UC05-A6 | UC05-11 |
| UC06-A1 | UC06-05 |
| UC07-A1 | UC07-05 |
| UC07-A2 | UC07-06 |
| UC07-A3 | UC07-04, UC07-05 |
| UC07-A4 | UC07-09, UC07-10 |
| UC07-A5 | UC07-14, UC07-15 |
| UC07-A6 | UC07-16 |
| UC08-A1 | UC08-04 |
| UC08-A2 | UC08-05 |
| UC08-A3 | UC08-06 |
| UC08-A4 | UC08-09, UC08-10 |
| UC08-A5 | UC08-11 |
| UC09-A1 | UC09-04 |
| UC09-A2 | UC09-05 |

## 5. Jadwal pelaksanaan

| Tahap | Kasus uji yang dijalankan |
|---|---|
| Akhir inkremen 1 (fondasi, autentikasi, UC01) | AUT-01, AUT-02, AUT-03, AUT-04, AUT-05, AUT-06, AUT-07, AUT-08, UC01-01, UC01-02, UC01-03, UC01-04, UC01-05, UC01-06 |
| Akhir inkremen 2 (UC02, UC07, UC03) | UC02-01, UC02-02, UC02-03, UC02-04, UC02-05, UC02-06, UC02-07, UC02-08, UC02-09, UC02-10, UC02-11, UC03-01, UC03-02, UC03-03, UC03-04, UC03-05, UC03-06, UC07-01, UC07-02, UC07-03, UC07-04, UC07-05, UC07-06, UC07-07, UC07-08, UC07-09, UC07-10, UC07-11, UC07-12, UC07-13, UC07-14, UC07-15, UC07-16 |
| Akhir inkremen 3 (UC04, UC08, UC05, UC06, UC09) | UC04-01, UC04-02, UC04-03, UC04-04, UC04-05, UC04-06, UC04-07, UC05-01, UC05-02, UC05-03, UC05-04, UC05-05, UC05-06, UC05-07, UC06-01, UC06-02, UC06-03, UC06-04, UC06-05, UC06-06, UC06-07, UC06-08, UC08-01, UC08-02, UC08-03, UC08-04, UC08-05, UC08-06, UC08-07, UC08-08, UC09-01, UC09-02, UC09-03, UC09-04, UC09-05, KON-01, KON-02, KON-03, KON-04, KON-05, KON-06, KON-07, KON-08, KON-09, KON-10, KON-11, KON-12, KON-13, KON-14, KON-15, KEM-01, KEM-02, KEM-03, KEM-04, KEM-05, KEM-06, UI-01, UI-02, UI-03, UC05-08, UC05-09, UC05-10, UC05-11, UC08-09, UC08-10, UC08-11 |
| Setelah deployment di Railway | NF-01, NF-02, NF-03, NF-04, NF-05 |

Kasus yang menggabungkan beberapa use case (KON, KEM-01 dan KEM-03) dijalankan di akhir inkremen tempat use case terakhir yang dilibatkan selesai.

## 6. Pencatatan hasil

- Kolom **Hasil aktual** diisi dengan "Lulus" atau "Gagal", tanggal, penguji, dan catatan singkat (untuk Gagal: apa yang terjadi).
- Kasus yang gagal diperbaiki dan diuji ulang; hasil terakhir yang dipakai di laporan, dan kegagalan awal tetap dicatat sebagai bahan evaluasi.
- Kasus NF bergantung pada deployment, jadi hasilnya diisi setelah aplikasi tersedia di Railway.

| Ringkasan | Jumlah |
|---|---|
| Total kasus uji | 118 |
| Prioritas 1 | 50 |
| Prioritas 2 | 68 |
| Otomatis (O) | 40 |
| Manual (M) | 78 |
| Lulus | |
| Gagal | |
