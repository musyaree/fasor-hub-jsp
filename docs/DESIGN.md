# DESIGN.md: arah visual Fasor Hub

> **Status: bagian 1-15 disetujui pemilik produk (21 November 2026). Bagian 16 (revisi immersive) disetujui pemilik produk pada 21 November 2026 dan menggantikan bagian 1, 4.1, dan 9 di bagian yang bertentangan.** **Revisi 21 November 2026: motif court line dan denah lapangan dihapus atas keputusan pemilik produk; bagian 3, 16.2, dan 16.5 diubah sesuai itu.** **Pembaruan setelah semua layar Stitch selesai diaudit: bagian 7, 8, dan 16.2 disesuaikan dengan hasil akhir, dan bagian 17 (hasil layar dan aturan implementasi) ditambahkan. Butir terbuka 14.5 diberi usulan keputusan yang menunggu persetujuan pemilik produk.** Butir di bagian 14 masih perlu diverifikasi. Dokumen ini mengikuti filter antislop (mode: selama pengerjaan).

Dokumen ini mengatur **tampilan**. Alur, isi layar, dan pesan tetap mengikuti [wireframe.md](03-desain/wireframe.md) dan [pesan-sistem.md](03-desain/pesan-sistem.md). Nama kode dan teks antarmuka berbahasa Inggris, sesuai [penamaan.md](03-desain/penamaan.md). Penjelasan di dokumen ini berbahasa Indonesia.

## 1. Design Read dan dial

> Reading this as: sistem peminjaman fasilitas olahraga kampus untuk mahasiswa, dosen, UKM, dan pengelola, bergaya terang yang bersih, **ENERGY 2 / RHYTHM 2 / MOTION 2**.

| Dial | Nilai | Arti di Fasor Hub |
|---|---|---|
| ENERGY | 2 | Ada identitas yang terasa (satu aksen, satu permukaan gelap di sisi pengelola), tetapi kalender tetap yang paling menonjol |
| RHYTHM | 2 | Dua kerangka halaman konsisten. Variasi komposisi hanya di Login/Registrasi, kepala halaman, dan layar dua panel (S08, S09) |
| MOTION | 2 | Gerak memandu perhatian pada momen tertentu (bagian 9), tanpa animasi dekoratif |

## 2. Yang dipertahankan dari prototipe lama

Hanya dua hal, atas keputusan pemilik produk:

1. **Peletakan:** bar atas untuk Borrower, menu samping 230px untuk Manager, kalender grid 7 kolom, panel sesi di sisi kalender, layout dua panel di verifikasi.
2. **Makna warna status**.

Gaya lain (warna merek, font, sudut, bayangan, ikon, tombol, tabel, kartu) dibuat baru.

## 3. Motif identitas: tidak ada

**Keputusan pemilik produk (21 November 2026):** tidak memakai court line (garis dengan lekukan setengah lingkaran) dan tidak memakai gambar denah lapangan di layar mana pun. Usulan awal tentang motif garis lapangan di bagian ini dan di bagian 16.2 dicabut.

**Pengganti:** belum ada. Tampilan bertumpu pada tiga hal: permukaan gelap `ink-deep` di band kepala halaman, panel Login, dan menu samping Manager; huruf judul besar; satu aksen biru. Ciri lain yang konsisten di semua layar adalah lima status yang selalu berpenanda ganda (ikon, label, dan bentuk tepi) dan penanda pilihan berupa garis dalam 2px beserta label "Selected".

**Catatan antislop (R-20, Blok 3):** filter menghendaki satu motif identitas yang spesifik dan berulang. Tanpa motif, desain berisiko terasa generik bila nama Fasor Hub diganti. Ini ditandai sebagai **butir terbuka** di bagian 14. Pemilik produk dapat memilih motif pengganti nanti atau menerima desain tanpa motif. **Usulan (belum disetujui):** terima desain tanpa motif. Setelah semua layar dibuat dan diaudit, ciri khasnya cukup terbaca dari band gelap, sidebar, dan status berpenanda ganda; menambah motif sekarang berisiko mengembalikan dekorasi yang sudah dicabut.

## 4. Warna

### 4.1 Prinsip

- Satu warna merek (biru) dipakai hemat: tombol utama, tautan, fokus, tanggal terpilih. Tidak pernah untuk status.
- Satu permukaan gelap (`ink`) hanya untuk menu samping Manager dan panel kiri Login/Registrasi. Ini bukan mode gelap (tidak ada pengalih tema), tetapi pembeda peran dan sumber variasi irama.
- Lima warna status bersifat **semantik**, bukan bagian palet merek, jadi tidak dihitung di batas R-29 (2-3 warna inti + 1 aksen). Ini tafsiran tim, ditulis sebagai alasan.

