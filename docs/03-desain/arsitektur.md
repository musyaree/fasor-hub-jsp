[Kembali ke daftar isi](../README.md)

# Arsitektur dan Pemetaan Teknis

Menjawab empat pertanyaan untuk implementasi Laravel: use case dikerjakan oleh route dan controller apa, siapa yang boleh mengakses apa, siapa mengunci apa dan dalam urutan apa, dan bagaimana sistem dijalankan di Railway. Dokumen ini turunan dari [model-data.md](model-data.md), [state-diagram.md](state-diagram.md), [diagram-sekuens.md](diagram-sekuens.md), [pesan-sistem.md](pesan-sistem.md), dan [wireframe.md](wireframe.md); bila ada yang berbeda, dokumen-dokumen itu yang berlaku dan dokumen ini diperbaiki.

Cakupan: arsitektur, tabel route dan controller, model, layanan, penguncian, keamanan, perintah terjadwal, deployment di Railway. Di luar cakupan: kode Laravel, migrasi, dan kasus uji (kasus uji ada di `rencana-uji.md`).

## 1. Keputusan arsitektur

| No | Keputusan | Alasan | Alternatif yang ditolak |
|---|---|---|---|
| 1 | Monolit Laravel dengan pola MVC; view Blade, JavaScript biasa untuk kalender; MySQL | Sesuai tugas WebPro (Laravel); prototipe sudah JavaScript biasa sehingga tampilannya dipindah ke Blade tanpa konsep baru | Livewire atau Inertia: menambah konsep yang harus dipelajari dan dijelaskan di laporan, tanpa kebutuhan yang tidak bisa dipenuhi Blade |
| 2 | Dua kelas layanan: `BookingService` (UC02, UC04, UC07, UC08, dan perintah terjadwal) dan `ScheduleService` (UC06). Semua transaksi dan penguncian ada di dua kelas ini; controller tipis | Perintah terjadwal harus memakai logika yang sama dengan UC07 dan UC08; urutan penguncian cukup ditegakkan di satu tempat | Semua logika di controller: lebih sedikit berkas, tetapi logika terduplikasi di tiga tempat dan urutan kunci bisa tidak konsisten |
| 3 | Autentikasi memakai fasilitas `Auth` bawaan Laravel, tanpa Breeze | Teks P21–P24 sudah baku, registrasi punya kolom tambahan (`identity`, `phone`), dan reset password serta verifikasi email tidak ada di use case | Breeze: menambah halaman dan alur yang tidak diminta, dan teks bawaannya harus diganti |
| 4 | Otorisasi dua lapis: middleware peran di route, dan query yang dibatasi `auth()->id()` untuk data milik pengguna | Menutup akses ke data pengguna lain lewat ID di URL | Hanya middleware: tidak mencegah pengguna membuka pengajuan orang lain |
| 5 | Urutan penguncian tetap: baris `bookings` dahulu, baru baris `time_slots` terurut menurut `time_slot_id` | Mencegah deadlock antara UC02, UC06, UC07, UC08, dan perintah terjadwal (bagian 6) | Urutan bebas per use case: berisiko dua proses saling menunggu |
| 6 | Semua teks pesan di `lang/en/messages.php` dengan kunci `p01` sampai `p33` | Teks di desain, kode, dan laporan sama persis ([pesan-sistem.md](pesan-sistem.md)) | Teks tertanam di view atau controller |
| 7 | Zona waktu aplikasi `Asia/Jakarta` | "Hari ini" pada aturan tanggal lampau harus hari ini di Indonesia, sedangkan cron Railway berjalan dalam UTC | Zona waktu bawaan (UTC): sesi hari ini bisa dianggap lampau pada pukul 00.00–07.00 WIB |
| 8 | Driver session `database` | Berkas di container Railway tidak dijamin bertahan antar deploy, jadi driver `file` tidak dipakai | `cookie`: tidak bisa mencabut sesi di sisi server saat logout |
| 9 | Perintah `bookings:expire-overdue` dijalankan langsung oleh service cron Railway, tanpa scheduler Laravel | Railway sudah menyediakan cron; scheduler Laravel hanya menambah satu lapisan yang tetap butuh cron setiap menit | Scheduler Laravel: lebih banyak komponen untuk satu tugas harian |
| 10 | Antarmuka **berbahasa Inggris saja**; semua teks layar, pesan, dan label enum tetap lewat berkas bahasa `lang/en`, tidak tertanam di view | Keputusan tim: satu bahasa menyederhanakan pekerjaan dan pengujian; berkas bahasa tetap dipakai supaya teks terpusat | Dua bahasa (`en` dan `id`) dengan pemilih bahasa: menambah terjemahan, middleware, dan kasus uji tanpa kebutuhan di use case |

