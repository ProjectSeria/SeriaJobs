package id.seria.gacha.editor;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EditorFacade {
   private static final String MAIN_LAYOUT_CLASS = "id.seria.gacha.editor.layouts.EditorMainLayout";
   private static PhoenixCrates PLUGIN;
   private static final Map<UUID, EditorFacade.EditorLastSession> SESSIONS = new HashMap<>();
   private static final double MILLIS_TO_RELEASE = 5000.0;

   public EditorFacade() {
   }

   private static EditorFacade.EditorLayout newMainLayout() throws ReflectiveOperationException {
      return (EditorFacade.EditorLayout)Class.forName("id.seria.gacha.editor.layouts.EditorMainLayout")
         .getDeclaredConstructor()
         .newInstance();
   }

   public static void saveCrate(ContainerView view, CrateType crateType) {
      Player player = view != null ? view.getViewer() : null;

      try {
         if (crateType.isRegistered()) {
            Crates.getCratesManager().saveCrateType(crateType);
         } else {
            Crates.getCratesManager().registerCrateType(crateType, true);
         }
      } catch (Exception e) {
         if (player != null) {
            Translations.error(player, e);
            player.closeInventory();
         } else {
            e.printStackTrace();
         }
      }
   }

   public static void saveCrate(ContainerView view, CrateType crateType, CustomReward reward) {
      if (!reward.isRegistered()) {
         crateType.addReward(reward);
      }

      saveCrate(view, crateType);
   }

   public static void saveKey(ContainerView view, Key key) {
      Player player = view.getViewer();

      try {
         if (!key.isRegistered()) {
            Crates.getKeysManager().registerKey(key, true);
         } else {
            Crates.getKeysManager().saveKey(key);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   public static void initialize(PhoenixCrates plugin) {
      if (PLUGIN == null) {
         PLUGIN = plugin;
         plugin.getEventBus().registerListener(new Listener() {
            @EventHandler
            private void onInventoryClose(InventoryCloseEvent event) {
               Player player = (Player)event.getPlayer();
               Inventory inventory = event.getInventory();
               InventoryHolder holder = inventory.getHolder();
               if (holder instanceof EditorFacade.EditorLayoutView) {
                  EditorFacade.setLastClosedMenu(player, ((EditorFacade.EditorLayoutView)holder).getContainer());
               }
            }
         });
      }
   }

   public static void openLayout(Player viewer, ContainerLayout layout) {
      try {
         layout.open(viewer);
      } catch (Exception e) {
         Translations.error(viewer, e);
         viewer.closeInventory();
      }
   }

   public static void openEditor(Player viewer) {
      EditorFacade.EditorLastSession session = SESSIONS.get(viewer.getUniqueId());
      if (session != null && System.currentTimeMillis() >= session.closedMillis + 5000.0) {
         SESSIONS.remove(viewer.getUniqueId());
         session = null;
      }

      if (session != null) {
         session.layout.open(viewer);
      } else {
         try {
            newMainLayout().open(viewer);
         } catch (Exception e) {
            Translations.error(viewer, e);
            viewer.closeInventory();
         }
      }
   }

   public static void setLastClosedMenu(Player player, ContainerLayout layout) {
      SESSIONS.put(player.getUniqueId(), new EditorFacade.EditorLastSession(System.currentTimeMillis(), layout));
   }

   public static class EditorHolderWrapper extends CustomInventory.HolderWrapper {
      public EditorHolderWrapper(CustomInventory sustainer) {
         super(sustainer);
      }
   }

   private static class EditorLastSession {
      private final long closedMillis;
      private final ContainerLayout layout;

      @Generated
      public EditorLastSession(long closedMillis, ContainerLayout layout) {
         this.closedMillis = closedMillis;
         this.layout = layout;
      }
   }

   public abstract static class EditorLayout extends ContainerLayout {
      public EditorLayout(Translatable title, ContainerLayout parent) {
         super(EditorFacade.PLUGIN, Translations.getProvider(), title, parent);
      }

      @Override
      public void open(Player viewer) {
         this.open(new EditorFacade.EditorLayoutView(this, viewer));
      }

      public PhoenixCrates getPlugin() {
         return (PhoenixCrates)this.plugin;
      }

      protected ComponentData createData(ItemStack item, Translatable title, Translatable description) {
         return this.createData(0, 0, item, title, description);
      }

      protected ComponentData createData(ItemBuilder builder, Translatable title, Translatable description) {
         return this.createData(0, 0, builder.build(), title, description);
      }

      protected ComponentData createData(XMaterial material, Translatable title, Translatable description) {
         return new ComponentData(material, title, description);
      }

      protected ComponentData createData(XMaterial material, String title, String... description) {
         return this.createData(0, 0, material, title, description);
      }

      protected ComponentData createData(ItemStack item, String title, String... description) {
         return this.createData(0, 0, item, title, description);
      }

      protected ComponentData createData(ItemBuilder item, String title, String... description) {
         return this.createData(0, 0, item.build(), title, description);
      }

      protected ComponentData createData(int x, int y, ItemStack item, Translatable title, Translatable description) {
         return new ComponentData(InventoryUtil.getPositionRaw(x, y), item, title, description);
      }

      protected ComponentData createData(int x, int y, ItemBuilder item, Translatable title, Translatable description) {
         return new ComponentData(InventoryUtil.getPositionRaw(x, y), item.build(), title, description);
      }

      @Deprecated
      protected ComponentData createData(int x, int y, ItemStack item, String title, String... description) {
         return new ComponentData(InventoryUtil.getPositionRaw(x, y), item, Translatable.literal(title), Translatable.literal(description));
      }

      @Deprecated
      protected ComponentData createData(int x, int y, ItemBuilder item, String title, String... description) {
         return new ComponentData(InventoryUtil.getPositionRaw(x, y), item.build(), Translatable.literal(title), Translatable.literal(description));
      }

      @Deprecated
      protected ComponentData createData(int x, int y, XMaterial material, String title, String... description) {
         return new ComponentData(InventoryUtil.getPositionRaw(x, y), material.parseItem(), Translatable.literal(title), Translatable.literal(description));
      }

      @Deprecated
      protected ComponentData createData(int x, int y, XMaterial material) {
         return new ComponentData(
            InventoryUtil.getPositionRaw(x, y), material.parseItem(), Translatable.literal(this.title), Translatable.literal(new ArrayList())
         );
      }

      public Translatable label(String key) {
         return Translatable.key("labels." + key);
      }
   }

   public static class EditorLayoutView extends ContainerView {
      public EditorLayoutView(EditorFacade.EditorLayout container, Player viewer) {
         super(container, viewer);
      }

      public EditorFacade.EditorLayout getContainer() {
         return (EditorFacade.EditorLayout)super.getContainer();
      }
   }

   public static class EditorPaginationWrapper extends CustomInventory.PaginationWrapper {
      public EditorPaginationWrapper(CustomInventory sustainer) {
         super(sustainer);
      }
   }

   @Deprecated
   public abstract static class LegacyEditorLayout extends CustomInventory {
      public LegacyEditorLayout(String name, int numRows) {
         super(EditorFacade.PLUGIN, name, numRows);
      }

      protected String[] t(String key, Object... arguments) {
         return Translations.t(Translatable.key(key, arguments));
      }

      protected String[] t(Translatable key) {
         return Translations.t(key);
      }

      protected String t(boolean state) {
         String key = state ? "labels.yes-word" : "labels.no-word";
         return Translations.t(Translatable.key(key))[0];
      }

      public Translatable label(String key) {
         return Translatable.key("labels." + key);
      }
   }

   public static class LegacyEditorWrapper extends EditorFacade.EditorLayout {
      private final CustomInventory inventory;

      public LegacyEditorWrapper(CustomInventory inventory) {
         super(Translatable.literal(inventory.getTitle()), null);
         this.inventory = inventory;
      }

      @Override
      public void open(Player viewer) {
         this.inventory.open(viewer);
      }

      @Override
      public void onPrepare(ContainerView containerView) {
      }

      @Override
      public void onRender(RenderViewContext context) {
         this.inventory.open(context.getViewer());
      }
   }
}