### 4.2 Token inti

| Token | Heks | Fungsi |
|---|---|---|
| `--color-brand` | #1F5FBF | Tombol utama, tautan, cincin fokus, tanggal terpilih |
| `--color-brand-on-ink` | #8DB4F0 | Aksen di atas `ink` (ikon menu aktif, teks aksen pada panel gelap). **Biru merek tidak dipakai di atas `ink`** |
| `--color-brand-fg` | #FFFFFF | Teks pada tombol utama |
| `--color-bg` | #F4F7FB | Latar halaman |
| `--color-surface` | #FFFFFF | Kartu, tabel, panel, bar atas |
| `--color-ink` | #142033 | Teks utama; juga permukaan gelap menu samping dan panel Login |
| `--color-text-muted` | #4A5A70 | Teks sekunder dan label |
| `--color-border` | #7A8AA2 | Tepi kolom isian dan kontrol |
| `--color-hairline` | #D5DDE8 | Tepi kartu dan pemisah tabel |
| `--color-brand-tint` | #E6EEFA | Latar menu aktif (terang) dan baris terpilih |

### 4.3 Token status

| Token | Heks | Dipakai untuk |
|---|---|---|
| `--color-status-available` | #1E8E3E | Tepi dan ikon Available, Approved |
| `--color-status-available-bg` / `-fg` | #DDF3E4 / #0B4F22 | Isian muda dan teks di atasnya |
| `--color-status-occupied` | #C62F2F | Tepi dan ikon Occupied, Rejected |
| `--color-status-occupied-bg` / `-fg` | #FBE0E0 / #7A1414 | Isian muda dan teks |
| `--color-status-closed` | #6B7280 | Tepi dan ikon Closed, Cancelled |
| `--color-status-closed-bg` / `-fg` | #E8EAEE / #2F3640 | Isian muda dan teks |
| `--color-status-pending` | #A87B00 | Tepi dan ikon PendingVerification, tanggal Partial |
| `--color-status-pending-bg` / `-fg` | #FFF1BF / #5C4300 | Isian muda dan teks |
| `--color-status-cancelling` | #C25E00 | Tepi dan ikon PendingCancellation |
| `--color-status-cancelling-bg` / `-fg` | #FFE3C7 / #6E3200 | Isian muda dan teks |

### 4.4 Kontras terukur

Dihitung dengan rumus WCAG (skrip, bukan uji pengguna).

| Pasangan | Rasio | Syarat |
|---|---|---|
| `ink` pada `bg` / `surface` | 15,22 / 16,35 | 4,5 |
| Putih pada `brand` (tombol utama) | 6,09 | 4,5 |
| `brand` pada `bg` / `surface` (tautan) | 5,67 / 6,09 | 4,5 |
| `brand` pada `brand-tint` | 5,21 | 4,5 |
| Putih pada `ink` (menu samping) | 16,35 | 4,5 |
| Putih pada `ink-raised` (item menu Manager aktif) | 14,21 | 4,5 |
| #A9B6C9 (teks sekunder di `ink`) pada `ink` | 7,96 | 4,5 |
| `brand-on-ink` pada `ink` | 7,72 | 4,5 |
| **`brand` pada `ink`** | **2,68** | **Gagal, jangan dipakai** |
| Teks status pada isian mudanya (hijau/merah/abu/kuning/oranye) | 8,36 / 8,69 / 10,12 / 8,23 / 8,05 | 4,5 |
| Tepi status pada `bg` | 3,91 / 5,08 / 4,50 / 3,55 / 3,99 | 3 |

## 5. Penanda status (tidak hanya warna)

Simulasi buta warna memberi jarak warna kuning dan oranye hanya sekitar 2 pada deuteranopia. Karena itu setiap status memakai **label teks + ikon + bentuk tepi**.

| Status | Ikon | Bentuk tepi | Label |
|---|---|---|---|
| Available, Approved | `check_circle` | Solid 1px | Available / Approved |
| Occupied, Rejected | `cancel` | Solid 1px; di sel kalender ditambah garis diagonal halus | Occupied / Rejected |
| Closed, Cancelled | `block` | Solid 1px | Closed / Cancelled |
| PendingVerification | `hourglass_top` | **Putus-putus** 1,5px | Pending Verification |
| PendingCancellation | `event_busy` | **Ganda** 3px (double) | Pending Cancellation |