## 2. Gambaran arsitektur

```
Browser
   |  HTTPS
   v
routes/web.php --> middleware: auth, role
                       |
                  Controller   (validasi lewat FormRequest, memanggil layanan, memilih view)
                       |
          BookingService / ScheduleService   <----  perintah Artisan
          (transaksi dan penguncian)                bookings:expire-overdue
                       |                                 ^
                 Model Eloquent                          |
                       |                         service cron Railway
                     MySQL                       (satu kali sehari)
```

Deployment di Railway memakai tiga service dalam satu project:

| Service | Isi |
|---|---|
| Aplikasi web | Laravel, melayani semua halaman |
| MySQL | Database `time_slots`, `bookings`, dan seterusnya |
| Cron | Menjalankan perintah Artisan harian, lalu berhenti |

## 3. Struktur proyek

```
app/
  Enums/            Role, FacilityType, BookingStatus, SlotStatus
  Models/           User, Facility, TimeSlot, Booking
  Services/         BookingService, ScheduleService
  Exceptions/       SlotUnavailableException, BookingStatusChangedException  (membawa kunci pesan)
  Console/Commands/ ExpireOverdueBookings  (bookings:expire-overdue)
                    GenerateTimeSlots      (slots:generate)
  Http/
    Controllers/    AuthController, ScheduleController, BookingController,
                    CancellationController, HistoryController, ManageScheduleController,
                    VerificationController, CancellationHandlingController, ReportController
    Middleware/     EnsureRole
    Requests/       LoginRequest, RegisterRequest, StoreBookingRequest, StoreCancellationRequest,
                    RejectBookingRequest, UpdateScheduleRequest, ReportRequest
lang/en/             messages.php (katalog pesan P01–P33) dan berkas teks layar
resources/views/    layouts/borrower.blade.php (bar atas), layouts/manager.blade.php (menu samping
                    dengan lencana jumlah), auth, schedule, bookings,
                    cancellations, history, manage-schedule, verification,
                    cancellation-requests, reports
database/           migrations (5 tabel + sessions), seeders (UserSeeder, FacilitySeeder,
                    TimeSlotSeeder, BookingSeeder)
routes/web.php
```

## 4. Route dan controller per use case

Semua route di bawah memakai middleware `auth` kecuali Login dan Registrasi. Kolom Peran menunjuk nilai `role` yang diizinkan, ditegakkan middleware `role:borrower` (pengguna) atau `role:manager` (pengelola); `guest` berarti belum login.

