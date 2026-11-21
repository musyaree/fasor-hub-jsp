[Kembali ke daftar isi](../README.md)

# Konvensi Penamaan

**Semua identifier kode memakai bahasa Inggris**: nama tabel, kolom, model, kelas, metode, enum, route, perintah Artisan, dan berkas. **Teks yang dilihat orang di aplikasi memakai bahasa Inggris**, lewat berkas bahasa Laravel `lang/en`; **isi dan nama berkas dokumen tetap bahasa Indonesia**. Dokumen ini adalah daftar padanan, supaya kode, desain, dan laporan memakai nama yang sama.

Aturan umum: tabel jamak `snake_case`, model tunggal `PascalCase`, kunci utama `id`, kunci asing `<model>_id` (konvensi Laravel, sehingga model tidak perlu `$table` maupun `$primaryKey`).

## Tabel dan kolom

| Konsep | Tabel | Model | Kolom (Indonesia menjadi Inggris) |
|---|---|---|---|
| Pengguna (peminjam dan pengelola) | `users` | `User` | `id`, `name` (nama), `email`, `password`, `role` (peran), `identity` (identitas), `phone` (telepon) |
| Fasilitas | `facilities` | `Facility` | `id`, `name` (nama), `type` (jenis), `description` (keterangan) |
| Sesi jadwal | `time_slots` | `TimeSlot` | `id`, `facility_id`, `date` (tanggal), `start_time` (jam mulai), `end_time` (jam selesai), `status` |
| Pengajuan peminjaman | `bookings` | `Booking` | `id`, `user_id`, `organization_unit` (unit/organisasi), `participant_count` (jumlah peserta), `contact` (kontak), `purpose` (keperluan), `status`, `rejection_reason` (alasan penolakan), `cancellation_reason` (alasan pembatalan), `previous_status` (status sebelum batal), `cancellation_requested_at` (waktu permintaan batal) |
| Sesi dalam pengajuan | `booking_time_slot` | (pivot) | `booking_id`, `time_slot_id` |

## Enum

| Enum | Nilai (kode) |
|---|---|
| `Role` | `Borrower` (1, pengguna), `Manager` (2, pengelola) |
| `FacilityType` | `Futsal` (1), `Basketball` (2), `Badminton` (3), `Tennis` (4) |
| `BookingStatus` | `PendingVerification` (1, Menunggu Verifikasi), `Approved` (2, Disetujui), `Rejected` (3, Ditolak), `PendingCancellation` (4, Menunggu Konfirmasi Pembatalan), `Cancelled` (5, Dibatalkan) |
| `SlotStatus` | `Available` (1, Tersedia), `Occupied` (2, Terisi), `Closed` (3, Ditutup) |

Label yang tampil ke pengguna (Indonesia) diambil lewat metode `label()` pada enum.

## Kelas dan metode

| Indonesia | Inggris |
|---|---|
| `PengajuanService` | `BookingService` |
| `PengajuanService::kirim` | `BookingService::submit` |
| `PengajuanService::setujui` | `BookingService::approve` |
| `PengajuanService::tolak` | `BookingService::reject` |
| `PengajuanService::ajukanBatal` | `BookingService::requestCancellation` |
| `PengajuanService::konfirmasiBatal` | `BookingService::confirmCancellation` |
| `PengajuanService::selesaikanKedaluwarsa` | `BookingService::expireOverdue` |
| (baru, 21 November 2026) | `ScheduleService::generate` |
| `JadwalService::simpan` | `ScheduleService::save` |
| `SesiTidakTersedia` | `SlotUnavailableException` |
| `StatusPengajuanBerubah` | `BookingStatusChangedException` |
| scope dan accessor `tersedia` | `available()` dan `is_available` |
| `Peran` (middleware) | `EnsureRole` (alias `role`) |

## Controller, request, dan perintah