**Perubahan dari usulan awal:** bilah kiri tebal untuk PendingCancellation dibuang karena termasuk pola "Colored Left Stripe" antislop. Pembedanya sekarang tepi ganda. Menu aktif di sidebar juga tanpa bilah kiri (bagian 7).

Kalender bulanan: tanggal Partial memakai `timelapse`. Sel menampilkan angka tanggal, ikon, dan teks pendek. Legenda wajib ada di bawah kalender (wireframe 1.3). Toast memakai `check_circle`, `cancel`, `warning` sesuai jenisnya.

**Belum terverifikasi:** apakah tepi putus-putus dan tepi ganda tetap terbedakan pada ukuran sel kecil dan pada simulasi buta warna. Diperiksa di tiap layar hasil.

## 6. Tipografi

| Aspek | Ketetapan |
|---|---|
| Font | **IBM Plex Sans** (Google Fonts), bobot 400, 500, 600, 700. Satu keluarga saja |
| Alasan (R-06, R-31) | Aplikasi ini penuh angka sejajar: tanggal, jam sesi, jumlah peserta, lencana, laporan. IBM Plex Sans punya angka tabular dan terasa institusional tanpa jadi font bawaan AI. Alternatif Inter ditolak karena tidak punya alasan khusus |
| Angka | `font-variant-numeric: tabular-nums` di kalender, tabel, lencana, jam sesi |
| Skala | 12/16 keterangan dan lencana; 14/20 isi tabel dan label; 16/24 isi utama; 20/28 judul kartu (600); 24/32 judul halaman (700); 30/36 judul login (700) |
| Larangan | Tanpa teks kapital berjarak lebar sebagai judul, tanpa font monospace untuk gaya |

**Belum terverifikasi:** dukungan `tnum` pada berkas IBM Plex Sans dari Google Fonts dan pada pembuat desain Stitch. Bila tidak ada, fallback ke font lain dengan angka tabular dan dokumen ini diperbarui.

## 7. Bentuk, jarak, dan komponen

| Elemen | Ketetapan | Alasan |
|---|---|---|
| Radius | 8px tombol dan kolom isian; 12px kartu, panel, dialog; 6px sel kalender; pil 9999px **hanya** lencana status | Variasi radius menandai tingkat hierarki (R-11) |
| Jarak | Kelipatan 4px; 8/12/16 antarelemen; padding kartu 20; jarak bagian 24 | Irama konsisten |
| Bayangan | Tidak ada pada kartu dan tabel (tepi 1px `hairline`). Hanya toast, dialog, dropdown: `0 4px 16px rgb(20 32 51 / 0.14)` | Bayangan hanya penanda elevasi (R-12) |
| Tombol utama | Isian `brand`, teks putih 600, tinggi 40px, fokus cincin 2px offset 2px, nonaktif #C9D3E0 dengan teks `text-muted` | Satu aksi utama per layar |
| Tombol sekunder | Putih, tepi 1px `border`, teks `ink`. Reject: tepi dan teks #C62F2F, bukan isian merah penuh | Aksi merusak tidak mendominasi |
| Tanpa panah dekoratif | Tombol tidak memakai `→` atau `↗` | R-08 |
| Lencana | Hanya untuk status nyata dan jumlah pada menu. Tanpa "New", "Beta", atau sejenisnya | R-09 |
| Menu Borrower | Bar atas 56px putih; item aktif berlatar `brand-tint`, teks `brand` | Tanpa bilah kiri |
| Menu Manager | Menu samping 230px berlatar `ink-deep` dengan nama aplikasi, keterangan "Manager", ikon Material Symbols di setiap item; item aktif berlatar `ink-raised`, teks putih, garis dalam 2px `line-on-ink`, ikon `brand-on-ink`; lencana jumlah berisian `brand-on-ink` dengan teks `ink-deep`; di bawah 800px menjadi bar atas gelap dengan daftar menu biasa | Permukaan gelap sebagai pembeda peran; garis dalam sebagai penanda non-warna |
| Tabel | Header 14/20 600 pada `bg`, baris 48px, pemisah 1px, tanpa zebra, kolom Status berisi lencana pil (ikon + label), angka rata kanan | Mudah dipindai |
| Sel kalender | Min 72px; isian muda status; tepi status; angka tanggal kiri atas; ikon kanan bawah; teks pendek 12/16. Dipilih: garis 2px `brand` dengan jarak 2px, tanpa glow. Lampau/tanpa sesi: `bg`, teks muted, tidak dapat dipilih. Hari ini: titik aksen di samping angka | Status terbaca sekilas tanpa warna saja |
| Pilihan baris dan tanggal | Baris atau kartu terpilih: garis dalam 2px `brand`, dan tombol Select berubah menjadi tombol terisi berlabel "Selected" (`aria-pressed`). Penanda tidak mengandalkan warna saja | Tombol nyata, bukan baris yang dapat diklik |
| Ikon | Material Symbols Outlined, 20px dalam teks, 24px di navigasi. Ikon status di bagian 5. Ikon jenis fasilitas hanya bila ada glyph yang relevan; bila tidak ada, teks saja | R-04 |

