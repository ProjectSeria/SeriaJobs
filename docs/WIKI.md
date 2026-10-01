<p align="center">
  <img src="logo.png" alt="SeriaJobs Logo" width="220" />
</p>

# 📖 SeriaJobs — Complete Official Wiki & Manual

Selamat datang di dokumentasi resmi **SeriaJobs v2.0.0**. Plugin ini dirancang khusus untuk ekosistem server Minecraft **Seria Network**, menghadirkan sistem profesi rantai pasok (*Supply-Chain Fulfillment*), bursa kerja dinamis (*Market Work Orders*), sistem reputasi pedagang bertingkat (*Reputation Tiers*), leaderboard otomatis berkala, dan integrasi multi-plugin terpadu.

---

## 📑 Daftar Isi
1. [Fitur Unggulan](#-fitur-unggulan)
2. [Daftar Perintah (Commands) & Izin (Permissions)](#-daftar-perintah-commands--izin-permissions)
   - [Perintah Pemain (Player Commands)](#perintah-pemain-player-commands)
   - [Perintah Administratif (Admin Commands)](#perintah-administratif-admin-commands)
3. [Daftar Lengkap Placeholder (PlaceholderAPI & SeriaCore)](#-daftar-lengkap-placeholder)
   - [Profil Pekerjaan & Slot](#1-profil-pekerjaan--slot)
   - [Sistem Reputasi & Multiplier](#2-sistem-reputasi--multiplier)
   - [Bursa Kerja (Market Orders)](#3-bursa-kerja-market-orders)
   - [Sistem Reroll](#4-sistem-reroll-bursa-kerja)
   - [Papan Peringkat (Leaderboard)](#5-papan-peringkat-leaderboard)
4. [Struktur Izin (Permission Nodes)](#-struktur-izin-permission-nodes)
5. [Integrasi Ekosistem Seria](#-integrasi-ekosistem-seria)

---

## 🌟 Fitur Unggulan
- **Bursa Kerja Dinamis (Dynamic Market Shifts):** Pesanan pasar berganti secara berkala dengan sistem countdown otomatis.
- **Milestone Level Progression (1 - 100):** Roadmap visual kenaikan level dengan pagination GUI dan pembagian reward otomatis.
- **Sistem Reputasi Pedagang & Multiplier Bertingkat:** Saldo reputasi pemain (ExcellentEconomy) membuka multiplier reward uang/XP serta membuka kapasitas slot pesanan tambahan (Slot 4 & 5).
- **Dual-Unlock Slot 4:** Slot ke-4 dapat dibuka instan oleh Paid Player (rank donatur) **atau** dibuka melalui grinding reputasi bagi pemain gratisan (F2P).
- **Leaderboard Berkala Otomatis:** Peringkat mingguan dan bulanan dengan hadiah berkala yang otomatis dibagikan dan di-reset. Metrik peringkat dapat dipilih (**XP** atau **ORDERS**).
- **Ukuran Sangat Ringan (Ultra-Lightweight):** Hanya **~540 KB** dengan performa tinggi berbasis HikariCP dan SQLite/MySQL.

---

## 💻 Daftar Perintah (Commands) & Izin (Permissions)

Perintah utama dapat diakses melalui: `/seriajobs`, `/jobs`, atau `/job`.

### Perintah Pemain (Player Commands)
| Perintah | Izin (Permission) | Deskripsi |
| :--- | :--- | :--- |
| `/jobs` | `seriajobs.use` | Membuka Menu Utama Pekerjaan (GUI interaktif). |
| `/jobs join <job_id>` | `seriajobs.use` | Mengambil dan bergabung ke suatu profesi (contoh: `farmer`, `miner`). |
| `/jobs leave <job_id>` | `seriajobs.use` | Mengundurkan diri dari suatu profesi (dikenakan cooldown). |
| `/jobs progression <job_id>` | `seriajobs.use` | Membuka GUI Roadmap Level Progression (Level 1–100) dan hadiahnya. |
| `/jobs market [job_id]` | `seriajobs.use` | Membuka GUI Bursa Kerja / Kontrak Pesanan Pasar aktif saat ini. |
| `/jobs reroll <1-5> [gins\|serium] [jobId]` | `seriajobs.use` | Me-reroll pesanan pada slot tertentu dengan Gins atau Serium. |
| `/jobs top [global\|jobId] [weekly\|monthly\|alltime]` | `seriajobs.use` | Melihat daftar peringkat leaderboard langsung di chat. |

### Perintah Administratif (Admin Commands)
| Perintah | Izin (Permission) | Deskripsi |
| :--- | :--- | :--- |
| `/jobs admin reload` | `seriajobs.admin` | Me-reload file `config.yml`, `messages_*.yml`, dan pool pekerjaan. |
| `/jobs admin forceroll [job_id]` | `seriajobs.admin` | Memaksa rotasi bursa kerja sekarang juga (semua job atau job tertentu). |
| `/jobs admin setlevel <player> <job_id> <level>` | `seriajobs.admin` | Mengatur level pekerjaan pemain secara langsung. |
| `/jobs admin resetreroll <player>` | `seriajobs.admin` | Mereset kuota reroll pemain ke 0 (menjadikan reroll pertama gratis kembali). |
| `/jobs admin debug [player]` | `seriajobs.admin` | Menampilkan ringkasan debug profil pekerjaan dan reputasi pemain. |
| `/jobs admin leaderboard forcepayout [weekly\|monthly]` | `seriajobs.admin` | Memaksa pembagian hadiah leaderboard dan mereset statistik periode. |
| `/jobs admin leaderboard reset [weekly\|monthly]` | `seriajobs.admin` | Mereset database skor leaderboard periode tertentu tanpa payout. |
| `/jobs admin leaderboard update` | `seriajobs.admin` | Memperbarui cache urutan peringkat leaderboard seketika. |

---

## 🧩 Daftar Lengkap Placeholder

Semua placeholder di bawah ini kompatibel dengan **PlaceholderAPI (PAPI)**, **SeriaCore GUI Engine**, dan **Hologram** (DecentHolograms / TrHologram / FancyHolograms).

### 1. Profil Pekerjaan & Slot
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_active_jobs_count%` | Jumlah pekerjaan yang sedang aktif diambil | `2` |
| `%seriajobs_active_jobs_list%` | Daftar nama pekerjaan yang diambil pemain | `Petani, Penambang` |
| `%seriajobs_max_slots%` | Batas maksimal pekerjaan yang boleh diambil | `3` atau `Tak Terbatas` |
| `%seriajobs_primary_job%` | Nama display pekerjaan utama pemain | `Petani` |
| `%seriajobs_level%` | Level pekerjaan utama pemain | `15` |
| `%seriajobs_xp%` | XP saat ini pada pekerjaan utama | `3,450` |
| `%seriajobs_required_xp%` | XP yang dibutuhkan untuk level up pekerjaan utama | `5,000` |
| `%seriajobs_level_<job_id>%` | Level pemain pada pekerjaan tertentu | `25` |
| `%seriajobs_xp_<job_id>%` | XP pemain pada pekerjaan tertentu | `12,800` |
| `%seriajobs_required_xp_<job_id>%` | XP naik level pada pekerjaan tertentu | `15,000` |
| `%seriajobs_order_completed%` | Total pesanan pasar yang telah diselesaikan | `142` |
| `%seriajobs_order_completed_formatted%`| Total pesanan selesai dengan format ribuan | `142` |

---

### 2. Sistem Reputasi & Multiplier
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_reputation%` | Saldo reputasi pemain saat ini | `1,250` |
| `%seriajobs_reputation_tier%` | Nama tingkatan reputasi pemain | `Pedagang Terkemuka` |
| `%seriajobs_reward_multiplier%` | Pengali hadiah uang & XP (dari tier reputasi) | `1.50` |
| `%seriajobs_order_multiplier%` | Pengali kebutuhan item pesanan pasar | `1.20` |
| `%seriajobs_resource_multiplier%` | Pengali hasil panen/resource pekerjaan utama | `1.10` |
| `%seriajobs_resource_multiplier_<job_id>%` | Pengali resource khusus pekerjaan tertentu | `1.25` |

---

### 3. Bursa Kerja (Market Orders)
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_rotation_countdown%` | Waktu tersisa hingga bursa kerja di-reset | `03:45:12` |
| `%seriajobs_order_max_slots%` | Kapasitas slot pesanan pasar yang terbuka (3 - 5) | `4` |
| `%seriajobs_order_progress_<job_id>_<idx>%` | Jumlah barang yang sudah disetor pada pesanan ke-`<idx>` | `32` |
| `%seriajobs_order_required_<job_id>_<idx>%` | Total kebutuhan barang setelah dikalikan multiplier | `64` |
| `%seriajobs_order_base_required_<job_id>_<idx>%` | Kebutuhan dasar barang sebelum multiplier | `48` |
| `%seriajobs_order_reputation_<slot 1-5>%` | Nilai reward reputasi yang didapat dari slot pesanan | `25` |

---

### 4. Sistem Reroll Bursa Kerja
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_reroll_current%` | Jumlah reroll yang sudah dipakai dalam siklus pasar | `3` |
| `%seriajobs_reroll_max%` | Batas maksimal reroll per siklus rotasi | `10` |
| `%seriajobs_reroll_remaining%` | Sisa kuota reroll yang masih tersedia | `7` |
| `%seriajobs_reroll_ratio%` | Rasio reroll terpakai / maksimal | `3/10` |
| `%seriajobs_can_reroll%` | Apakah pemain masih bisa me-reroll (`true`/`false`) | `true` |
| `%seriajobs_reroll_cost_gins%` | Biaya uang Gins untuk reroll berikutnya | `4,500` |
| `%seriajobs_reroll_cost_serium%` | Biaya mata uang premium Serium untuk reroll | `1` |

---

### 5. Papan Peringkat (Leaderboard)

#### A. Entri Peringkat Global & Job
Format: `%seriajobs_top_global_[period_]<pos>_<field>%` atau `%seriajobs_top_job_<job_id>_[period_]<pos>_<field>%`
- `[period]`: `weekly` (bawaan), `monthly`, atau `alltime`
- `<pos>`: Posisi peringkat (1 sampai 50)
- `<field>`:
  - `name`: Nama pemain
  - `score`: Nilai skor utama (XP atau Order sesuai `sort-metric` di konfigurasi)
  - `score_formatted`: Nilai skor dengan format ribuan (`150,000`)
  - `xp` / `xp_formatted`: Akumulasi XP pekerjaan
  - `orders` / `orders_formatted`: Total pesanan pasar selesai
  - `level`: Level pekerjaan (khusus all-time)

**Contoh Praktis:**
- `%seriajobs_top_global_1_name%` — Nama Juara 1 Global (Mingguan)
- `%seriajobs_top_global_1_score_formatted%` — Skor Juara 1 Global
- `%seriajobs_top_global_monthly_1_name%` — Nama Juara 1 Global Bulanan
- `%seriajobs_top_job_farmer_1_name%` — Juara 1 Petani Minggu Ini
- `%seriajobs_top_job_miner_monthly_1_score_formatted%` — Skor Juara 1 Penambang Bulan Ini

#### B. Peringkat Personal Pemain yang Melihat
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_top_global_player_rank%` | Peringkat global mingguan pemain | `3` atau `-` |
| `%seriajobs_top_global_player_rank_monthly%` | Peringkat global bulanan pemain | `12` |
| `%seriajobs_top_global_player_rank_alltime%` | Peringkat global sepanjang masa pemain | `5` |
| `%seriajobs_player_rank_job_<job_id>%` | Peringkat pemain khusus di job tertentu | `2` |
| `%seriajobs_player_leaderboard_xp_formatted%` | Akumulasi XP pemain di siklus berjalan | `45,200` |
| `%seriajobs_player_leaderboard_orders%` | Total pesanan selesai pemain di siklus berjalan | `19` |

#### C. Countdown Reset Leaderboard
| Placeholder | Deskripsi | Contoh Nilai |
| :--- | :--- | :--- |
| `%seriajobs_leaderboard_weekly_countdown%` | Sisa waktu hingga reset mingguan & pembagian reward | `3d 14h 20m` |
| `%seriajobs_leaderboard_monthly_countdown%` | Sisa waktu hingga reset bulanan & pembagian reward | `18d 06h 45m` |

---

## 🔒 Struktur Izin (Permission Nodes)

| Permission Node | Default | Deskripsi |
| :--- | :--- | :--- |
| `seriajobs.use` | `true` | Mengizinkan pemain membuka menu bursa kerja, roadmap level, dan mengambil order. |
| `seriajobs.admin` | `op` | Memberikan akses penuh ke perintah `/jobs admin`. |
| `seriajobs.slot.4` | `false` | Membuka Slot ke-4 secara instan tanpa perlu syarat reputasi (untuk rank VIP). |
| `seriajobs.slot.5` | `false` | Membuka Slot ke-5 secara instan (untuk rank donatur MVP). |
| `seriajobs.rank.paid` | `false` | Permission donatur global (otomatis membuka hingga 5 slot penuh). |
| `seriajobs.slots.<N>` | `false` | Memberikan batas kuota pengambilan pekerjaan sebanyak `<N>` cabang sekaligus. |
| `seriajobs.slots.*` | `false` | Menghapus batasan kuota pekerjaan (pemain bisa mengambil seluruh pekerjaan). |

---

## 🌐 Integrasi Ekosistem Seria

SeriaJobs terintegrasi secara *native* dengan seluruh sistem Seria Network:
1. **ExcellentEconomy:** Sinkronisasi saldo reputasi pedagang secara real-time.
2. **Vault:** Pembayaran imbalan Gins dari setoran pesanan pasar.
3. **AuraSkills:** Sinkronisasi penambahan XP skill Minecraft (Farming, Mining, Fishing, Foraging).
4. **SeriaPrefix & MiniMessage:** Dukungan penuh tag warna (`<sc:title-color>`, `<sc:gins-icon>`, `<gold>`, dll.).
5. **SeriaCrafting, SeriaMagicBundle, MMOItems, MythicMobs & CustomFishing:** Dukungan penuh sebagai target pesanan setoran bursa kerja maupun hadiah milestone kenaikan level.
