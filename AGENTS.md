# Workspace Instructions & Memory (Project Seria)

## Build Output Rule (MANDATORY)
Setiap kali selesai melakukan build / compile plugin apa pun di workspace ini (misalnya dengan `mvn clean package`, `mvn package`, `gradlew shadowJar`, atau command build lainnya):
1. **PASTIKAN SELALU menyalin / memindahkan file `.jar` hasil kompilasi ke folder:**
   `C:\Users\chuen\OneDrive\Documents\Personal Data\Project Seria\OutputJar`
2. Jangan menyalin file non-final seperti `original-*.jar`.
3. Selalu verifikasi bahwa file `.jar` di `OutputJar` telah ter-update dengan versi dan timestamp terbaru setelah proses build selesai.

## Personal Assistant & YAML Auto-Detection Rule (MANDATORY)
1. **Peran Utama:** Bertindak sebagai Personal Assistant resmi untuk Project Seria di sesi percakapan ini dan seluruh percakapan berikutnya.
2. **Auto-Detection & Auto-Repair YAML:** Setiap kali terdapat file konfigurasi YAML (`config.yml`, `jobs/*.yml`, `menus/*.yml`, `placeholder/*.yml`, `messages_*.yml`, dll.) yang mengalami error sintaks, kesalahan indentasi, mapping rusak, placeholder tidak cocok, atau inkonsistensi struktur:
   - **Wajib langsung mendeteksi** masalah tersebut secara proaktif.
   - **Langsung perbaiki** file terkait agar kembali valid dan fungsional.
   - Jelaskan perbaikan yang telah dilakukan secara ringkas dan solutif kepada user.

## YAML-First Fix & Zero-Hardcoding Policy (MANDATORY)
1. **Prioritas Konfigurasi YAML:** Setiap kali user meminta bantuan untuk melakukan perbaikan, penyesuaian logika, penambahan alur aksi/command, atau penambahan fitur GUI:
   - **Wajib utamakan perbaikan pada file YAML terlebih dahulu** (`config.yml`, `menus/*.yml`, `placeholder/*.yml`, dll.) dengan memanfaatkan struktur logika dan placeholder engine SeriaCore yang sudah ada.
   - Gunakan kode Java yang ada sebagai referensi pembacaan (ground truth) tentang bagaimana konfigurasi tersebut diproses.
2. **Larangan Keras Hardcoding di Java Class:**
   - **DILARANG KERAS melakukan hardcoding** elemen GUI (seperti nomor slot item, material tombol, teks lore/nama, ukuran GUI, atau alur aksi navigasi) di dalam Java class.
   - Seluruh aspek GUI **wajib 100% fleksibel dan dikendalikan sepenuhnya melalui file konfigurasi YAML**.
   - Java class hanya boleh bertindak sebagai *generic data-driven engine* (pembaca konfigurasi yang fleksibel).
3. **Kriteria Ketat Modifikasi Java Class:**
   - Java class **HANYA BOLEH diubah/ditambah jika terdapat keterbatasan teknis fundamental** pada Java class (misalnya: method hook plugin eksternal belum ada, action identifier baru yang belum dikenali engine, atau sistem kalkulasi matematis dasar yang belum didukung).
   - Jika kebutuhan user masih dapat diselesaikan menggunakan command (`[player]`, `[console]`), placeholder SeriaCore, atau struktur section menu YAML yang sudah ada, **DILARANG mengubah kode Java**.

## Server Configuration & Active Document Priority Rule (MANDATORY)
1. **Fokus pada File Server Aktif:**
   - Setiap kali user meminta bantuan untuk melakukan perbaikan/penyesuaian konfigurasi (fix, config edit, menu layout, dll.), **selalu utamakan file yang sedang dibuka oleh user** (periksa metadata `Active Document` dan tag `@file` seperti file di `Server Data/seria-dev/plugins/...`).
   - Sadari bahwa target perbaikan utama adalah **file konfigurasi yang berjalan di server nyata**, bukan sekadar template bawaan di `src/main/resources/`.
2. **Proaktif Meminta Pembukaan File Terkait:**
   - Jika dalam proses analisis atau troubleshooting diperlukan isi dari file konfigurasi server lain (misalnya `placeholder/reroll.yml`, `config.yml`, file menu lain, atau messages), **segera infokan kepada user secara jelas untuk membuka file tersebut di editor**, agar context-nya dapat dibaca dan perbaikan dapat diselaraskan secara akurat.

## Dynamic Menu Layout & Fillers Rule (MANDATORY)
Setiap kali diminta membuat, memperbaiki, atau menyusun layout pada file konfigurasi menu GUI YAML (misalnya `job_orders_gui.yml`, `job_progression_gui.yml`, dll.):

