# SeriaArmorSet

Engine kustom untuk **Project Seria** yang menangani kemampuan dan skill khusus dari **MMOItems Armor Set Bonus** yang tidak dapat ditangani secara native oleh MythicLib atau MythicMobs.

Plugin ini dibangun dengan arsitektur modular extensible skill, di mana pengembang dapat mendaftarkan berbagai skill unik (seperti Telepathy drop collector, custom vacuum, shield, evasion, custom triggers, dll.) tanpa harus mengubah core listener.

---

## Fitur Utama

- **Extensible Skill Engine**:
  - Menyediakan sistem `ArmorSkill` modular.
  - Mengizinkan satu armor set memiliki banyak custom skills sekaligus.
  - Terintegrasi dengan MythicLib & MMOItems secara non-intrusif.
- **Skill Bawaan: Telepathy Drop Collector**:
  - **Block Break Telepathy**: Drop dari blok yang dihancurkan pemain langsung masuk ke inventory secara instan.
  - **Mob Kill Telepathy**: Drop dari mob/monster (vanilla dan MythicMobs) langsung masuk ke inventory killer.
  - **XP Direct Transfer**: EXP dari mob langsung diberikan ke killer.
  - **Inventory Overflow Handling**: Item yang tidak muat tetap jatuh di tanah secara aman.
- **Dual Set Detection Engine**:
  - *Metode Utama (Preferred API)*: Membaca slot armor via MMOItems `PlayerData#getInventory()#getEquipped()`.
  - *Metode Fallback (MythicLib NBT)*: Memeriksa tag NBT `MMOITEMS_ITEM_SET` pada slot armor pemain via MythicLib `NBTItem`.
  - Case-insensitive set IDs.
- **Dukungan MythicMobs Drop Tables**:
  - Mengintersepsi event `MythicMobDeathEvent` sehingga custom drop tables MythicMobs terjamin masuk ke inventory pemain.
- **Standarisasi Ekosistem SeriaCore**:
  - Struktur folder modular: mendukung `config.yml` dan file definisi terpisah di folder `sets/*.yml`.
  - Pesan mendukung Adventure MiniMessage, SeriaCore semantic tags (`<sc:prefix>`, `<title-color>`, `<sc:success-color>`, dll.), dan format warna legacy `&`.
  - Menyediakan PlaceholderAPI:
    - `%seriaarmorset_has_set%`
    - `%seriaarmorset_active_set%`
    - `%seriaarmorset_pieces_worn%`
    - `%seriaarmorset_pieces_required%`
    - `%seriaarmorset_active_skills%`
    - *(Serta alias backwards-compatible `%settelepathy_...%`)*

---

## Struktur Folder Standar

```
SeriaArmorSet/
├── config.yml                # Pengaturan umum, hooks, opsi default & fallback
├── sets/                     # Folder modular definisi armor set (standar SeriaCore)
│   └── halloween_2026.yml    # Definisi set Halloween 2026
```

---

## Perintah & Permissions

| Perintah | Deskripsi | Permission | Default |
|---|---|---|---|
| `/armorset reload` | Memuat ulang seluruh konfigurasi & set | `seriaarmorset.reload` | OP |
| `/armorset status [player]` | Mengecek set dan skill aktif pada pemain | `seriaarmorset.status` | OP |
| `/armorset list` | Melihat daftar seluruh set & skill terdaftar | `seriaarmorset.admin` | OP |
| `/armorset help` | Menampilkan menu bantuan | - | Semua |

*Aliases perintah: `/seriaarmorset`, `/settelepathy`, `/telepathy`.*

---

## Contoh Konfigurasi (`sets/halloween_2026.yml`)

```yaml
sets:
  HALLOWEEN2026_FREE:
    pieces-required: 4
    skills:
      telepathy:
        enabled: true
        blocks: true
        mobs: true
        xp: true
        play-sound: true

  HALLOWEEN2026_PREMIUM:
    pieces-required: 4
    skills:
      telepathy:
        enabled: true
        blocks: true
        mobs: true
        xp: true
        play-sound: true
      # Contoh skill kustom mendatang:
      # eclipse_vacuum:
      #   radius: 6.0
```

---

## Langkah Build (Maven)

```bash
mvn clean package
```
File jar hasil kompilasi otomatis berada di `OutputJar/SeriaArmorSet-1.0.0.jar`.