| UC | Layar | Metode dan URL | Controller@action | Peran | Request |
|---|---|---|---|---|---|
| Autentikasi | S01 | GET `/login`, POST `/login` | `AuthController@showLogin`, `@login` | guest | `LoginRequest` |
| Autentikasi | S02 | GET `/register`, POST `/register` | `AuthController@showRegister`, `@register` | guest | `RegisterRequest` |
| Autentikasi | - | POST `/logout` | `AuthController@logout` | Semua | - |
| (beranda) | - | GET `/` | Mengalihkan: pengguna ke `/schedule`, pengelola ke `/manage-schedule` | Semua | - |
| UC01 | S03 | GET `/schedule?facility=&month=` | `ScheduleController@index` | borrower | - |
| UC02 | S03 | POST `/bookings` | `BookingController@store` | borrower | `StoreBookingRequest` |
| UC03 | S04 | GET `/booking-status` | `BookingController@index` | borrower | - |
| UC04 | S05 | GET `/cancellations` | `CancellationController@index` | borrower | - |
| UC04 | S05 | POST `/cancellations/{booking}` | `CancellationController@store` | borrower | `StoreCancellationRequest` |
| UC05 | S06 | GET `/history?facility=&status=&start=&end=` | `HistoryController@index` | borrower, manager | `HistoryFilterRequest` |
| UC06 | S07 | GET `/manage-schedule?facility=&month=` | `ManageScheduleController@index` | manager | - |
| UC06 | S07 | PUT `/manage-schedule` | `ManageScheduleController@update` | manager | `UpdateScheduleRequest` |
| UC07 | S08 | GET `/verification?facility=` | `VerificationController@index` | manager | - |
| UC07 | S08 | POST `/verification/{booking}/approve` | `VerificationController@approve` | manager | - |
| UC07.1 | S08 | POST `/verification/{booking}/reject` | `VerificationController@reject` | manager | `RejectBookingRequest` |
| UC08 | S09 | GET `/cancellation-requests?facility=` | `CancellationHandlingController@index` | manager | - |
| UC08 | S09 | POST `/cancellation-requests/{booking}/confirm` | `CancellationHandlingController@confirm` | manager | - |
| UC09 | S10 | GET `/reports?start=&end=` | `ReportController@index` | manager | `ReportRequest` |
| Otomatis | - | `php artisan bookings:expire-overdue` | `ExpireOverdueBookings` | (cron) | - |
| Otomatis | - | `php artisan slots:generate` | `GenerateTimeSlots` | (cron) | - |

Catatan:
- **Satu halaman untuk UC01 dan UC02.** `ScheduleController@index` memuat semua sesi satu fasilitas untuk satu bulan dengan satu kueri, sehingga memilih tanggal di kalender tidak memerlukan permintaan baru (sama seperti prototipe). Diagram sekuens UC02 menyingkat controller pada langkah memilih tanggal; permintaan sesungguhnya satu GET per bulan, seperti di diagram UC06.
- **Detail pengajuan** (UC03, UC04, UC07, UC08) dimuat bersama daftar dan ditampilkan di panel lewat JavaScript, sehingga tidak ada route `show` terpisah. Yang diubah selalu lewat route POST atau PUT di atas.
- **Pengajuan milik pengguna lain:** route yang menerima `{booking}` untuk pengguna (UC04) memuat model lewat query yang dibatasi `user_id = auth()->id()`; bila tidak ditemukan, hasilnya 404 (bagian 7).
- **Filter daftar (UC05, UC07, UC08)** memakai parameter query pada route GET di atas dan disaring di sisi server, tanpa paket tambahan. Fasilitas disaring lewat `whereHas('timeSlots', ...)` pada `time_slots.facility_id` (satu pengajuan hanya berisi sesi dari satu fasilitas dan satu tanggal), status lewat `bookings.status`, dan rentang tanggal lewat `time_slots.date` (batas awal dan akhir termasuk). Urutan tetap waktu pengajuan. Query riwayat pengguna tetap dibatasi `user_id = auth()->id()` sebelum filter diterapkan, sehingga filter atau parameter tidak membuka data pengguna lain; parameter `facility` hanya dipakai bila peran pengelola. `HistoryFilterRequest` memeriksa tanggal awal tidak setelah tanggal akhir (pesan P19); nilai `facility` atau `status` yang tidak dikenal diabaikan. Jumlah pada lencana menu samping dihitung tanpa filter. Form Setujui, Tolak, dan Konfirmasi Pembatalan membawa nilai `facility` yang sedang aktif sehingga redirect kembali ke daftar yang sama. Hasil kosong karena filter menampilkan P33; P08, P28, dan P29 hanya bila tidak ada data tanpa filter.
- **Dua kerangka halaman.** View pengguna memakai `@extends('layouts.borrower')` dan view pengelola `@extends('layouts.manager')` ([wireframe.md](wireframe.md), bagian 1.4). Lencana jumlah di menu pengelola dihitung oleh satu view composer pada layout pengelola dengan dua kueri `COUNT` (status 1 dan status 4) setiap halaman dimuat; tidak ada route atau layar dasbor.
- **Aturan isian** (spasi dianggap kosong, jumlah peserta bilangan bulat 1 atau lebih, batas panjang sesuai kamus data) ditulis di `FormRequest` dan batas karakter formulir ([pesan-sistem.md](pesan-sistem.md), bagian 4). `ReportRequest` mewajibkan `start` dan `end`; kosong diperlakukan seperti periode tidak valid (P19).
- **Validasi formulir** memakai `FormRequest`; pesan kesalahan P03, P07, P14, P19, P21, P22, P23 diambil dari `lang/en/messages.php`. Aturan dan perilaku hilangnya pesan mengikuti [wireframe.md](wireframe.md), keputusan 14.

