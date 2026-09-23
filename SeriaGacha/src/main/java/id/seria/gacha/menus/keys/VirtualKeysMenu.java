package id.seria.gacha.menus.keys;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.menus.GenericConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.PaginatedContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.keys.Key;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.entity.Player;

public class VirtualKeysMenu extends GenericConfigurableLayout<VirtualKeysMenu.VirtualKeysMenuConfiguration> {
   public VirtualKeysMenu(VirtualKeysMenu.VirtualKeysMenuConfiguration configuration) {
      super(configuration);
   }

   @Override
   public void onPrepare(ContainerView view) {
      SimplePaginatedList<Key> list = view.computeMetadataIfAbsent("virtual-keys.list", this::createPaginationComponent);
      this.addComponent(view, list);
   }

   @Override
   public void onOpen(OpenViewContext context) {
      super.onOpen(context);
      context.setContainerType(ContainerType.fromRows(this.getConfiguration().getSettings().getRows()));
   }

   private SimplePaginatedList<Key> createPaginationComponent() {
      return (SimplePaginatedList<Key>)this.<Key>createSimplePagination(
            this.getConfiguration(),
            Key.class,
            view -> {
               Player player = view.getViewer();
               return Crates.getKeysManager()
                  .getRegisteredKeys()
                  .stream()
                  .filter(k -> k != null && k.isVirtual() && k.isEnabled())
                  .filter(k -> KeyFacade.countTotalAvailableKeys(k, player) > 0)
                  .collect(Collectors.toList());
            }
         )
         .setOnItemRender(
            (context, index, item, key) -> {
               Player player = context.getViewer();
               int amount = KeyFacade.countTotalAvailableKeys(key, player);
               ItemBuilder builder = ItemBuilder.of(key.createItemStack());
               List<String> lore = this.r(
                  this.getConfiguration().getKeyItem().getLore(),
                  "%key_name%",
                  key.getFormattedDisplayName(player),
                  "%amount%",
                  amount,
                  "%item_lore%",
                  builder.getLore()
               );
               item.withItem(builder.build())
                  .withDisplayName(
                     this.r(this.getConfiguration().getKeyItem().getDisplayName(), "%key_name%", key.getFormattedDisplayName(player), "%amount%", amount)
                  )
                  .withAmount(amount)
                  .withLore(lore);
            }
         );
   }

   public static class KeyItemConfiguration {
      @ConfigField
      private String displayName;
      @ConfigField
      private List<String> lore;

      @Generated
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public KeyItemConfiguration() {
      }

      @Generated
      public KeyItemConfiguration(String displayName, List<String> lore) {
         this.displayName = displayName;
         this.lore = lore;
      }
   }

   public static class VirtualKeysMenuConfiguration extends PaginatedContainerConfiguration {
      @ConfigField(path = "key-item")
      private final VirtualKeysMenu.KeyItemConfiguration keyItem;

      public VirtualKeysMenuConfiguration() {
         this.settings.setType("VIRTUAL_KEYS");
         this.settings.setTitle("Your Virtual Keys");
         this.settings.setRows(6);
         this.settings.setRefreshEnabled(true);
         this.settings.setRefreshRate(20);
         this.items.clear();
         ContainerItemsConfiguration.ItemConfiguration downFiller = new ContainerItemsConfiguration.ItemConfiguration(
            new SlotCompound(45, 53),
            new ComplexMaterial(XMaterial.BLACK_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "&e",
            Lists.newArrayList(),
            Lists.newArrayList()
         );
         downFiller.setHideTooltip(true);
         this.items.put("down-filler", downFiller);
         this.items
            .put(
               "close-menu",
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(49, 49),
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&aClose Menu",
                  Lists.newArrayList(new String[]{"&7Click here to close this menu.", "", "&6Left-Click: &eClose menu."}),
                  Lists.newArrayList(new String[]{"[CLOSE_INVENTORY]"})
               )
            );
         this.pagination.setAlignItems(PaginationSettings.ItemsAlign.LEFT);
         this.pagination.setSlots(SlotCompound.fromGrid(10, 3, 7));
         this.pagination
            .setFillItem(
               new ContainerItemsConfiguration.NonSlotItemConfiguration(
                  new ComplexMaterial(XMaterial.AIR), 0, "", false, "", Lists.newArrayList(), Lists.newArrayList()
               )
            );
         this.pagination
            .setPreviousItem(
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(18),
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&6Previous page - Page %page%",
                  Lists.newArrayList(new String[]{"&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<"}),
                  Lists.newArrayList()
               )
            );
         this.pagination
            .setNextItem(
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(26),
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&6Next page - Page %page%",
                  Lists.newArrayList(new String[]{"&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>"}),
                  Lists.newArrayList()
               )
            );
         this.keyItem = new VirtualKeysMenu.KeyItemConfiguration("&e%key_name%", Lists.newArrayList(new String[]{"%item_lore%", "", "&fAmount: &e%amount%"}));
      }

      public VirtualKeysMenu.VirtualKeysMenuConfiguration clone() {
         return new VirtualKeysMenu.VirtualKeysMenuConfiguration();
      }

      @Generated
      public VirtualKeysMenu.KeyItemConfiguration getKeyItem() {
         return this.keyItem;
      }
   }
}
