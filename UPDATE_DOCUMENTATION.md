# 🚀 Project Seria - Update Documentation & Changelog v2.0.0

Dokumen ini berisi rangkuman lengkap mengenai pembaruan arsitektur sistem, standardisasi skema konfigurasi YAML, integrasi plugin baru `SeriaGacha`, serta peningkatan parser engine pada `SeriaCore` dan `SeriaJobs`.

---

## 📌 Ringkasan Pembaruan Utama

1. **Standardisasi GUI Menu Engine**:
   - Semua 17 plugin Seria kini mendukung spesifikasi YAML menu modern dengan parameter dinamis, variabel lokal, semantic color tags, dan dynamic slot resolution.
2. **Dynamic Math & Requirement Evaluator**:
   - Engine baru pada `SeriaCore` (`MenuRequirementEvaluator`) yang mendukung ekspresi matematika `$e(...)`, pembulatan `round(...)`, serta evaluasi logika bertingkat (`view-requirement`).
3. **Pembaruan Skema Jobs & Polynomial Progression**:
   - `SeriaJobs` kini mendukung persamaan polinomial untuk leveling, income, points, dan XP progression, serta 100 level reward tier terstruktur.
4. **Penambahan Plugin Baru `SeriaGacha`**:
   - Refactor dan modernisasi source code dari `clean-src` menjadi `SeriaGacha` (package `id.seria.gacha`) yang mendukung Paper 1.21.4, NMS Mojang mappings, dan Paper Dialogs.

---

## 🛠️ 1. Spesifikasi Menu YAML Baru (`menus/*.yml`)

Seluruh menu YAML di seluruh plugin Seria kini menggunakan struktur berikut:

```yaml
# ==============================================================================
# Seria Menu Configuration
# ==============================================================================

menu-parameter:
  menu-title: "<title-color><bold>MENU TITLE"
  open-sound: "BLOCK_CHEST_OPEN:1.0:1.0"
  type: CHEST
  rows: 6
  parameters:
    category: "general"

variables:
  current_page: 1

layout:
  - "#########"
  - "#.......#"
  - "#.......#"
  - "#.......#"
  - "#.......#"
  - "####<####"

items:
  header:
    slot: 4
    material: PAPER
    hide-tooltip: true
    name: "<title-color><bold>INFO HEADER"
    lore:
      - "<lore-color>Parameter: %parameters_category%"
      - "<lore-color>Halaman: %variables_current_page%"

  dynamic-slot-button:
    slot: "%placeholders_get-selected-navbar-slot%"
    material: NETHER_STAR
    name: "<accentuation-color><bold>Selected Tab"
    view-requirement:
      requirements:
        1:
          type: "placeholder"
          input: "$e(%variables_current_page% + 1)"
          comparison: ">="
          output: "2"
    actions:
      - "[open-menu] sub-menu"
      - "[refresh]"

fillers:
  glass:
    slots: [0, 1, 2, 3, 5, 6, 7, 8]
    material: GRAY_STAINED_GLASS_PANE
    name: " "
```

### Fitur Engine Menu yang Didukung:
- **Semantic Color Tags**:
  - `<title-color>` : Hex `#E87A90`
  - `<sub-title-color>` : Hex `#F4A7B9`
  - `<lore-color>` : Hex `#FEDFE1`
  - `<accentuation-color>` : Hex `#D05A3F`
  - `<money-color>` : Hex `#E198B4`
  - `<gem-color>` : Hex `#9E7A7E`
  - `<risenite-color>` : Hex `#91B493`
  - `<common-color>` : Hex `#A8A6A8`
  - `<error-color>` : Hex `#C73E3A`
  - `<action-color>` : Hex `#F1948A`
- **Actions**: `[open-menu] <id>`, `[close]`, `[player] <cmd>`, `[console] <cmd>`, `[refresh]`.
- **Dynamic Slot Matching**: Placeholder slot string `%placeholders_...%`, multi-slot arrays `[0, 1, 2]`, dan layout grid strings.

---

## ⛏️ 2. Spesifikasi Skema Job Baru (`jobs/*.yml`)

File konfigurasi pekerjaan (`miner.yml`, `farmer.yml`, `fisherman.yml`) telah diperbarui dengan formula matematika dinamis:

```yaml
job-name: "Miner"
job-display-name: "<accentuation-color><bold>MINER"
icon: DIAMOND_PICKAXE
max-level: 100

# Polynomial & Mathematical Progression Equations
leveling-progression-equation: "round(100 * (joblevel ^ 1.8), 0)"
income-progression-equation: "round(10 + (joblevel * 2.5), 2)"
points-progression-equation: "round(1 + (joblevel * 0.5), 0)"
experience-progression-equation: "round(5 * (joblevel ^ 1.2), 0)"

reward-levels:
  1:
    commands:
      - "eco give %player% 100"
    messages:
      - "<accentuation-color>Selamat! Anda mencapai Level 1 Miner!"
    display-rewards:
      - "<lore-color>+ $100 Saldo"
  100:
    commands:
      - "eco give %player% 50000"
      - "crate give %player% master_key 1"
    messages:
      - "<accentuation-color><bold>MASTER MINER! Level Maksimal Tercapai!"
    display-rewards:
      - "<lore-color>+ $50,000 Saldo"
      - "<lore-color>+ 1x Master Crate Key"
```

---

## 📦 3. Daftar Plugin Seria yang Telah Diperbarui

| Plugin | Versi | Status | Modul / Pembaruan |
|---|---|---|---|
| **`SeriaCore`** | 2.0.0 | ✅ Built | MenuRequirementEvaluator, MiniMessage Semantic Tags, Dynamic Slots & Parameters |
| **`SeriaJobs`** | 2.0.0 | ✅ Built | Math equations, 100-level reward tiers, YAML Menus (actions, menu, leaderboard) |
| **`SeriaGacha`** | 2.0.0 | ✅ Built | New plugin refactor (NMS 1.21.4, Paper Dialog stubs, modern GUI) |
| **`SeriaCrafting`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/crafting.yml`) |
| **`SeriaCrate`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/crates.yml`) |
| **`SeriaMinions`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/minions.yml`) |
| **`SeriaGens`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/gens.yml`) |
| **`SeriaFarm`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/farm.yml`) |
| **`SeriaFortune`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/fortune.yml`) |
| **`SeriaMagicBundle`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/bundle.yml`) |
| **`SeriaPrestige`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/prestige.yml`) |
| **`SeriaTranslator`**| 4.1.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/translator.yml`) |
| **`SeriaTree`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/skilltree.yml`) |
| **`SeriaWardrobe`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/wardrobe.yml`) |
| **`SeriaAEFusion`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/aefusion.yml`) |
| **`SeriaBooster`** | 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/booster.yml`) |
| **`SeriaCollection`**| 2.0.0 | ✅ Updated | Standardized YAML menu schemas (`menus/default.yml`, `menus/collection.yml`) |

---

## 🔧 Build & Deployment Guide

Untuk mem-build ulang modul utama:

```bash
# Build SeriaCore
cd SeriaCore
mvn clean install -DskipTests

# Build SeriaJobs
cd ../SeriaJobs
mvn clean package -DskipTests

# Build SeriaGacha
cd ../SeriaGacha
./gradlew build
```

Semua file `.jar` output akan tersimpan di direktori `target/` atau `build/libs/` masing-masing plugin.