## 5. Model dan relasi

| Model | Tabel | Kunci utama | Relasi |
|---|---|---|---|
| `User` | `users` | `id` | `hasMany Booking` |
| `Facility` | `facilities` | `id` | `hasMany TimeSlot` |
| `TimeSlot` | `time_slots` | `id` | `belongsTo Facility`; `belongsToMany Booking` (pivot `booking_time_slot`) |
| `Booking` | `bookings` | `id` | `belongsTo User`; `belongsToMany TimeSlot` (pivot `booking_time_slot`) |

- Nama model tunggal dan nama tabel jamak mengikuti konvensi Laravel, sehingga model tidak perlu `$table` maupun `$primaryKey` ([model-data.md](model-data.md), bagian 8). Tabel pivot `booking_time_slot` tidak memakai `timestamps`.
- Label enum yang tampil ke pengguna diambil lewat `label()` dari berkas bahasa `lang/en`.
- Kode status (CHAR 1) dibungkus enum PHP (`BookingStatus`, `SlotStatus`, `Role`, `FacilityType`) lewat `$casts`, sehingga kode tidak berisi angka mentah.
- `TimeSlot` punya satu scope `available()` (status 1 dan `date >= today`) dan satu accessor `is_available` yang sama untuk tampilan. Inilah satu-satunya definisi "tersedia" (aturan integritas 12), dipakai UC01, UC02, dan pemeriksaan di layanan.
- `$fillable` ditulis eksplisit di setiap model; `status` pengajuan dan sesi tidak pernah diisi dari data permintaan.

## 6. Layanan, transaksi, dan penguncian

### 6.1 Aturan penguncian

1. Setiap perubahan data dilakukan dalam satu transaksi database.
2. Bila sebuah proses mengunci baris `bookings`, itu dilakukan lebih dulu (`lockForUpdate`, lalu memeriksa status).
3. Baris `time_slots` dikunci sesudahnya, selalu terurut menurut `time_slot_id` naik.
4. Pemeriksaan status dan penulisan terjadi di bawah kunci yang sama, tidak pernah di luar transaksi.

Aturan 2 dan 3 membuat dua proses yang menyentuh baris yang sama selalu mengambil kunci dalam urutan yang sama, sehingga tidak saling menunggu (deadlock). UC02 dan UC06 hanya mengunci sesi (UC02 belum punya baris `bookings`), jadi mengikuti aturan 3.

### 6.2 Metode layanan

| Layanan dan metode | UC | Kunci (urutan) | Syarat di bawah kunci | Bila syarat gagal | Data yang ditulis |
|---|---|---|---|---|---|
| `BookingService::submit` | UC02 | sesi terpilih | Semua sesi status 1 dan `date >= today` | `SlotUnavailableException` (P04); tidak ada yang disimpan | `bookings` (status 1), `booking_time_slot` |
| `BookingService::approve` | UC07 | pengajuan, lalu sesinya | Status pengajuan 1; semua sesi status 1 dan `date >= today` | `BookingStatusChangedException` (P30), atau `SlotUnavailableException` (P15) | `bookings` (status 2), `time_slots` (status 2) |
| `BookingService::reject` | UC07.1 | pengajuan | Status pengajuan 1; alasan tidak kosong | `BookingStatusChangedException` (P30); alasan kosong dicegah `RejectBookingRequest` (P14) | `bookings` (status 3, `rejection_reason`) |
| `BookingService::requestCancellation` | UC04 | pengajuan | Status pengajuan 1 atau 2 dan `date >= today` | `BookingStatusChangedException` (P31) | `bookings` (status 4, `previous_status` = status lama, `cancellation_reason`, `cancellation_requested_at`) |
| `BookingService::confirmCancellation` | UC08 | pengajuan, lalu sesinya bila `previous_status` = 2 | Status pengajuan 4 | `BookingStatusChangedException` (P32); kegagalan lain membatalkan transaksi (P18) | `bookings` (status 5), `time_slots` (status 2 menjadi 1, hanya bila sebelumnya Disetujui) |
| `BookingService::expireOverdue` | Otomatis | per pengajuan: pengajuan, lalu sesinya | Status masih 1 atau 4 dan `date < today` | Dilewati tanpa pesan (sudah berubah) | Status 1 menjadi 3 dengan alasan baku P26; status 4 menjadi 5, sesi dilepas bila `previous_status` = 2 |
| `ScheduleService::save` | UC06 | sesi yang diubah | Sesi yang akan ditutup tidak berstatus 2 | Sesi itu dilewati, perubahan lain tersimpan (P11) | `time_slots` (status 1 atau 3) |

