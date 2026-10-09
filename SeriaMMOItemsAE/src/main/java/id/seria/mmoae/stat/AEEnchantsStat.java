package id.seria.mmoae.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AEEnchantsStat extends StringListStat {

    public static final String STAT_ID = "AE_ENCHANTS";
    public static final String NBT_TAG = "AE_ENCHANTS";

    public AEEnchantsStat() {
        super(
                STAT_ID,
                Material.ENCHANTED_BOOK,
                "AE Enchants",
                new String[]{
                        "AdvancedEnchantments custom enchants",
                        "applied when the item is generated or updated.",
                        "",
                        ChatColor.YELLOW + "Formats supported:",
                        ChatColor.GRAY + "  ae-enchants:",
                        ChatColor.GRAY + "    decapitation: 3",
                        ChatColor.GRAY + "    critical: 3",
                        ChatColor.GRAY + "  or:",
                        ChatColor.GRAY + "  ae-enchants:",
                        ChatColor.GRAY + "    - 'decapitation 3'"
                },
                new String[]{"!block", "all"}
        );
    }

    @Override
    public StringListData whenInitialized(Object object) {
        return new StringListData(parseRawObject(object));
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        super.whenApplied(item, data);
        if (data.getList() != null && !data.getList().isEmpty()) {
            item.addItemTag(new ItemTag(NBT_TAG, String.join(";;", data.getList())));
        }
    }

    /**
     * Flexible parser supporting both YAML Map/Section format and List format.
     */
    public static List<String> parseRawObject(Object object) {
        List<String> list = new ArrayList<>();
        if (object == null) return list;

        if (object instanceof ConfigurationSection sec) {
            for (String key : sec.getKeys(false)) {
                int lvl = sec.getInt(key, 1);
                list.add(key.trim() + " " + lvl);
            }
        } else if (object instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    String key = entry.getKey().toString().trim();
                    int lvl = 1;
                    if (entry.getValue() instanceof Number) {
                        lvl = ((Number) entry.getValue()).intValue();
                    } else if (entry.getValue() != null) {
                        try {
                            lvl = Integer.parseInt(entry.getValue().toString().trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    list.add(key + " " + lvl);
                }
            }
        } else if (object instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item != null) {
                    String str = item.toString().trim();
                    if (!str.isEmpty()) {
                        str = str.replace(":", " ");
                        list.add(str.replaceAll("\\s+", " "));
                    }
                }
            }
        } else if (object instanceof String str) {
            str = str.trim();
            if (!str.isEmpty()) {
                str = str.replace(":", " ");
                list.add(str.replaceAll("\\s+", " "));
            }
        }

        return list;
    }
}
