package id.seria.gacha.limbo.item.hooks;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.common.utils.configuration.adapter.Adaptable;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.adapter.BaseItemAdapter;
import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Adaptable(adapter = BaseItemAdapter.class)
public class HookItem extends BaseItem {
   public static final Set<String> NAMESPACES = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList("itemsadder", "nexo", "mythicitem", "mmoitem", "craftengine", "hdb", "executableitem", "eco"))
   );
   protected final String namespace;
   protected final String id;

   public HookItem(String namespace, String id) {
      this.namespace = namespace;
      this.id = id;
   }

   @NotNull
   @Override
   public ItemStack build() {
      RItemStack item = this.createDefault();
      return item != null ? this.override(item).bukkit() : this.createFailedId();
   }

   @Nullable
   @Override
   protected RItemStack createDefault() {
      NamespacedItemHook hook = PhoenixCrates.getLegacyInstance().getHookRegistry().getNamespacedHook(this.namespace);
      if (hook == null) {
         return this.createFailedHook();
      }

      ItemStack extracted = hook.getItemFromId(this.id);
      return extracted == null ? null : RItemStack.of(extracted);
   }

   @NotNull
   @Override
   public BaseItem clone() {
      return new HookItem(this.namespace, this.id);
   }

   @Override
   public String getNamespace() {
      return this.namespace;
   }

   private ItemStack createFailedId() {
      RItemStack stack = RItemStack.of(XMaterial.DIRT);
      stack.displayName(Text.plain("\u00a7cInvalid " + StringUtil.humanize(this.getNamespace()) + " Item"));
      stack.lore(
         Text.plain(Lists.newArrayList(new String[]{"\u00a7cInvalid Item Id: " + this.id, "", "\u00a7e\u00a7nContact the server administrador about this."}))
      );
      return stack.bukkit();
   }

   private RItemStack createFailedHook() {
      RItemStack stack = RItemStack.of(XMaterial.DIRT);
      stack.displayName(Text.plain("\u00a7cInvalid " + StringUtil.humanize(this.getNamespace()) + " Item"));
      stack.lore(Text.plain(Lists.newArrayList(new String[]{"\u00a7cHook not available!", "\u00a7e\u00a7nContact the server administrador about this."})));
      return stack;
   }

   @Generated
   public String getId() {
      return this.id;
   }
}