Pengecualian membawa kunci pesan; controller menangkapnya dan menampilkan pesan dari `lang/en/messages.php`. Pesan hasil proses (P02, P06, P10, P11, P12, P13, P17) tampil sebagai toast lewat flash session; pesan kesalahan (P04, P15, P18, P30, P31, P32) tampil sebagai pesan kesalahan di halaman yang sama. Jenis tiap pesan mengikuti [pesan-sistem.md](pesan-sistem.md).

### 6.3 Dua pemeriksaan yang berbeda untuk sesi

- **Penolakan karena sesi tidak tersedia** (P04 saat kirim, P15 saat setuju) membaca status sesi di bawah kunci, jadi hasilnya tidak bergantung pada tampilan yang sudah usang di browser pengguna atau pengelola.
- **Konflik UC06 dan UC07:** bila UC07 mengunci sesi lebih dulu, UC06 membaca status 2 dan masuk A1 (P11). Bila UC06 menutup sesi lebih dulu, UC07 membaca status 3 dan masuk A3 (P15). Siapa pun yang lebih dulu, hasilnya konsisten ([diagram-sekuens.md](diagram-sekuens.md), bagian 3.4).

## 7. Autentikasi dan keamanan

| Aspek | Mekanisme |
|---|---|
| Login | `Auth::attempt` dengan email dan password; gagal menampilkan P21; sesi diregenerasi setelah login |
| Registrasi | Hanya peran 1; `identity` wajib; password disimpan dengan `Hash::make`; kolom `role` tidak pernah dibaca dari form |
| Akun pengelola | Dibuat seeder, tidak bisa mendaftar sendiri ([model-data.md](model-data.md), keputusan 7) |
| Peran | Middleware `EnsureRole` membandingkan `auth()->user()->role` dengan parameter route; tidak cocok menghasilkan 403 |
| Kepemilikan data | UC03, UC04, UC05 (pengguna) memakai query `where user_id = auth()->id()`; pengelola membaca semua |
| Sesi habis | Permintaan tanpa sesi login dialihkan ke `/login` oleh middleware `auth` (UC05 A3) |
| CSRF | `@csrf` pada semua formulir dan token di permintaan AJAX |
| Injeksi SQL | Semua kueri lewat Eloquent atau query builder dengan parameter terikat |
| XSS | Semua keluaran Blade memakai `{{ }}` (otomatis di-escape); `{!! !!}` tidak dipakai |
| Mass assignment | `$fillable` eksplisit; `status` tidak pernah diisi dari permintaan |
| Rahasia | `APP_KEY` dan kredensial database hanya di variabel lingkungan Railway, tidak di repositori |

## 8. Perintah terjadwal dan cron Railway

Perintah `bookings:expire-overdue` menjalankan aturan integritas 11:
1. Ambil id pengajuan berstatus 1 atau 4 yang tanggal sesinya sebelum hari ini (`Asia/Jakarta`).
2. Untuk setiap pengajuan, jalankan `BookingService::expireOverdue` dalam transaksi sendiri (kunci pengajuan, periksa ulang status dan tanggal, tulis, lalu lepas sesi bila `previous_status` = 2).
3. Tutup koneksi database dan keluar.

Sifat yang dibutuhkan:
- **Idempoten:** menjalankannya dua kali, atau saat run sebelumnya belum selesai, tidak mengubah hasil, karena status diperiksa ulang di bawah kunci.
- **Berhenti sendiri.** Dokumentasi Railway menyebut proses cron harus selesai dan keluar; koneksi terbuka bisa membuat run berikutnya dilewati.
- **Satu transaksi per pengajuan,** bukan satu untuk seluruh daftar, supaya kunci dipegang singkat dan satu kegagalan tidak membatalkan yang lain.

