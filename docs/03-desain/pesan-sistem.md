[Kembali ke daftar isi](../README.md)

# Katalog Pesan Sistem

Daftar final seluruh teks pesan yang tampil kepada pengguna dan pengelola, diambil dari spesifikasi use case UC01–UC09 (P01–P20 dan P30–P32), dari layar autentikasi (P21–P24), dari halaman jadwal (P25), dari aturan sistem (P26), dari daftar kosong di Pembatalan, Verifikasi, dan Penanganan Pembatalan (P27–P29), dan dari hasil filter yang kosong (P33). Aplikasi **berbahasa Inggris**: kolom "Teks (en)" adalah teks yang tampil di aplikasi (`lang/en/messages.php`), sedangkan kolom "Teks (id)" adalah teks acuan yang dikutip di spesifikasi use case dan wireframe. Gunakan dokumen ini sebagai satu-satunya sumber teks saat membuat wireframe dan aplikasi, supaya teks di desain, kode, dan laporan sama persis.

## 1. Jenis pesan

| Jenis | Bentuk tampilan | Dipakai untuk |
|---|---|---|
| **Toast hijau** | Pesan singkat sementara di layar, tidak memblokir halaman dan bukan popup | Aksi berhasil |
| **Toast merah** | Sama seperti toast hijau | Keputusan penolakan sudah tersimpan (UC07) |
| **Toast kuning** | Sama seperti toast hijau | Aksi tersimpan sebagian dan ada yang tidak berubah (UC06) |
| **Pesan kesalahan** | Teks merah di dekat formulir atau aksi yang bermasalah | Validasi gagal atau proses gagal |
| **Peringatan** | Teks peringatan di halaman, sebelum aksi dilakukan | Kondisi yang menghalangi aksi (UC07 A3) |
| **Pemberitahuan** | Teks netral di area konten | Data kosong atau informasi belum tersedia |
| **Isi formulir** | Nilai awal pada kolom yang dapat diedit | Alasan penolakan otomatis (UC07 A3) |
| **Alasan otomatis sistem** | Teks alasan penolakan yang diisi sistem tanpa tindakan pengelola | Penolakan otomatis pengajuan yang tanggalnya terlewat (P26) |

Aturan penulisan: teks toast keberhasilan memakai huruf kapital di awal setiap kata ("Pengajuan Terkirim"); jenis pesan lain memakai kalimat biasa. Semua teks berbahasa Indonesia, singkat, dan satu baris.

## 2. Daftar pesan

