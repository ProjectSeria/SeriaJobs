# SeriaArmorSet (SetTelepathy)

Spigot/Paper plugin untuk ekosistem **Project Seria** yang mengirimkan drop langsung ke inventory pemain (*Telepathy drop collector*) ketika mereka mengenakan armor set MMOItems tertentu yang telah dikonfigurasi.

Plugin ini menggantikan enchant Telepathy konvensional dan beroperasi penuh dari bonus set armor MMOItems.

---

## Fitur Utama

- **Block Break Telepathy**: Drop dari blok yang dihancurkan oleh pemain langsung masuk ke inventory pemain secara instan.
- **Mob Kill Telepathy**: Drop dari monster/mob (termasuk vanilla mob dan custom MythicMobs) langsung masuk ke inventory pemain yang membunuh (*killer*).
- **XP Direct Transfer**: EXP dari mob yang dibunuh langsung ditransfer ke pemain killer (dapat dikonfigurasi).
- **Full Inventory Handling**: Jika inventory pemain penuh atau hanya muat sebagian, sisa item (*leftover*) tetap jatuh di tanah secara normal.
- **MMOItems & MythicLib Set Detection**:
  - *Metode Utama*: Menggunakan API resmi `PlayerData#getInventory()#getEquipped()` untuk memeriksa set armor aktif.
  - *Metode Fallback*: Memeriksa tag NBT `MMOITEMS_ITEM_SET` pada setiap slot armor pemain menggunakan MythicLib `NBTItem`.
  - ID set bersifat *case-insensitive*.
- **MythicMobs Support**: Mendukung custom drop tables MythicMobs sehingga item drop khusus tetap terkirim ke inventory killer.
- **SeriaCore Integration**:
  - Mengikuti layout konfigurasi standar Project Seria (`debug`, `sets`, `options`, `hooks`, `messages`).
  - Mendukung MiniMessage gradient, SeriaCore semantic color tags (`<sc:prefix>`, `<title-color>`, `<sc:success-color>`), dan legacy color codes.
  - Registrasi placeholder PlaceholderAPI: `%settelepathy_has_set%`, `%settelepathy_active_set%`, `%settelepathy_pieces_worn%`, `%settelepathy_pieces_required%`.

---

## Versi Pengujian & Kompatibilitas

- **Java Version**: Java 17 / Java 21
- **Server Software**: Paper / Purpur 1.20.x & 1.21.x
- **MMOItems Version**: 6.10.x (diuji dengan MMOItems-API 6.10.1-SNAPSHOT)
- **MythicLib Version**: 1.7.x (diuji dengan MythicLib-dist 1.7.1-SNAPSHOT)
- **MythicMobs Version**: 5.6.x / 5.7.x (diuji dengan Mythic-Dist 5.7.2)
- **SeriaCore Version**: 2.0.0

---

## Langkah Build (Maven)

1. Buka terminal pada folder plugin ini:
   ```bash
   cd "C:\Users\chuen\OneDrive\Documents\Personal Data\Project Seria\SeriaArmorSet"
   ```
2. Jalankan kompilasi dengan Maven (Java 17 atau 21):
   ```bash
   mvn clean package
   ```
3. File `.jar` hasil kompilasi akan berada di:
   - `target/SeriaArmorSet-1.0.0.jar`
   - Serta otomatis disalin ke folder `OutputJar/` Project Seria:
     `C:\Users\chuen\OneDrive\Documents\Personal Data\Project Seria\OutputJar\SeriaArmorSet-1.0.0.jar`

---

## Perintah & Permission

| Perintah | Deskripsi | Permission | Default |
|---|---|---|---|
| `/settelepathy reload` | Memuat ulang konfigurasi `config.yml` tanpa restart | `settelepathy.reload` | OP |
| `/settelepathy status [player]` | Memeriksa set telepathy yang sedang dipakai pemain | `settelepathy.status` | OP |
| `/settelepathy help` | Menampilkan bantuan perintah | - | Semua |

*Aliases perintah: `/seriaarmorset`, `/armorset`, `/telepathy`.*

---

## Konfigurasi (`config.yml`)

```yaml
sets:
  HALLOWEEN2026_FREE:
    pieces-required: 4
  HALLOWEEN2026_PREMIUM:
    pieces-required: 4

options:
  blocks: true         # Block drop -> inventory
  mobs: true           # Mob drop -> killer inventory
  xp: true             # Mob XP -> killer
  play-sound: true     # Mainkan efek suara pickup
  armor-slots-only: true
```
