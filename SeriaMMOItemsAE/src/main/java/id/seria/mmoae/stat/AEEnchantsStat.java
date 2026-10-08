package id.seria.mmoae.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

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
                        ChatColor.YELLOW + "Format: <enchant_name> <level>",
                        ChatColor.GRAY + "Example: decapitation 3"
                },
                new String[]{"!block", "all"}
        );
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        super.whenApplied(item, data);
        if (data.getList() != null && !data.getList().isEmpty()) {
            item.addItemTag(new ItemTag(NBT_TAG, String.join(";;", data.getList())));
        }
    }
}