Pengaturan service cron di Railway:

| Pengaturan | Nilai | Alasan |
|---|---|---|
| Perintah | `php artisan bookings:expire-overdue; php artisan slots:generate` | Dua perintah berurutan dalam satu service cron, tanpa scheduler Laravel. Tanda `;` dipakai agar kegagalan satu perintah tidak menghentikan yang lain. Belum diverifikasi: apakah kolom perintah cron Railway menjalankan teks itu lewat shell; bila tidak, bungkus dengan `sh -c '...'` |
| Jadwal | `5 17 * * *` | Railway memakai UTC; 17.05 UTC adalah 00.05 WIB, sesaat setelah pergantian hari |
| Kebijakan restart | Tidak ada | Perintah idempoten dan dijalankan lagi esok hari; restart otomatis hanya mengulang kegagalan yang sama |

### Perintah `slots:generate`

Menjaga agar setiap fasilitas selalu punya sesi untuk 35 hari ke depan, sehingga Pengelola tidak kehabisan jadwal dan P01 nyaris tidak pernah tampil. Keputusan pemilik produk 21 November 2026 (model-data.md, keputusan 1); UC06 tetap hanya membuka dan menutup sesi.
1. Hitung hari ini (`Asia/Jakarta`), lalu rentang H sampai H+34.
2. Untuk setiap fasilitas dan tiap tanggal dalam rentang, buat empat sesi dari `config/slots.php` (08.00-10.00, 10.00-12.00, 13.00-15.00, 15.00-17.00) berstatus Tersedia, hanya bila belum ada (`ScheduleService::generate`, memakai batas unik fasilitas, tanggal, dan jam).
3. Tutup koneksi database dan keluar.

Sifat yang dibutuhkan:
- **Idempoten dan tidak menimpa:** sesi yang sudah ada, termasuk yang Ditutup atau Terisi, tidak diubah. Yang ditambahkan hanya sesi yang belum ada.
- **Aman terhadap UC06 dan UC07:** hanya menyisipkan baris baru pada tanggal mendatang; batas unik menolak duplikat bila dua run bertumpuk. Tidak mengunci baris `bookings`, jadi urutan penguncian (bagian 6) tidak berubah.
- **Berhenti sendiri,** seperti `bookings:expire-overdue`.

Bila cron mati lebih dari 35 hari, sesi habis dan P01 tampil di jadwal; itu dianggap kondisi operasional, bukan kasus desain.

Ketepatan waktu cron tidak memengaruhi kebenaran: sesi pada tanggal lampau sudah diturunkan dari `date` (aturan integritas 12), jadi tidak bisa dipilih, disetujui, atau diubah walau cron terlambat. Yang bergantung pada cron hanya label status di Riwayat (Ditolak atau Dibatalkan otomatis).

## 9. Deployment di Railway (paket trial)

| Item | Isi |
|---|---|
| Service | Aplikasi web, MySQL, cron (3 dari batas 5 service paket trial) |
| Variabel lingkungan | `APP_KEY` (dari `php artisan key:generate --show`), `APP_NAME=Fasor Hub`, `APP_ENV=production`, `APP_DEBUG=false`, `APP_TIMEZONE=Asia/Jakarta`, `SESSION_DRIVER=database`, `DB_*` dari service MySQL Railway |
| Setiap deploy | `php artisan migrate --force` |
| Sekali di awal | `php artisan db:seed --force` (pengelola, pengguna uji, fasilitas, sesi) |
| Rahasia | Hanya `APP_KEY` (dibuat pengembang) dan kredensial database (disediakan service MySQL Railway); tidak ada layanan pihak ketiga lain |

Batasan paket trial (menurut dokumentasi Railway): kredit sekali pakai $5 selama 30 hari; bila trial atau kredit habis, deployment dihentikan, dan volume yang dibuat akun trial dihapus 30 hari setelah kredit berakhir. Konsekuensinya untuk rancangan ini: migrasi dan seeder harus dapat dijalankan ulang dari nol, dan bukti (dump database, tangkapan layar) disimpan di luar Railway.

