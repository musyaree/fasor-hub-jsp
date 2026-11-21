# Dokumentasi Fasor Hub

Dokumentasi rancangan **Fasor Hub**, sistem peminjaman fasilitas olahraga kampus. Folder ini adalah **acuan utama** perancangan.

## Struktur

```
docs/
├── 02-use-case/   spesifikasi, activity diagram, user story, EARS per use case
├── 03-desain/     model data, state diagram, wireframe, pesan, sekuens, arsitektur, rencana uji
├── DESIGN.md      arah visual, token, aturan implementasi antarmuka
└── prototipe/     prototipe HTML low-fidelity yang bisa diklik
```

## Use case

Setiap dokumen berisi spesifikasi, activity diagram (kode PlantUML), user story, EARS, dan catatan rancangan.

| Kode | Nama | Aktor | Dokumen |
|---|---|---|---|
| UC01 | Melihat Informasi Fasilitas dan Jadwal | Pengguna | [uc01.md](02-use-case/uc01.md) |
| UC02 | Mengajukan Peminjaman | Pengguna | [uc02.md](02-use-case/uc02.md) |
| UC03 | Memantau Status Pengajuan | Pengguna | [uc03.md](02-use-case/uc03.md) |
| UC04 | Mengajukan Pembatalan | Pengguna | [uc04.md](02-use-case/uc04.md) |
| UC05 | Melihat Riwayat Peminjaman | Pengguna dan Pengelola | [uc05.md](02-use-case/uc05.md) |
| UC06 | Mengelola Jadwal Fasilitas | Pengelola | [uc06.md](02-use-case/uc06.md) |
| UC07 dan UC07.1 | Memverifikasi Pengajuan dan Memberikan Alasan Penolakan | Pengelola | [uc07.md](02-use-case/uc07.md) |
| UC08 | Menangani Pembatalan | Pengelola | [uc08.md](02-use-case/uc08.md) |
| UC09 | Melihat Laporan Penggunaan | Pengelola | [uc09.md](02-use-case/uc09.md) |

## Desain

| Dokumen | Isi |
|---|---|
| [model-data.md](03-desain/model-data.md) | ERD konseptual dan fisik, kamus data, aturan integritas, pemetaan use case ke tabel, DDL |
| [state-diagram.md](03-desain/state-diagram.md) | Lima status pengajuan dan tiga status sesi, transisi, dan invarian |
| [pesan-sistem.md](03-desain/pesan-sistem.md) | Katalog 33 pesan sistem (P01-P33) dengan teks acuan Indonesia dan teks aplikasi Inggris, jenis, dan pemicu |
| [wireframe.md](03-desain/wireframe.md) | Wireframe 10 layar, konvensi, peta navigasi, dan keputusan antarmuka |
| [diagram-sekuens.md](03-desain/diagram-sekuens.md) | Diagram sekuens UC02, UC07, dan UC06 |
| [penamaan.md](03-desain/penamaan.md) | Konvensi penamaan: identifier kode berbahasa Inggris, daftar padanan Indonesia ke Inggris |
| [arsitektur.md](03-desain/arsitektur.md) | MVC Laravel, route dan controller per use case, layanan dan penguncian, keamanan, perintah terjadwal, deployment di Railway |
| [rencana-uji.md](03-desain/rencana-uji.md) | 118 kasus uji, data uji, dan keterlacakan ke pesan, aturan integritas, invarian, dan alur alternatif |

## Desain visual

[DESIGN.md](DESIGN.md) memuat arah visual (warna, tipografi, komponen, status), hasil akhir tiap layar, dan aturan implementasi antarmuka.

Acuan visual high-fidelity dibuat di Stitch: [https://stitch.google.com/projects/14085482433280035910](https://stitch.google.com/projects/14085482433280035910). Proyek itu privat, jadi hanya bisa dibuka oleh akun yang diundang, dan ia berisi beberapa versi layar; yang final adalah yang terbaru pada papan, dengan nama layar di [DESIGN.md](DESIGN.md) bagian 17.1. Layar Stitch hanya **acuan visual**: yang berlaku adalah [wireframe.md](03-desain/wireframe.md), [pesan-sistem.md](03-desain/pesan-sistem.md), dan [DESIGN.md](DESIGN.md).

## Prototipe

[prototipe/index.html](prototipe/index.html) adalah prototipe HTML low-fidelity yang bisa diklik untuk semua layar.

## Membaca diagram

- **Mermaid** (ERD, state diagram, peta navigasi, diagram sekuens) dirender langsung oleh GitHub. Di VS Code, pasang ekstensi pratinjau Mermaid untuk Markdown.
- **PlantUML** (activity diagram) **tidak** dirender GitHub dan tampil sebagai kode. Di VS Code, pasang ekstensi PlantUML dan atur `"plantuml.render": "Local"` (butuh Java), lalu tekan `Alt+D` di dalam blok `@startuml`.

## Kesepakatan istilah

- **Status pengajuan:** Menunggu Verifikasi, Disetujui, Ditolak, Menunggu Konfirmasi Pembatalan, Dibatalkan.
- **Status sesi:** Tersedia, Terisi, Ditutup.
- Identifier kode (tabel, kolom, kelas, metode, route) memakai bahasa Inggris, teks yang tampil ke pengguna tetap bahasa Indonesia; padanannya ada di [penamaan.md](03-desain/penamaan.md).
- Teks pesan yang tampil ke pengguna hanya ditulis di [pesan-sistem.md](03-desain/pesan-sistem.md); desain, kode, dan laporan merujuk ke ID pesan (P01-P33).