| ID | UC | Pemicu (langkah di spesifikasi) | Teks (id) | Teks (en) | Jenis |
|---|---|---|---|---|---|
| P01 | UC01 | A1, informasi tidak tersedia (dari 2.1) | Informasi belum tersedia | Information is not available yet | Pemberitahuan |
| P02 | UC02 | 4.3, pengajuan tersimpan | Pengajuan Terkirim | Booking submitted | Toast hijau |
| P03 | UC02 | A1, data pemohon tidak lengkap (dari 3.1) | Data pemohon belum lengkap, harap dilengkapi | Applicant details are incomplete, please complete them | Pesan kesalahan |
| P04 | UC02 | A2, sesi tidak tersedia saat kirim (dari 4.1) | Sesi sudah tidak tersedia, silakan pilih sesi lain | The session is no longer available, please choose another session | Pesan kesalahan |
| P05 | UC03 | A1, belum ada pengajuan (dari 1.1) | Belum ada pengajuan yang perlu dipantau | There are no bookings to track yet | Pemberitahuan |
| P06 | UC04 | 3.5, permintaan pembatalan terkirim | Permintaan Pembatalan Terkirim | Cancellation request sent | Toast hijau |
| P07 | UC04 | A1, formulir tidak lengkap (dari 3.1) | Formulir tidak lengkap, harap dilengkapi | The form is incomplete, please complete it | Pesan kesalahan |
| P08 | UC05 | A1, belum ada riwayat (dari 1.3) | Belum ada riwayat peminjaman | There is no booking history yet | Pemberitahuan |
| P09 | UC05 | A2, gagal memuat data (dari 1.2); disertai tombol "Coba lagi" | Gagal memuat riwayat peminjaman | Failed to load booking history | Pesan kesalahan |
| P10 | UC06 | 3.3, perubahan jadwal tersimpan | Jadwal Tersimpan | Schedule saved | Toast hijau |
| P11 | UC06 | A1, ada sesi yang baru terisi (dari 3.1) | Jadwal tersimpan. Ada sesi yang baru terisi, tidak ditutup. | Schedule saved. Some sessions were just taken and were not closed. | Toast kuning |
| P12 | UC07 | 3.6, pengajuan disetujui | Pengajuan Disetujui | Booking approved | Toast hijau |
| P13 | UC07 | A1.7, pengajuan ditolak | Pengajuan Ditolak | Booking rejected | Toast merah |
| P14 | UC07 | A2, alasan penolakan kosong (dari A1.4) | Alasan penolakan wajib diisi | A rejection reason is required | Pesan kesalahan |
| P15 | UC07 | A3.1, ada sesi tidak tersedia (dari 3.3) | Ada sesi yang tidak tersedia | Some sessions are unavailable | Peringatan |
| P16 | UC07 | A3.3, alasan penolakan otomatis | Jadwal yang diajukan sudah tidak tersedia | The requested schedule is no longer available | Isi formulir (dapat diedit) |
| P17 | UC08 | 2.6, pembatalan dikonfirmasi | Pembatalan Dikonfirmasi | Cancellation confirmed | Toast hijau |
| P18 | UC08 | A2, pembatalan gagal diproses (dari 2.2) | Pembatalan gagal diproses, silakan coba lagi | The cancellation could not be processed, please try again | Pesan kesalahan |
| P19 | UC09 | A1, periode tidak valid (dari 2.1) | Tanggal awal tidak boleh setelah tanggal akhir | The start date cannot be after the end date | Pesan kesalahan |
| P20 | UC09 | A2, tidak ada data peminjaman (dari 2.2) | Tidak ada peminjaman pada periode ini | There are no bookings in this period | Pemberitahuan |
| P21 | Autentikasi | Login gagal | Email atau password salah | Incorrect email or password | Pesan kesalahan |
| P22 | Autentikasi | Registrasi dengan email yang sudah ada | Email sudah terdaftar | This email is already registered | Pesan kesalahan |
| P23 | Autentikasi | Registrasi dengan kolom kosong | Data pendaftaran belum lengkap, harap dilengkapi | Registration details are incomplete, please complete them | Pesan kesalahan |
| P24 | Autentikasi | Registrasi berhasil | Akun Berhasil Dibuat | Account created successfully | Toast hijau |
| P25 | UC01, UC02, UC06 | Panel sesi sebelum tanggal dipilih pada kalender | Pilih tanggal pada kalender terlebih dahulu | Select a date on the calendar first | Pemberitahuan |
| P26 | Sistem (terjadwal) | Pengajuan Menunggu Verifikasi melewati tanggal peminjaman, ditolak otomatis | Pengajuan tidak diverifikasi hingga melewati tanggal peminjaman | The booking was not verified before the booking date passed | Alasan otomatis sistem |
| P27 | UC04 | Daftar pengajuan yang dapat dibatalkan kosong (Kondisi Awal) | Tidak ada pengajuan yang dapat dibatalkan | There are no bookings that can be cancelled | Pemberitahuan |
| P28 | UC07 | Antrian pengajuan Menunggu Verifikasi kosong (Kondisi Awal) | Tidak ada pengajuan yang menunggu verifikasi | There are no bookings waiting for verification | Pemberitahuan |
| P29 | UC08 | Daftar permintaan pembatalan kosong (Kondisi Awal) | Tidak ada permintaan pembatalan yang menunggu | There are no cancellation requests waiting | Pemberitahuan |
| P30 | UC07 | A4, pengajuan sudah tidak menunggu verifikasi (dari 3.2 dan A1.5) | Pengajuan sudah tidak menunggu verifikasi | The booking is no longer waiting for verification | Pesan kesalahan |
| P31 | UC04 | A2, pengajuan sudah tidak dapat dibatalkan (dari 3.2) | Pengajuan sudah tidak dapat dibatalkan | The booking can no longer be cancelled | Pesan kesalahan |
| P32 | UC08 | A3, permintaan pembatalan sudah diproses (dari 2.1) | Permintaan pembatalan sudah diproses | The cancellation request has already been processed | Pesan kesalahan |
| P33 | UC05, UC07, UC08 | Daftar kosong hanya karena filter, sedangkan daftar tanpa filter berisi data (A6 UC05, A6 UC07, A5 UC08) | Tidak ada pengajuan yang sesuai dengan filter | There are no bookings that match the selected filters | Pemberitahuan |