Belum diverifikasi dan ditunda ke tahap deployment: cara persis menjalankan Laravel dan menghubungkan MySQL di Railway (berkas konfigurasi build dan perintah start). Alasan ditunda: butuh akun dan proyek Railway yang belum dibuat; pemilik: anggota yang mengerjakan deployment (TODO bagian deployment).

## 10. Pemetaan aturan integritas ke mekanisme

| Aturan integritas ([model-data.md](model-data.md)) | Mekanisme di arsitektur |
|---|---|
| 1 Nilai peran, status, jenis terbatas | Enum PHP dengan `$casts`, plus `CHECK` di migrasi |
| 2 Email unik | Batas unik di migrasi; `RegisterRequest` memberi P22 |
| 3 Sesi tidak dobel | Batas unik (`facility_id`, `date`, `start_time`) di migrasi |
| 4 Alasan wajib pada status 3, 4, 5 | `RejectBookingRequest`, `StoreCancellationRequest`; `requestCancellation` mengisi `previous_status` |
| 5 Pengajuan menunggu tidak mengunci sesi | `submit` tidak menulis `time_slots`; hanya `approve` yang menulis status 2 |
| 6 Dua pengajuan tidak menguasai satu sesi | `lockForUpdate` dan pemeriksaan status sesi di `approve` |
| 7 Pembatalan melepas sesi hanya bila sebelumnya Disetujui | `confirmCancellation` memeriksa `previous_status` |
| 8 Sesi Terisi tidak dapat ditutup | `ScheduleService::save` melewati sesi berstatus 2 (P11) |
| 9 `identity` wajib untuk pengguna | `RegisterRequest` |
| 10 Satu pengajuan 1 sampai 4 sesi, fasilitas dan tanggal sama | `StoreBookingRequest` memeriksa jumlah dan kesamaan fasilitas serta tanggal |
| 11 Penolakan dan pembatalan otomatis | Perintah `bookings:expire-overdue` (bagian 8) |
| 12 Sesi lampau tidak tersedia | Scope `available()` dan accessor `is_available` pada `TimeSlot` |
| 13 Status diperiksa saat diproses | Pemeriksaan di bawah `lockForUpdate` pada `approve`, `reject`, `requestCancellation`, `confirmCancellation` (P30, P31, P32) |

Invarian 1–6 di [state-diagram.md](state-diagram.md) dijaga oleh aturan 5, 6, 7, 8, 11, 12, dan 13 di atas, ditambah aturan penguncian pada bagian 6.1.

## 11. Pemetaan kebutuhan non-fungsional

| Kebutuhan | Target (kebutuhan non-fungsional pada laporan KPPL) | Mekanisme | Cara memeriksa |
|---|---|---|---|
| Usability | Satu pengajuan selesai kurang dari 5 menit | UC01 dan UC02 satu halaman (S03), nama pemohon dan kontak terisi dari akun, pesan jelas dengan teks baku | Uji coba pengajuan oleh pengguna uji, dihitung waktunya |
| Security | Data pribadi dilindungi, akun pengelola hanya bisa diakses pemiliknya | Bagian 7: middleware peran, query berbatas kepemilikan, hash password, CSRF, `$fillable` | Kasus uji akses lintas peran dan lintas pengguna |
| Performance | Halaman jadwal dan formulir termuat kurang dari 3 detik | Satu kueri per bulan per fasilitas, indeks unik (`facility_id`, `date`, `start_time`), eager loading relasi di daftar | Ukur waktu muat S03 setelah deploy di Railway |
| Reliability | Tidak ada dua jadwal yang bentrok | Transaksi, `lockForUpdate`, urutan kunci tetap (bagian 6); perintah terjadwal idempoten | Kasus uji konflik: dua persetujuan serentak, UC06 dan UC07 serentak, perintah dijalankan dua kali |

Waktu muat di Railway trial belum diketahui: belum ada deployment, dan perilaku service paket trial saat tidak ada lalu lintas belum diverifikasi. Ini diukur setelah deployment dan dicatat di `rencana-uji.md` sebagai uji non-fungsional, bukan diasumsikan di sini.