## 8. Komposisi per layar (RHYTHM 2)

Peletakan mengikuti wireframe. Satu titik fokus per layar.

| Layar | Titik fokus | Catatan komposisi |
|---|---|---|
| S01 Login, S02 Registrasi | Formulir | Dua kolom: panel kiri `ink-deep` dengan nama aplikasi dan judul besar; formulir di kanan. Pada ponsel panel kiri menjadi pita atas |
| S03 Jadwal dan Peminjaman | Kalender bulanan | Kalender lebar, panel sesi dan formulir di sisi atau bawah. Kepala halaman berupa band gelap (bagian 16.2) |
| S04 Status, S05 Pembatalan, S06 Riwayat | Daftar atau tabel | Tabel dengan lencana status di atas 800px, kartu bertumpuk di bawahnya; kartu detail di bawah daftar. S06 punya baris filter (bagian 17.2). Keadaan kosong berupa satu kalimat dari katalog pesan |
| S07 Kelola Jadwal | Kalender dan tombol Simpan | Sama dengan S03 tanpa formulir pengajuan |
| S08 Verifikasi, S09 Pembatalan | Antrean dan panel detail | Dua kartu bertumpuk (antrean di atas, detail dan aksi di bawah) dengan filter fasilitas di kartu antrean. Susunan dua kolom dari usulan awal tidak dipakai di hasil akhir agar tabel tetap lebar |
| S10 Laporan | Tabel penggunaan per fasilitas | Tanpa grafik hiasan. Hanya angka dari data nyata, tidak ada statistik karangan |

## 9. Gerak (MOTION 2)

Semua gerak memakai CSS transisi dan JavaScript biasa, tanpa paket. Setiap gerak punya tujuan.

| Momen | Gerak | Durasi | Tujuan |
|---|---|---|---|
| Pindah bulan | Grid bergeser searah panah | 180 ms | Menunjukkan maju atau mundur |
| Pilih tanggal | Panel sesi muncul dengan fade dan geser kecil | 160 ms | Panel terasa berasal dari tanggal terpilih |
| Pilih sesi | Sel terpilih terisi, ringkasan ikut berubah | 120 ms | Umpan balik pilihan tercatat |
| Toast | Masuk dari kanan, keluar sendiri setelah jeda baca | 200 ms masuk | Pesan tidak mengejutkan atau menutup konten |
| Dialog | Lapisan gelap 40% memudar, dialog naik 8px | 180 ms | Perpindahan fokus jelas; Escape menutup |
| Setujui atau Tolak | Lencana berganti dengan kilatan singkat, baris keluar dari antrean | 220 ms | Hasil tindakan terlihat tanpa muat ulang |
| Skeleton muat | Berdenyut pelan | 1,2 s per siklus | Menandakan data sedang diambil |

- Kurva `ease-out` untuk masuk, `ease-in` untuk keluar. Tidak ada animasi pantul, melayang, atau fade-up serentak.
- **`prefers-reduced-motion`:** semua transisi dimatikan atau jadi instan. Fungsi tetap berjalan.
- Stitch hanya menghasilkan layar statis, jadi gerak diterapkan dan dinilai saat membangun Blade.

## 10. Keadaan (R-27)

Setiap layar yang menampilkan data punya tiga keadaan:

| Keadaan | Tampilan |
|---|---|
| Empty | Satu kalimat dari katalog pesan, tanpa ilustrasi |
| Loading | Skeleton pada bentuk isi (baris tabel, sel kalender) |
| Error | Pesan dari katalog dengan ikon `warning`/`cancel` dan aksi coba lagi bila relevan |

## 11. Konten