| Indonesia | Inggris |
|---|---|
| `JadwalPinjamController` | `ScheduleController` |
| `PengajuanController` | `BookingController` |
| `PembatalanController` | `CancellationController` |
| `RiwayatController` | `HistoryController` |
| `KelolaJadwalController` | `ManageScheduleController` |
| `VerifikasiController` | `VerificationController` |
| `PenangananPembatalanController` | `CancellationHandlingController` |
| `LaporanController` | `ReportController` |
| `RegistrasiRequest` | `RegisterRequest` |
| `PengajuanRequest` | `StoreBookingRequest` |
| `PembatalanRequest` | `StoreCancellationRequest` |
| `PenolakanRequest` | `RejectBookingRequest` |
| `PerubahanJadwalRequest` | `UpdateScheduleRequest` |
| `LaporanRequest` | `ReportRequest` |
| `pengajuan:selesaikan-kedaluwarsa` | `bookings:expire-overdue` (kelas `ExpireOverdueBookings`) |
| (baru, 21 November 2026) | `slots:generate` (kelas `GenerateTimeSlots`) |

## Route

| Indonesia | Inggris |
|---|---|
| `/registrasi` | `/register` |
| `/jadwal` | `/schedule` |
| `/pengajuan` | `/bookings` |
| `/status-pengajuan` | `/booking-status` |
| `/pembatalan` | `/cancellations` |
| `/riwayat` | `/history` |
| `/kelola-jadwal` | `/manage-schedule` |
| `/verifikasi` (`.../setujui`, `.../tolak`) | `/verification` (`.../approve`, `.../reject`) |
| `/penanganan-pembatalan` (`.../konfirmasi`) | `/cancellation-requests` (`.../confirm`) |
| `/laporan` | `/reports` |
| parameter `?fasilitas=&bulan=` | `?facility=&month=` |
| parameter `?awal=&akhir=` | `?start=&end=` |
| parameter rute `{pengajuan}` | `{booking}` |

## Berkas dan view

| Indonesia | Inggris |
|---|---|
| `lang/id/pesan.php` | `lang/en/messages.php` |
| `layouts/pengguna.blade.php`, `layouts/pengelola.blade.php` | `layouts/borrower.blade.php`, `layouts/manager.blade.php` |
| folder view `jadwal`, `pengajuan`, `pembatalan`, `riwayat`, `kelola`, `verifikasi`, `penanganan`, `laporan` | `schedule`, `bookings`, `cancellations`, `history`, `manage-schedule`, `verification`, `cancellation-requests`, `reports` |
| `PenggunaSeeder`, `FasilitasSeeder`, `JadwalSesiSeeder`, `PengajuanSeeder` | `UserSeeder`, `FacilitySeeder`, `TimeSlotSeeder`, `BookingSeeder` |

## Bahasa antarmuka (Inggris)

| Hal | Aturan |
|---|---|
| Bahasa aplikasi | Inggris (`en`) saja; tidak ada pilihan bahasa |
| Berkas | `lang/en/messages.php`, kunci `p01` sampai `p33`, dipakai lewat `__('messages.p01')`; teks Inggris di kolom "Teks (en)" [pesan-sistem.md](pesan-sistem.md) |
| Teks layar lain (menu, judul, tombol, label status) | Tetap lewat berkas bahasa `lang/en`, tidak ditulis langsung di view, supaya teks terpusat |
| Label enum (mis. `BookingStatus`) | Diambil lewat `label()` dari berkas bahasa |
| Acuan | Spesifikasi use case dan wireframe memakai teks Indonesia sebagai acuan desain; teks Inggris adalah teks yang tampil di aplikasi |

## Yang tetap bahasa Indonesia

- Isi dan nama berkas dokumen (`uc01.md`, `arsitektur.md`, `rencana-uji.md`, dan seterusnya).
- Nama use case dan aktor (Pengguna, Pengelola), ID pesan (`P01`-`P33`), ID kasus uji, dan data uji (misalnya `pengguna@contoh.id`).
- [Prototipe](../prototipe/index.html): nama variabel JavaScript di dalamnya masih memakai bahasa Indonesia karena prototipe hanya acuan tampilan dan akan dibuang.
