# SeriaMMOItemsAE

Plugin bridge ringan (Paper / Purpur 1.20+) untuk menghubungkan template konfigurasi YAML **MMOItems** dengan sistem enchant custom dari **AdvancedEnchantments (AE)**.

---

## 📌 Problem & Solusi

* **Problem**: MMOItems hanya mendukung enchantment Bukkit registry secara native di section `enchantments:`. AdvancedEnchantments menggunakan format proprietary (compound NBT, tracking hash, dynamic lore) sehingga penulisan raw NBT via `custom-nbt:` tidak memicu engine ability/listener AE saat digunakan dalam combat.
* **Solusi**: `SeriaMMOItemsAE` mendaftarkan custom stat `AE_ENCHANTS` (`ae-enchants`) ke MMOItems dan menginjeksi enchantments resmi melalui API `AEAPI.applyEnchant(enchant, level, item)` pada saat item digenerate, direforged (revision update), atau masuk ke inventory pemain.

---

## ⚙️ Contoh Konfigurasi Template MMOItems

Tambahkan list `ae-enchants` pada template item MMOItems Anda:

```yaml
HALLOWEEN2026_FREE_SWORD:
  base:
    material: NETHERITE_SWORD
    name: '<sc:halloween-title>The Hallowed Sword'
    ae-enchants:
      - 'decapitation 3'
      - 'critical 3'
```

Format list yang didukung:
* `<enchant_name> <level>` (contoh: `decapitation 3`, `critical 3`)

---

## 🛡️ Kompatibilitas Item Revision (Updater)

* **Reforge Protection**: Saat MMOItems melakukan check revision / auto-update, event `MMOItemReforgeFinishEvent` akan dicegat untuk menerapkan enchant AE dari template baru.
* **Player Enchant Preservation**: Jika opsi `preserve-player-enchants-on-reforge: true` aktif di `config.yml`, enchant tambahan yang dipasang secara manual oleh pemain sebelum update akan dipertahankan dan tidak terhapus.

---

## ⌨️ Perintah Admin

| Command | Permission | Deskripsi |
|---|---|---|
| `/mmoae reload` | `seriammoae.admin` | Memuat ulang `config.yml` |
| `/mmoae apply` | `seriammoae.admin` | Menerapkan enchant AE ke item MMOItems di tangan utama |
| `/mmoae info` | `seriammoae.admin` | Menampilkan status hook MMOItems & AE |

*Aliases*: `/aebridge`, `/mmoitems-ae`