- Semua teks UI berasal dari `lang/en/messages.php` dan berkas bahasa lain. Tidak ada teks buatan baru di desain.
- Tidak ada tanda pisah panjang (`—`) di teks UI (R-02). Gunakan koma, titik, titik dua, atau kurung.
- Tombol berbahasa spesifik, misalnya "Submit booking request", "Approve", "Reject". Bukan "Get Started" atau "Learn More".
- **Tidak ada** bagian pemasaran: testimoni, statistik, logo klien, FAQ, harga, "How it works". Produk ini sistem internal, bukan landing page.
- Aset belum final: logo `[LOGO]` (teks "Fasor Hub" saja), foto fasilitas tidak ada, avatar pakai inisial. Tidak ada orang atau data karangan (R-23, R-38).
- Menu hanya berisi layar yang ada (R-24).

## 12. Daftar tolak (turunan antislop)

Tidak dipakai di Fasor Hub: gradien merek, glassmorphism, glow, bayangan di semua komponen, latar kisi atau titik, bilah warna di tepi kiri, panah dekoratif, lencana kapsul "AI Powered" dan sejenisnya, ikon generik (sparkle, bintang, petir), ilustrasi Undraw/Storyset/blob 3D, denah lapangan dan court line (dicabut pemilik produk), tata letak bento, jendela terminal palsu, mode gelap sebagai tema.

## 13. Alasan tertulis (R-31)

| Keputusan | Alasan satu baris |
|---|---|
| Aksen biru #1F5FBF | Berbeda jauh dari lima warna status, sehingga status tidak tertukar dengan merek (jarak warna terkecil terhadap tepi status 50,3) |
| Permukaan `ink` untuk Manager | Membedakan peran dan memberi variasi irama tanpa menambah warna |
| Tanpa motif garis lapangan | Keputusan pemilik produk; pengganti belum ada (bagian 3) |
| IBM Plex Sans | Angka tabular untuk kalender dan tabel |
| Tepi putus-putus dan ganda | Membedakan dua status pending bagi pengguna buta warna |
| Tanpa bayangan pada kartu | Hierarki dari tepi dan jarak; bayangan disimpan untuk elemen melayang |
| Radius 6/8/12 | Hierarki ukuran; pil hanya untuk lencana status |

## 14. Item belum terverifikasi

1. Dukungan `tnum` pada IBM Plex Sans (Google Fonts dan Stitch).
2. Nama glyph Material Symbols yang dipakai (`hourglass_top`, `event_busy`, `timelapse`, `block`, dan ikon jenis fasilitas), termasuk apakah ada glyph yang cocok untuk badminton.
3. Keterbedaan tepi putus-putus dan ganda pada ukuran sel kecil.
4. Seluruh angka kontras dihitung dengan rumus, bukan uji pengguna.
5. **Butir terbuka:** tidak ada motif identitas (bagian 3) dan momen MOTION 3 hilang (bagian 16.5). Perlu keputusan pemilik produk: pilih pengganti atau terima. **Usulan: terima, dan nilai ulang setelah halaman Blade pertama berjalan.**
6. Perbedaan kecil antara layar Stitch dan aturan dokumen ini (bagian 17.3). Diselesaikan saat membuat Blade, bukan dengan mengedit Stitch lagi.

## 15. Pemeriksaan per layar

Sebelum sebuah layar dianggap selesai, periksa terhadap Delivery Gate antislop dan daftar ini:

- Teks UI berbahasa Inggris dan berasal dari katalog.
- Lima status lengkap dengan ikon, bentuk tepi, dan label.
- Pesan P01-P33 yang relevan punya tempat tampil.
- Muat di lebar ponsel tanpa luapan horizontal dan target sentuh minimal 44px.
- Empty, loading, dan error tersedia.
- Fokus papan ketik terlihat dan semua kontrol dapat dijangkau dengan Tab.
- Tanpa tanda pisah panjang, tanpa data karangan, tanpa tautan menu ke halaman yang tidak ada.

## 16. Revisi immersive (disetujui)

Permintaan pemilik produk: tampilan terlalu sederhana, ingin lebih immersive lewat **visual, gerak, serta warna dan desain**. Pilihan dial: **selektif**.

### 16.1 Dial baru

> Reading this as: sistem peminjaman fasilitas olahraga kampus, dengan zona masuk dan kepala halaman yang imersif lewat permukaan gelap dan huruf besar, dan zona kerja yang tetap tenang, **ENERGY 3 di zona imersif / 2 di zona kerja, RHYTHM 3 / 2, MOTION 3 pada transisi utama / 2 di tempat lain**.