## 3. Pesan menurut jenis

| Jenis | Jumlah | ID |
|---|---|---|
| Toast hijau | 6 | P02, P06, P10, P12, P17, P24 |
| Toast merah | 1 | P13 |
| Toast kuning | 1 | P11 |
| Pesan kesalahan | 13 | P03, P04, P07, P09, P14, P18, P19, P21, P22, P23, P30, P31, P32 |
| Peringatan | 1 | P15 |
| Pemberitahuan | 9 | P01, P05, P08, P20, P25, P27, P28, P29, P33 |
| Isi formulir | 1 | P16 |
| Alasan otomatis sistem | 1 | P26 |

## 4. Catatan penggunaan

- **Status dan label** yang dipakai di pesan harus sama dengan kamus data: status pengajuan (Menunggu Verifikasi, Disetujui, Ditolak, Menunggu Konfirmasi Pembatalan, Dibatalkan) dan status sesi (Tersedia, Terisi, Ditutup).
- **Pesan P13 (Pengajuan Ditolak)** berwarna merah karena menandakan hasil penolakan, bukan kegagalan sistem. Pesan P11 berwarna kuning karena hasilnya sebagian sesuai permintaan.
- **Pesan P16** bukan pesan sistem biasa, tetapi nilai awal yang dapat diedit pengelola pada formulir alasan penolakan.
- **Implementasi Laravel:** simpan teks di berkas bahasa `lang/en/messages.php` dengan kunci sesuai ID (`p01` sampai `p33`), sehingga wireframe, kode, dan laporan merujuk ke teks yang sama.
- **Kapan pesan kesalahan hilang:** pesan kesalahan validasi (P03, P07, P14, P19, P21, P22, P23) hilang begitu pengguna mengubah isian di formulir yang sama; validasi tetap dijalankan saat tombol ditekan, sehingga pesan muncul lagi bila isian masih tidak valid. Pesan hasil proses sistem tidak mengikuti aturan ini: P04 hilang saat pengguna memilih fasilitas, tanggal, atau sesi lain, dan P18 tetap sampai pengelola mencoba lagi.
- **Aturan isian formulir:** isian yang hanya berisi spasi dianggap kosong; jumlah peserta adalah bilangan bulat 1 atau lebih; batas panjang mengikuti kamus data (unit organisasi 50, kontak 20, keperluan 200, alasan penolakan dan alasan pembatalan 255 karakter) dan ditegakkan dengan batas karakter di formulir, sehingga tidak ada pesan baru untuk isian yang terlalu panjang. Server memeriksa ulang aturan yang sama.
- **Perubahan teks** dilakukan di dokumen ini lebih dulu (kolom Teks (en), dan Teks (id) bila acuannya berubah), lalu disalin ke berkas bahasa dan spesifikasi use case.
- **Daftar kosong dan filter:** P08, P28, dan P29 hanya tampil bila daftar kosong **tanpa filter** (tidak ada data sama sekali). Bila filter aktif dan data tanpa filter masih ada tetapi tidak ada yang cocok, tampil P33. Pesan P33 sama untuk S06, S08, dan S09. Kesalahan periode pada filter riwayat memakai P19 yang sudah ada.
