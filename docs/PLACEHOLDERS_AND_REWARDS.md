# Dokumentasi Placeholder Leaderboard & Kustomisasi Rewards SeriaJobs

Dokumen ini menjelaskan daftar lengkap placeholder **Leaderboard SeriaJobs** yang dapat diparsing langsung oleh **PlaceholderAPI (PAPI)** maupun **SeriaCore (GUI Engine & Math Evaluator $e(...))**, serta panduan penulisan manual tampilan hadiah `{rewards_list}` pada file konfigurasi pekerjaan (`<job>.yml`).

---

## 1. Placeholder Leaderboard (SeriaJobs)

Semua placeholder di bawah ini ber-identifier `%seriajobs_<placeholder>%`. Placeholder ini kompatibel dengan:
- **PlaceholderAPI (PAPI)**: Chat, Scoreboard, Tablist, Hologram (DecentHolograms / FancyHolograms).
- **SeriaCore GUI Engine**: Menu YAML, Lore item, Title menu, Item requirement evaluation (`selector.requirements`).
- **SeriaCore Math Engine**: Dapat dibungkus dalam ekspresi matematika `$e(round(%seriajobs_top_global_1_xp%, 0))` dsb.

### A. Leaderboard Global (Seluruh Pekerjaan)
Menampilkan peringkat pemain teratas berdasarkan akumulasi XP dan Pesanan (Work Orders) dari seluruh pekerjaan.

| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%seriajobs_top_global_<pos>_name%` | Nama pemain peringkat ke-`<pos>` (Mingguan) | `Steve` |
| `%seriajobs_top_global_<pos>_xp%` | Total XP angka murni (bisa untuk math `$e(...)`) | `125000` |
| `%seriajobs_top_global_<pos>_xp_formatted%` | Total XP berformat ribuan | `125,000` |
| `%seriajobs_top_global_<pos>_orders%` | Jumlah pesanan pasar terselesaikan | `42` |
| `%seriajobs_top_global_<pos>_orders_formatted%` | Jumlah pesanan berformat ribuan | `42` |
| `%seriajobs_top_global_<pos>_level%` | Total level pekerjaan | `85` |

#### Spesifikasi Periode (Weekly, Monthly, All-Time):
Format: `%seriajobs_top_global_<periode>_<pos>_<field>%`
- **Mingguan (Weekly)**:
  - `%seriajobs_top_global_weekly_1_name%`
  - `%seriajobs_top_global_weekly_1_xp%`
  - `%seriajobs_top_global_weekly_1_orders%`
- **Bulanan (Monthly)**:
  - `%seriajobs_top_global_monthly_1_name%`
  - `%seriajobs_top_global_monthly_1_xp%`
  - `%seriajobs_top_global_monthly_1_orders%`
- **Sepanjang Waktu (All-Time)**:
  - `%seriajobs_top_global_alltime_1_name%`
  - `%seriajobs_top_global_alltime_1_level%`
  - `%seriajobs_top_global_alltime_1_xp%`

---

### B. Leaderboard Per-Pekerjaan (Per-Job Leaderboard)
Format placeholder sangat fleksibel dan mendukung nama pekerjaan apa pun yang terdaftar (contoh: `farmer`, `miner`, `alchemist`, `fisherman`).

Format:
`%seriajobs_top_job_<job_id>_[period_]<pos>_<field>%`  
*atau*  
`%seriajobs_top_job_[period_]<job_id>_<pos>_<field>%`

| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%seriajobs_top_job_farmer_1_name%` | Juara 1 Farmer (Mingguan) | `Alex` |
| `%seriajobs_top_job_farmer_1_xp%` | XP Juara 1 Farmer (angka) | `45000` |
| `%seriajobs_top_job_farmer_1_xp_formatted%` | XP Juara 1 Farmer (berformat) | `45,000` |
| `%seriajobs_top_job_farmer_1_orders%` | Pesanan Juara 1 Farmer | `18` |
| `%seriajobs_top_job_farmer_1_level%` | Level Juara 1 Farmer | `32` |
| `%seriajobs_top_job_farmer_monthly_1_name%` | Juara 1 Farmer (Bulanan) | `Alex` |
| `%seriajobs_top_job_monthly_farmer_1_xp%` | XP Bulanan Juara 1 Farmer | `190000` |
| `%seriajobs_top_job_miner_alltime_1_name%` | Juara 1 Miner Sepanjang Masa | `Notch` |

---

### C. Posisi Peringkat & Statistik Pemain (Personal Player Placeholders)
Digunakan untuk menampilkan peringkat dan progres pemain itu sendiri di scoreboard, hologram, atau menu profil:

| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%seriajobs_top_global_player_rank%` | Peringkat global pemain (Mingguan) | `1` atau `-` jika belum masuk rank |
| `%seriajobs_top_global_player_rank_monthly%` | Peringkat global pemain (Bulanan) | `5` |
| `%seriajobs_top_global_player_rank_alltime%` | Peringkat global pemain (All-time) | `12` |
| `%seriajobs_top_job_<job_id>_player_rank%` | Peringkat pemain di pekerjaan tertentu | `2` |
| `%seriajobs_top_job_<job_id>_player_rank_monthly%` | Peringkat bulanan di pekerjaan tertentu | `4` |
| `%seriajobs_player_leaderboard_xp%` | Akumulasi XP pemain di siklus mingguan | `8500` |
| `%seriajobs_player_leaderboard_xp_formatted%` | XP pemain siklus mingguan (berformat) | `8,500` |
| `%seriajobs_player_leaderboard_xp_monthly%` | Akumulasi XP pemain di siklus bulanan | `35000` |
| `%seriajobs_player_leaderboard_orders%` | Total pesanan pemain di siklus mingguan | `7` |
| `%seriajobs_player_leaderboard_orders_monthly%`| Total pesanan pemain di siklus bulanan | `28` |

---

### D. Countdown Reset Leaderboard & Rotasi Pasar
| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%seriajobs_leaderboard_weekly_countdown%` | Waktu tersisa hingga reset mingguan | `2d 14h 32m` |
| `%seriajobs_leaderboard_monthly_countdown%` | Waktu tersisa hingga reset bulanan | `12d 8h 15m` |
| `%seriajobs_rotation_countdown%` | Waktu tersisa hingga rotasi order berikutnya | `02:45:10` |

---

## 2. Kustomisasi Manual Tampilan Hadiah `{rewards_list}`

Pada file `<job>.yml`, lore pada pesanan pasar (`orders-pool`) menggunakan placeholder `{rewards_list}`.

Sebelumnya, format teks `{rewards_list}` di-generate secara hardcoded di kode Java. Sekarang, developer memiliki kebebasan penuh untuk menulis teks tampilan secara manual dan fleksibel.

Tersedia **2 cara** untuk menyesuaikannya:

### Cara 1: Menggunakan field `display:` pada setiap entri `rewards:` (Direkomendasikan)
Developer cukup menambahkan field `display:` pada setiap baris tipe reward. Anda dapat menggunakan format warna MiniMessage (`<green>`, `<yellow>`, `<lore-color>`, dll.) atau legacy color (`&a`, `&e`):

```yaml
orders-pool:
  wheat_delivery:
    display-name: "<yellow>Panen Gandum Berkualitas</yellow>"
    lore:
      - "<gray>Status: <yellow>{progress}/{required} Gandum</yellow>"
      - "<gray>Kirim hasil panen gandum segar untuk lumbung desa."
    required-item:
      source: "VANILLA"
      material: "WHEAT"
      amount: 64
      strict-nbt: false
    rewards:
      - type: "VAULT"
        amount: 30000.0
        display: "  <lore-color>▪ <money-color>30.000 $"
      - type: "JOB_XP"
        amount: 190
        display: "  <lore-color>▪ <yellow>190 Job XP"
      - type: "AURASKILLS_XP"
        skill: "farming"
        amount: 380
        display: "  <lore-color>▪ <green>380 Farming XP"
```

#### Token Otomatis dalam `display:`:
Jika ingin menggunakan variabel nilai pada teks custom:
- `{amount}`: Nilai jumlah hadiah terformat (misal `30,000`).
- `{amount_raw}`: Nilai angka mentah (misal `30000.0`).
- `{xp}`: Jumlah poin XP.
- `{skill}`: Nama skill AuraSkills dengan awalan huruf kapital (misal `Farming`).
- `{item}` / `{material}`: Nama material item.

Contoh dengan token:
```yaml
    rewards:
      - type: "VAULT"
        amount: 30000.0
        display: "  <green>+ ${amount} Saldo Bank</green>"
      - type: "JOB_XP"
        amount: 190
        display: "  <yellow>+ {xp} Poin Karir</yellow>"
      - type: "AURASKILLS_XP"
        skill: "farming"
        amount: 380
        display: "  <aqua>+ {xp} Exp Keahlian {skill}</aqua>"
```

---

### Cara 2: Menggunakan list `display-rewards:` pada level Order
Jika developer ingin menulis seluruh blok teks hadiah secara langsung sekaligus tanpa terikat satu per satu ke objek reward (mirip seperti `display-rewards:` pada `reward-levels:`):

```yaml
orders-pool:
  carrot_supply:
    display-name: "<gold>Pasokan Wortel Segar</gold>"
    lore:
      - "<gray>Status: <yellow>{progress}/{required} Wortel</yellow>"
      - "<gray>Kirim stok wortel panen ke pasar kota."
    required-item:
      source: "VANILLA"
      material: "CARROT"
      amount: 128
      strict-nbt: false
    rewards:
      - type: "VAULT"
        amount: 45000.0
      - type: "JOB_XP"
        amount: 280
      - type: "AURASKILLS_XP"
        skill: "farming"
        amount: 550
    display-rewards:
      - "  <lore-color>▪ <money-color>45.000 $"
      - "  <lore-color>▪ <yellow>280 Job XP"
      - "  <lore-color>▪ <green>550 Farming XP"
```

Jika `display-rewards:` (atau `rewards-display:`) didefinisikan, `{rewards_list}` akan langsung menampilkan baris-baris tersebut ke dalam lore kartu order GUI secara tepat dan presisi.