| Zona | ENERGY | RHYTHM | MOTION | Layar |
|---|---|---|---|---|
| Imersif | 3 | 3 | 3 | Login, Registrasi, band kepala halaman di setiap layar |
| Kerja | 2 | 2 | 2 | Kalender, daftar sesi, formulir, tabel (S03 sampai S10) |

**Alasan (R-31):** status di kalender dan tabel harus terbaca sekilas dan lolos kontras, jadi di sana dekorasi dibatasi. Suasana dibuat di sekeliling area kerja.

### 16.2 Band kepala halaman

Setiap layar berkerangka memulai dengan satu **band** lebar berlatar `ink-deep` setinggi sekitar 140px (Borrower) atau 120px (Manager):

- Judul halaman besar (40/44 di atas 800px, 32/38 di bawahnya, 700) dan satu kalimat deskripsi fungsi halaman (tabel di bawah). Keputusan pemilik produk 21 November 2026: subjudul adalah deskripsi, bukan data.
- Garis tepi bawah 2px `line-on-ink` sebagai pemisah ke area kerja (pemisah fungsional, bukan motif).
- Tanpa denah lapangan dan tanpa court line.

Subjudul band (satu kalimat, kurang dari 12 kata, diawali kata kerja, huruf biasa, diakhiri titik, tanpa tanda pisah panjang dan tanpa kata promosi):

| Halaman | Subjudul |
|---|---|
| Schedule & Booking | Pick a facility and date, choose your sessions, and send a booking request. |
| Booking Status | Follow the status of your upcoming booking requests. |
| Cancellation | Request to cancel a booking that is pending or approved. |
| History | See all your past and current bookings. (varian Manager: See all bookings across every facility.) |
| Manage Schedule | Open or close time slots for each facility and date. |
| Verification | Review booking requests and approve or reject them. |
| Cancellation Requests | Confirm cancellation requests from borrowers. |
| Reports | See how each facility was used over a period. |

Jumlah data (misalnya "4 bookings") tidak lagi di band. Bila diperlukan, tampil sebagai teks kecil di atas tabel. Tidak ada angka karangan.

**Alasan:** satu tempat yang kuat untuk suasana lewat blok gelap dan huruf besar, tanpa mengganggu kalender.

### 16.3 Warna imersif

Tidak ada warna matang baru. Imersif dicapai lewat **lapisan nada** dalam keluarga biru-ink yang sudah ada:

| Token | Heks | Fungsi |
|---|---|---|
| `--color-ink-deep` | #0B1426 | Band kepala halaman, panel Login, latar menu samping Manager |
| `--color-ink` | #142033 | Teks utama dan permukaan gelap sekunder |
| `--color-ink-raised` | #1B2B45 | Kartu dan panel di atas `ink-deep` |
| `--color-line-on-ink` | #8DB4F0 | Garis pemisah band dan teks aksen di zona gelap (7,72:1 pada `ink`) |

Kontras terhitung (rumus WCAG): putih pada `ink-deep` 18,38:1 dan pada `ink-raised` 14,21:1; teks sekunder #A9B6C9 pada `ink-deep` 8,95:1 dan pada `ink-raised` 6,92:1; `line-on-ink` pada `ink-deep` 8,68:1 dan pada `ink-raised` 6,71:1.

**Mengapa tidak menambah warna merek baru:** lima warna status memakai hijau, merah, kuning, oranye, dan abu. Hue bebas hanya biru. Ungu ditolak (R-01: skema ungu-hitam adalah bawaan AI). Teal ditolak karena terlalu dekat dengan hijau Available bagi pengguna buta warna merah-hijau. Warna per jenis olahraga ditolak karena melanggar R-29 dan bisa bertabrakan dengan status.

### 16.4 Tipografi

Tetap IBM Plex Sans. Di band: judul 40/44 (700), angka besar tabular untuk jumlah nyata. Di Login: judul 48/52. Area kerja tetap pada skala bagian 6.

### 16.5 Gerak MOTION 3 pada transisi utama

Menambah, bukan mengganti, tabel bagian 9. Semua mematuhi `prefers-reduced-motion`.

| Momen | Gerak | Durasi | Tujuan |
|---|---|---|---|
| Pindah halaman | Band tetap, area kerja memudar dan bergeser 8px | 220 ms | Menjaga konteks kerangka |
| Indikator menu aktif | Latar item aktif bergeser ke item baru | 200 ms | Posisi pengguna jelas |
| Pindah bulan, pilih tanggal, pilih sesi, toast, dialog, Approve/Reject | Seperti bagian 9 | | |

Tidak ada animasi serentak pada banyak kartu, tidak ada parallax, tidak ada gerak yang berulang tanpa henti.