1. **Wajib Sertakan Blok Fillers Border Standar:**
   ```yaml
   fillers:
     glass:
       material: "YELLOW_STAINED_GLASS_PANE"
       name: " "
       slots: [0, 1, 7, 8, 9, 36, 44, 45, 46, 52, 53]
       item-flags:
         - "HIDE_ADDITIONAL_TOOLTIP"
   ```

2. **Wajib Gunakan Struktur Layout Dinamis Berbasis Taint:**
   - Semua slot dinamis **wajib** didefinisikan di dalam `layout.<group-slots>.slots` (dilarang menggunakan konfigurasi slot legacy di root seperti `level-slots:`).
   - Seluruh kartu dinamis (seperti `incomplete`, `completed`, `reached`, `next`, `locked`) **wajib diletakkan langsung di dalam blok `items:`** dengan properti `taints:` yang cocok dengan `selector` di `layout`:
   ```yaml
   layout:
     order-slots:
       slots: [20, 22, 24, 30, 32]
       item-provider: "%parameters_job_orders%"
       selector:
         requirements:
           1:
             taint: "orders-item"
             comparison: "=="
             value: true  

   items:
     incomplete:
       name: "<sc:error-color>{order_display_name}"
       taints:
         orders-item: true

     completed:
       name: "<sc:success-color>{order_display_name}"
       taints:
         orders-item: true
   ```

3. **Penghapusan Bagian Legacy & Standarisasi:**
   - Hapus section terpisah di luar `items:` (seperti `level-display:`, `level-slots:`, dll.). Pindahkan seluruh status kartu dinamis tersebut ke dalam `items:` dengan taint yang sesuai.
   - Sesuaikan `item-provider` dengan placeholder provider yang relevan (`%parameters_job_orders%`, `%parameters_job_levels%`, dll.).
   - **Dilarang keras mengubah tulisan/pesan/warna** yang sudah ada jika pengguna hanya meminta perbaikan atau penyesuaian struktur layout.

4. **Wajib Gunakan Struktur Priority & View Requirement untuk Slot yang Sama:**
   Jika sebuah slot inventory digunakan oleh lebih dari satu item kondisional (multi-state item):
   - Wajib gunakan `priority:` (integer) pada setiap kandidat item, di mana angka yang lebih tinggi dievaluasi lebih awal oleh menu renderer.
   - Wajib pasang blok `view-requirement:` pada item dengan prioritas lebih tinggi untuk mengecek kondisi pemain.
   - Gunakan item prioritas lebih rendah (misal `priority: 1` atau tanpa view-requirement) sebagai fallback default saat kondisi tidak terpenuhi.
   
   Contoh format standar:
   ```yaml
   items:
     # Item prioritas tinggi (jika syarat terpenuhi)
     button_available:
       slot: 22
       priority: 2
       view-requirement:
         requirements:
           check_condition:
             type: "placeholder"
             input: "%condition%"
             comparison: "=="
             output: "true"
       material: "EMERALD"
       name: "<green>Tersedia</green>"

     # Item prioritas rendah (fallback default saat syarat tidak terpenuhi)
     button_locked:
       slot: 22
       priority: 1
       material: "BARRIER"
       name: "<red>Terkunci</red>"
   ```

5. **Wajib Gunakan Urutan Standar Properti Item (Item Property Ordering):**
   Seluruh item di dalam blok `items:` wajib disusun secara rapi dan konsisten mengikuti urutan hierarki berikut:
   ```yaml
   items:
     item_key:
       name: "<title-color>Nama Item"
       material: "ITEM_MATERIAL"
       taints:                   # jika merupakan dynamic tainted item
         item-taint: true
       slot: 22                  # jika item statis / bertarget slot
       priority: 2               # jika merupakan multi-state candidate item
       lore:
         - "<gray>Baris deskripsi..."
       view-requirement:         # selalu diletakkan di bawah lore
         requirements:
           check_condition:
             type: "placeholder"
             input: "%condition%"
             comparison: "=="
             output: "true"
       click-requirement:        # optional
         requirements:
           ...
       actions:                  # atau click-action / left-click-action / right-click-action
         - "[action_command]"
   ```


## Git Branching, Commit & Push Workflow Rule (MANDATORY)
Setiap kali mengerjakan task (baik di repository utama maupun sub-repository plugin), **WAJIB** mengikuti alur kerja standar (SOP) berikut:

1. **Pembuatan Branch Sebelum Mengubah Kode:**
   - **Fitur Baru / Enhancement:** Buat dan beralih ke branch spesifik `feat/<nama-fitur>`
     *(Contoh: `git checkout -b feat/ui-inventory` atau `git checkout -b feat/custom-fishing`)*.
     *Aturan Khusus:* **TIDAK PERLU** membuat branch `feat/` baru apabila fungsi/fitur yang akan dikerjakan sudah ada / relevan dengan branch fitur sebelumnya yang sedang aktif, atau hanya berupa penambahan minor. Cukup lanjutkan pengerjaan dan commit pada branch yang sudah ada.
   - **Perbaikan Bug / Hotfix:** Buat dan beralih ke branch spesifik `fix/<nama-bug>`
     *(Contoh: `git checkout -b fix/progressbar-syntax` atau `git checkout -b fix/placeholder-null`)*.
     *Aturan Khusus:* Jika perbaikan merupakan kelanjutan dari bugfix yang sedang berjalan di branch `fix/` aktif, lanjutkan commit langsung pada branch tersebut.

2. **Lakukan Perubahan Kode / Konfigurasi:**
   - Terapkan modifikasi file yang dibutuhkan sesuai instruksi task.

3. **Stage Semua File yang Berubah:**
   - Gunakan `git add .` untuk menyertakan seluruh file yang ditambah/diubah di dalam project repository terkait.

4. **Commit dengan Pesan Terstruktur & Poin Penjelasan:**
   - Wajib menyertakan ringkasan deskriptif serta rincian poin perubahan yang telah dilakukan:
     ```bash
     git commit -m "feat: <ringkasan fitur>

     - rincian fitur / perubahan 1
     - rincian fitur / perubahan 2
     - dll"
     ```
     *(atau `fix: <ringkasan bug> ...` untuk perbaikan bug)*.

5. **Push ke Remote Repository:**
   - Jalankan `git push` (atau `git push -u origin <nama-branch>` jika branch baru pertama kali di-push).

6. **Larangan Otomatis Merge ke Main (MANDATORY):**
   - **DILARANG KERAS melakukan merge ke branch `main` (atau branch default lain) secara otomatis**, kecuali user secara eksplisit meminta atau menginstruksikan proses merge.
   - Setelah proses push selesai, tetap berada pada branch fitur/fix (`feat/*` atau `fix/*`) dan laporkan status branch/PR kepada user untuk ditinjau.

## MythicMobs Spawner Configuration Standard (MANDATORY)
Setiap kali membuat, mengedit, atau memperbaiki file konfigurasi MythicMobs Spawner (`plugins/MythicMobs/spawners/**/*.yml`):

1. **Struktur Root & Hirarki Indentasi (2 Spasi):**
   - Root key **wajib** berupa identifier spawner (biasanya sama dengan nama file tanpa `.yml`).
   - Seluruh properti spawner diletakkan langsung di bawah root key dengan indentasi 2 spasi.
   - **DILARANG KERAS** menyisipkan sub-section `yml:` di bawah root key (bug umum yang terjadi bila spawner dibuat dengan menyertakan ekstensi `.yml` pada identifier spawner).
   - **DILARANG** meninggalkan sisa trailing dummy fields kosong di tingkat root (seperti `MobName: ''`, `CooldownTimer: 0`, `WarmupTimer: 0`, `ActiveMobs: 0`).

2. **Format Standar Properti Spawner (Ground Truth):**
   ```yaml
   <spawner_internal_name>:
     MobName: <mythicmob_id>
     World: <world_name>
     SpawnerGroup: <spawner_group_name>
     X: <x_pos>
     Y: <y_pos>
     Z: <z_pos>
     Radius: 1
     RadiusY: 0
     UseTimer: true
     Yaw: 0.0
     Pitch: 0.0
     MobLevel: '1'
     Cooldown: 30
     CooldownTimer: 0
     Warmup: 30
     WarmupTimer: 0
     CheckForPlayers: false
     ActivationRange: 25
     ScalingRange: -1.0
     LeashRange: 30
     HealOnLeash: true
     ResetThreatOnLeash: true
     ShowFlames: false
     Breakable: false
     MobsPerSpawn: 5
     ActiveMobs: 0
   ```

3. **Konsistensi Format & File Encoding:**
   - Gunakan encoding **UTF-8 (without BOM)** dengan line endings **LF**.
   - Setiap file spawner individual mewakili satu entitas spawner unik dengan koordinat dan konfigurasi target mob yang terdefinisi dengan jelas.

## External Plugin API Inquiry Rule (MANDATORY)
Setiap kali membutuhkan API, event, method, class internal, atau dependency dari plugin eksternal Seria (seperti MythicMobs, MMOItems, PacketEvents, AuraSkills, LuckPerms, Eco, dll.) untuk membangun, memperluas, atau mengupdate plugin Seria:
- **WAJIB selalu bertanya dan meminta konfirmasi kepada user terlebih dahulu** sebelum berasumsi, mengintegrasikan, atau memodifikasi hook API plugin eksternal tersebut.


