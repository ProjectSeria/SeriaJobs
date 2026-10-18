# Project Seria - Comprehensive API Reference (Internal & External)

Dokumen ini merupakan panduan ground truth arsitektur dan referensi API resmi untuk seluruh plugin internal (Project Seria) maupun plugin eksternal utama yang terhubung di server Seria.

---

## 1. MythicMobs API (External)
- **Official Javadoc:** [https://www.mythiccraft.io/javadocs/mythic/](https://www.mythiccraft.io/javadocs/mythic/)
- **Maven Dependency:** `io.lumine:Mythic-Dist:5.6.2` (atau 5.7.2) / Scope: `provided`

### 1.1 Entry Point Utama
```java
import io.lumine.mythic.bukkit.MythicBukkit;

MythicBukkit mythic = MythicBukkit.getPlugin();
```

### 1.2 Mob & Entity API
- **`MobManager / MobExecutor`**: `mythic.getMobManager()`
  - `getActiveMob(UUID uuid)`: Mengambil `Optional<ActiveMob>` dari entitas yang hidup.
  - `getMythicMob(String internalName)`: Mengambil template mob `Optional<MythicMob>`.
  - `spawnMob(String internalName, Location loc)`: Melakukan spawn MythicMob.
- **`ActiveMob` (`io.lumine.mythic.core.mobs.ActiveMob`)**:
  - `getEntity()`: Mendapatkan `AbstractEntity` (dapat dikonversi ke Bukkit dengan `.getBukkitEntity()`).
  - `getType()`: Mendapatkan template `MythicMob`.
  - `getDisplayName()` / `setDisplayName(String)`: Membaca / mengubah display name dinamis mob.
  - `getLevel()` / `setLevel(double)`: Level aktif mob.
  - `getVariables()`: Registry variabel runtime mob.
  - `signalMob(AbstractEntity trigger, String signal)`: Mengirim trigger signal.
- **`MythicMob` (`io.lumine.mythic.api.mobs.MythicMob`)**:
  - `getInternalName()`: ID unik konfigurasi mob (misal: `PumpkinHeadZombie`).
  - `getDisplayName()`: `PlaceholderString` display template.

### 1.3 Event Listeners (`io.lumine.mythic.bukkit.events.*`)
| Event Class | Keterangan & Trigger |
|---|---|
| `MythicMobSpawnEvent` | Dipanggil saat entitas MythicMob berhasil di-spawn (`getMob()`, `getEntity()`, `getMobType()`). |
| `MythicMobPreSpawnEvent` | Dipanggil sebelum spawn (dapat dibatalkan / dimodifikasi). |
| `MythicMobDeathEvent` | Dipanggil saat MythicMob mati (`getKiller()`, `getMob()`, `getDrops()`). |
| `MythicMobDespawnEvent` | Dipanggil saat MythicMob melakukan despawn. |
| `MythicDamageEvent` | Dipanggil saat skill damage MythicMob mengenai target (`getDamage()`, `getCaster()`, `getTarget()`). |
| `MythicMechanicLoadEvent` | Registrasi custom mechanic skill dari plugin external ke MythicMobs. |
| `MythicConditionLoadEvent` | Registrasi custom condition skill ke MythicMobs. |
| `MythicTargeterLoadEvent` | Registrasi custom targeter skill ke MythicMobs. |
| `MythicReloadedEvent` / `MythicPostReloadedEvent` | Dipanggil saat `/mm reload` selesai dieksekusi. |

### 1.4 Placeholder API (`io.lumine.mythic.api.skills.placeholders.*`)
- **`PlaceholderManager` (`mythic.getPlaceholderManager()`)**:
  - `register(String key, Placeholder placeholder)`: Mendaftarkan custom tag/placeholder ke MythicMobs.
  - `register(String[] keys, Placeholder placeholder)`: Mendaftarkan alias placeholder.
- **`PlaceholderString`**:
  - Digunakan MythicMobs untuk string dinamis (mendukung format placeholder internal & PAPI).

---

## 2. MMOItems & MythicLib API (External)
- **Maven Dependency:** `net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT`, `io.lumine:MythicLib-dist:1.7.1-SNAPSHOT`

### 2.1 Entry Point & Item Generation
```java
import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.api.item.NBTItem;

ItemStack item = MMOItems.plugin.getItem("TYPE", "ID");
NBTItem nbt = NBTItem.get(item);
```
### 2.2 Event Listeners
- **`ItemBuildEvent` (`net.Indyuce.mmoitems.api.event.ItemBuildEvent`)**:
  - Dipanggil saat MMOItems selesai membuat ItemStack. Digunakan `SeriaCore` (`MMOItemsTagListener`) untuk inject parsing tag `<sc:...>` ke Display Name & Lore.

---

## 3. PacketEvents API (External)
- **Maven Dependency:** `com.github.retrooper.packetevents:spigot:2.7.0+` / Scope: `compileOnly`

### 3.1 Packet Interception
```java
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;

PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.NORMAL);
```
- **Packet Processors di SeriaPrefix:**
  - `WrapperPlayServerSystemChatMessage` (`PacketType.Play.Server.SYSTEM_CHAT_MESSAGE`)
  - `WrapperPlayServerActionBar` (`PacketType.Play.Server.ACTION_BAR`)
  - `WrapperPlayServerBossBar` (`PacketType.Play.Server.BOSS_BAR`)
  - `WrapperPlayServerSetTitleText` & `WrapperPlayServerSetTitleSubtitle`

---

## 4. SeriaCore Backbone API (Internal - Seria Core Engine)
- **Package:** `id.seria.core`
- **Main Class:** `id.seria.core.SeriaCorePlugin`

### 4.1 Tag & Semantic Formatting Engine
- **`BaseSeriaMenu.formatSemanticColorTags(String text)`**:
  - Mengonversi seluruh format semantic Seria:
    - `<sc:prefix>`, `<p:prefix>`, `%sc_prefix%`
    - `<sc:menu-prefix>`, `%sc_menu-prefix%`
    - Tag warna dinamis: `<sc:<tag>>` (misal `<sc:halloween-color>`)
    - Tag icon/lore: `<sc:<tag>-icon>`, `<sc:<tag>-lore>`
- **`CustomPlaceholderManager`**:
  - `resolveSemanticTag(Player player, String params)`: Resolver PAPI untuk expansion `%sc_<params>%`.
  - Otomatis sinkron membaca definisi dari file placeholder `SeriaPrefix/placeholders/*.yml`.

### 4.2 Item Provider Registry
- **`ItemProviderRegistry` (`plugin.getItemProviderRegistry()`)**:
  - Provider modular untuk parsing item placeholder:
    - MMOItems (`mmoitems`, `mmo`)
    - MythicMobs (`mythicmobs`, `mythic`, `mm`)
    - EcoItems, Vanilla, HeadDatabase, dll.

---

## 5. SeriaPrefix API (Internal - Shading & Packet Translation)
- **Package:** `id.seria.prefix`
- **Tujuan:** Engine registrasi placeholder dan filter packet tingkat rendah.
- **Catatan Arsitektur:** `SeriaPrefix` hanya mendaftarkan placeholder dan packet filtering. Seluruh logika inti pemrosesan semantik warna dan integrasi plugin ditangani oleh `SeriaCore`.

---

## 6. FancyHolograms API (External)
- **Official Docs:** [https://fancyinnovations.com/docs/minecraft-plugins/fancyholograms/api/getting-started](https://fancyinnovations.com/docs/minecraft-plugins/fancyholograms/api/getting-started)
- **Repository Maven:**
  ```xml
  <repository>
      <id>fancyinnovations-releases</id>
      <name>FancyInnovations Repository</name>
      <url>https://repo.fancyinnovations.com/releases</url>
  </repository>
  ```
- **Maven Dependency:** `de.oliver:FancyHolograms:2.9.1` / Scope: `provided`

### 6.1 Entry Point & Hologram Manager
```java
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.hologram.Hologram;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;

HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
```

### 6.2 Data Model & Manipulasi Text Hologram
- **`Hologram` (`de.oliver.fancyholograms.api.hologram.Hologram`)**:
  - `getData()`: Mendapatkan data konfigurasi hologram (`HologramData`).
  - `queueUpdate()` / `forceUpdate()`: Memicu render ulang hologram ke viewer.
  - `refreshForViewers()`: Refresh tampilan hologram untuk pemain yang berada di radius jangkauan.
- **`TextHologramData` (`de.oliver.fancyholograms.api.data.TextHologramData`)**:
  - `getText()`: Membaca list baris teks (`List<String>`).
  - `setText(List<String>)`: Mengubah seluruh baris teks hologram.
  - `addLine(String)` / `removeLine(int)`: Menambah atau menghapus baris individual.
  - `getTextUpdateInterval()` / `setTextUpdateInterval(int)`: Interval auto-update placeholder (-1 untuk disable).
  - `getTextAlignment()` / `setTextAlignment(TextAlignment)`: Alignment teks (`LEFT`, `CENTER`, `RIGHT`).

### 6.3 Semantic MiniMessage Tag Injection & Cross-Version Architecture
> [!NOTE]
> Dependency internal `ChatColorHandler` mengalami perubahan struktur paket drastis antar-versi FancyHolograms:
> - **v2.9.x**: `de.oliver.fancyholograms.libs.chatcolorhandler.parsers.Parser`
> - **v2.10.x**: `de.oliver.fancyholograms.libs.chatcolorhandler.common.parser.Parser`
> - **v2.11.x**: Refaktor ke `org.lushplugins` / Kyori MiniMessage langsung.
> 
> Mengimpor `ChatColorHandler` secara langsung di compile-time akan menyebabkan `NoClassDefFoundError` saat dijalankan pada versi yang berbeda.
> 
> **Pendekatan Standar & Stabil (Official API):**
> Format seluruh baris teks hologram langsung melalui model `TextHologramData` menggunakan event lifecycle Bukkit (`HologramCreateEvent`, `HologramUpdateEvent`, `HologramsLoadedEvent`) serta scanning saat plugin startup via `FancyHologramsPlugin.get().getHologramManager().getHolograms()`.
> FancyHolograms secara native merender string yang telah diformat menggunakan Adventure MiniMessage.
>
> ```java
> if (data instanceof TextHologramData textData) {
>     List<String> formatted = textData.getText().stream()
>         .map(line -> BaseSeriaMenu.formatSemanticColorTags(line))
>         .toList();
>     textData.setText(formatted);
>     hologram.queueUpdate();
>     hologram.refreshForViewers();
> }
> ```

### 6.4 Event Listeners (`de.oliver.fancyholograms.api.events.*`)
| Event Class | Keterangan & Trigger |
|---|---|
| `HologramCreateEvent` | Dipanggil saat hologram baru dibuat di server (`getHologram()`, `getPlayer()`). |
| `HologramUpdateEvent` | Dipanggil saat properti atau teks hologram diubah (`getUpdatedData()`, `getModification()`). |
| `HologramsLoadedEvent` | Dipanggil saat FancyHolograms selesai memuat semua file hologram dari disk (`getManager()`). |
| `HologramDeleteEvent` | Dipanggil saat hologram dihapus. |
| `HologramShowEvent` / `HologramHideEvent` | Dipanggil saat hologram ditampilkan atau disembunyikan dari pemain. |