**Catatan:** dengan animasi menggambar garis denah dicabut, momen khas MOTION 3 hilang. Gerak yang tersisa setara MOTION 2. Dial MOTION 3 pada transisi utama tidak tercapai sampai ada pengganti, jadi dianggap MOTION 2 sampai diputuskan lain.

### 16.6 Risiko revisi

1. Band menambah tinggi halaman; kalender turun di layar kecil. Band dikecilkan di bawah 800px, dan kalender tetap di atas lipatan.
2. Tanpa motif dan tanpa denah, band dan panel Login hanya blok gelap berisi teks, sehingga kesan imersif lebih lemah dari yang disetujui di bagian 16.1. Setelah semua layar Stitch dibuat, penilaian ini tetap berlaku; usulan keputusannya ada di bagian 3 dan butir 14.5. Dinilai ulang setelah halaman Blade pertama berjalan.
3. Zona gelap yang lebih luas bukan mode gelap. Pengalih tema tetap tidak ada (R-34).

## 17. Hasil layar dan aturan implementasi

Bagian ini mencatat hasil akhir setelah semua layar dibuat di [Stitch](https://stitch.google.com/projects/14085482433280035910) (proyek privat; hanya akun yang diundang yang bisa membukanya) dan diaudit, serta aturan yang terbukti perlu dipegang saat membuat Blade. Layar Stitch adalah **acuan visual**; sumber kebenaran tetap [wireframe.md](03-desain/wireframe.md), [pesan-sistem.md](03-desain/pesan-sistem.md), dan dokumen ini. Bila ada perbedaan, dokumen yang berlaku. Proyek Stitch menyimpan beberapa versi tiap layar karena setiap edit menambah layar baru; yang final adalah yang terbaru pada papan, dengan nama pada kolom "Layar Stitch" di bawah.

### 17.1 Layar dan keadaan

| Layar | Kerangka | Keadaan yang dirancang | Layar Stitch |
|---|---|---|---|
| S01 Login, S02 Registrasi | Dua panel (panel kiri `ink-deep`) | Awal, kesalahan validasi (P21, P22, P23) | Login, Register |
| S03 Jadwal dan Peminjaman | Bar atas Borrower | Awal, tanggal terpilih, loading, tanpa sesi (P01), sesi tidak tersedia (P04), data tidak lengkap (P03), dialog persetujuan, toast P02 | Schedule & Booking |
| S04 Status Pengajuan | Bar atas Borrower | Terisi, loading, kosong (P05) | Booking Status |
| S05 Pembatalan | Bar atas Borrower | Terisi, loading, kosong (P27), pengajuan berubah (P31), formulir tidak lengkap (P07), toast P06 | Cancellation |
| S06 Riwayat | Borrower atau Manager | Terisi, loading, kosong (P08), gagal memuat (P09), filter tanpa hasil (P33), periode tidak valid (P19) | History |
| S07 Kelola Jadwal | Sidebar Manager | Tanggal terpilih, belum ada tanggal (P25), loading, tanpa sesi (P01), simpan (P10), simpan sebagian (P11) | Manage Schedule |
| S08 Verifikasi | Sidebar Manager | Terisi, loading, kosong (P28), filter tanpa hasil (P33), sesi tidak tersedia (P15), alasan kosong (P14), pengajuan berubah (P30), toast P12 dan P13 | Verification |
| S09 Pembatalan | Sidebar Manager | Terisi, loading, kosong (P29), filter tanpa hasil (P33), gagal (P18), sudah diproses (P32), toast P17 | Cancellation Requests |
| S10 Laporan | Sidebar Manager | Terisi, loading, tanpa peminjaman (P20), periode tidak valid (P19), kolom kosong (P07) | Reports |

### 17.2 Aturan implementasi (turunan audit)

**Responsif dan struktur**
- **Satu titik pecah: 800px**, ditulis mobile-first (`min-width: 800px`). Tidak ada titik pecah kedua (tidak ada `sm:`, `md:`, `lg:`, dan tidak ada `max-width` pada media query).
- Tabel hanya terlihat di atas 800px dan kartu bertumpuk hanya di bawahnya. Keduanya tidak boleh tampil bersamaan; penyembunyian lewat kelas, bukan `display` yang ditulis ulang oleh JavaScript.
- Menu ponsel berupa daftar tautan biasa di dalam `nav`. Tidak memakai `role="menu"`, `menuitem`, atau `separator`. "Sign out" adalah tombol.
- Tidak ada `role="button"` pada baris atau kartu. Hanya tombol nyata (Select, Details) yang memilih sebuah entri.
- Tidak ada `role="alert"` pada blok statis. Pesan kesalahan berada dalam kontainer `aria-live="polite"` yang selalu ada di DOM; isi kontainer yang diganti, bukan kontainernya yang disembunyikan.

**Pilihan, fokus, dan pesan**
- Pilihan entri: tombol berlabel unik (`aria-label` berisi fasilitas dan tanggal), `aria-pressed`, garis dalam 2px, dan label "Selected" (bagian 7).
- Setelah memilih entri, fokus pindah ke judul kartu detail (`tabindex="-1"`) lalu halaman digulir ke sana (halus, kecuali `prefers-reduced-motion`). Setelah aksi yang menghapus entri (Approve, Reject, Confirm, kirim), fokus pindah ke judul kartu detail bila ada entri berikutnya, atau ke judul daftar.
- Satu toast per halaman: wilayah `role="status"` yang selalu ada, di kanan atas, dengan isi yang diganti. Gaya: hijau (P02, P06, P10, P12, P17, P24), merah (P13), kuning (P11), sesuai token status. Teks hanya dari katalog.
- Kesalahan validasi hilang begitu isian diubah (wireframe, keputusan 14). `aria-invalid` dipasang pada isian yang bermasalah.
- Keadaan kosong memakai pesan katalog; P33 hanya bila filter yang membuat hasil kosong (wireframe, keputusan 19).

**Filter (S06, S08, S09)**
- S06: Facility (hanya Manager), Status, tanggal awal dan akhir, tombol Apply dan Reset; kesalahan periode memakai P19. S08 dan S09: dropdown Facility yang langsung berlaku, tanpa tombol.
- Mengganti filter menutup kartu detail. Lencana menu samping tidak mengikuti filter. Filter tetap aktif setelah Approve, Reject, atau Confirm.

**Dialog (S03)**
- `role="dialog"` dan `aria-modal="true"`, diberi label oleh judulnya; fokus masuk ke dialog, terkunci di dalamnya, Escape menutup, dan fokus kembali ke tombol pemicu. Tombol utama dialog nonaktif sampai kotak persetujuan dicentang.

**Kontrol desain (`data-design-only`)**
- Kotak "Design preview" di setiap layar hanya alat peninjau keadaan di Stitch. **Tidak diimplementasikan di Blade.** Keadaan yang diwakilinya muncul dari data nyata.

**Visual**
- Kartu tanpa bayangan; bayangan hanya `0 4px 16px rgb(20 32 51 / 0.14)` untuk toast, dialog, dan dropdown. Tanpa blur latar, glow, gradien (kecuali garis diagonal halus pada sel Occupied), dan huruf kapital berjarak.
- Label status pada sel kalender 12px dan tidak terpotong; tinggi sel menyesuaikan, bukan labelnya dipotong.
- Padding bawah area kerja minimal 140px hanya bila ada elemen melayang; tanpa kontrol desain di Blade, padding biasa.
- Nama fasilitas seragam di semua layar: Futsal Field 1, Basketball Court A (dan B), Tennis Court 1, Badminton Hall.

### 17.3 Perbedaan kecil di layar Stitch (diselesaikan saat membuat Blade)

| Layar | Perbedaan | Aturan yang berlaku |
|---|---|---|
| S03 | Contoh data November tidak konsisten dengan status sel (sel Full atau Closed membuka sesi Available); fokus tidak pindah setelah kirim | Status sel dihitung dari sesi nyata; fokus mengikuti bagian 17.2 |
| S07 | Label status sel kalender terpotong ("Availa...") | 12px dan tidak terpotong (bagian 17.2) |
| S08 | Teks toast "Booking request approved/rejected"; kelas `sm:` pada baris filter | Toast P12 "Booking approved" dan P13 "Booking rejected"; satu titik pecah 800px |
| S08 | Kotak P30 tanpa `aria-live` | Kontainer `aria-live` selalu ada |
| S09 | Toast berlatar gelap | Toast hijau sesuai token status |
| S10 | Nama fasilitas di tabel bukan `th scope="row"`; tautan menu `href="#"` | Header baris yang benar; tautan ke route nyata |
| S10 | Tanggal kosong menampilkan P07, yang belum tercantum pada pemetaan pesan S10 di wireframe | Tambahkan P07 pada pemetaan S10 di wireframe bila perilaku ini dipertahankan; atau tanggal dibuat wajib lewat atribut `required` |

