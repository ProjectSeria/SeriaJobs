package id.seria.gacha.editor.layouts;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.AsyncPaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.api.PhoenixPluginsAPI;
import id.seria.gacha.lib.common.utils.api.Product;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.PlayerUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.bukkit.event.inventory.ClickType;

public class EditorAddonsLayout extends EditorFacade.EditorLayout {
   private static final String STORE_BASE_URL = "https://www.phoenixplugins.com/product/";
   private static final String DOCS_BASE_URL = "https://phoenx.wiki/phoenix-crates/add-ons/";
   private static final String DOCS_INDEX_URL = "https://phoenx.wiki/phoenix-crates";
   private static final String DISCONTINUED_MARKER = "(discontinued)";
   private static final String ANIMATION_MARKER = "animation";
   private static final String FILTER_TYPE_KEY = "editor.addons.filter.type";
   private static final String CATEGORY_TYPE_KEY = "editor.addons.category.type";
   private static final AtomicBoolean ADDONS_FETCHED = new AtomicBoolean(false);
   private static final List<Product> CACHED_ADDONS = new ArrayList<>();

   public EditorAddonsLayout(EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.addons"), parent);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(2, 5),
               this.createData(XMaterial.HOPPER, Translatable.key("editor.addons.filter.title"), Translatable.key("editor.addons.filter.description")),
               Arrays.asList(EditorAddonsLayout.FilterType.values()),
               this.getFilterType(view)
            )
            .setItemFormatter(EditorAddonsLayout.FilterType::getTitle)
            .setOnOptionChange(value -> {
               this.setFilterType(view, value);
               view.updateView();
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(3, 5),
               this.createData(
                  ItemBuilder.of(XMaterial.FIREWORK_ROCKET).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.addons.category.title"),
                  Translatable.key("editor.addons.category.description")
               ),
               Arrays.asList(EditorAddonsLayout.CategoryType.values()),
               this.getCategoryType(view)
            )
            .setItemFormatter(EditorAddonsLayout.CategoryType::getTitle)
            .setOnOptionChange(value -> {
               this.setCategoryType(view, value);
               view.updateView();
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         this.createComponent(
               SlotCompound.from(7, 5),
               this.createData(XMaterial.BOOK, Translatable.key("editor.addons.install.title"), Translatable.key("editor.addons.install.description"))
            )
            .setFixedPosition(true)
      );
      this.addComponent(view, new EditorAddonsLayout.AddonsListComponent());
   }

   private EditorAddonsLayout.FilterType getFilterType(ContainerView view) {
      return view.getMetadata("editor.addons.filter.type", EditorAddonsLayout.FilterType.ALL);
   }

   private void setFilterType(ContainerView view, EditorAddonsLayout.FilterType filterType) {
      view.setMetadata("editor.addons.filter.type", filterType, true);
   }

   private EditorAddonsLayout.CategoryType getCategoryType(ContainerView view) {
      return view.getMetadata("editor.addons.category.type", EditorAddonsLayout.CategoryType.ALL);
   }

   private void setCategoryType(ContainerView view, EditorAddonsLayout.CategoryType categoryType) {
      view.setMetadata("editor.addons.category.type", categoryType, true);
   }

   private List<Product> getFilteredAddons(ContainerView view) {
      EditorAddonsLayout.FilterType filterType = this.getFilterType(view);
      EditorAddonsLayout.CategoryType categoryType = this.getCategoryType(view);
      synchronized (CACHED_ADDONS) {
         this.prepareCachedAddons();
         return CACHED_ADDONS.stream().filter(addon -> {
            switch (filterType) {
               case ALL:
               default:
                  return true;
               case FREE:
                  return addon.getOriginalPrice() == 0.0;
               case PREMIUM:
                  return addon.getOriginalPrice() > 0.0;
            }
         }).filter(addon -> {
            switch (categoryType) {
               case ALL:
               default:
                  return true;
               case ANIMATIONS:
                  return isAnimation(addon);
               case FEATURES:
                  return !isAnimation(addon);
               case INSTALLED:
                  return this.isInstalled(addon);
               case NOT_INSTALLED:
                  return !this.isInstalled(addon);
            }
         }).sorted(Comparator.comparing(EditorAddonsLayout::isAnimation)).collect(Collectors.toList());
      }
   }

   private static XMaterial getDisplayMaterial(Product addon) {
      EditorAddonsLayout.AddonDisplay display = EditorAddonsLayout.AddonDisplay.bySlug(getSlug(addon));
      return display != null ? display.material : XMaterial.PAPER;
   }

   private boolean isInstalled(Product addon) {
      EditorAddonsLayout.AddonDisplay display = EditorAddonsLayout.AddonDisplay.bySlug(getSlug(addon));
      if (display == null) {
         return false;
      }

      for (Class<?> loaded : this.getPlugin().getAddonRegistry().getLoadedAddons().keySet()) {
         if (loaded.getName().endsWith(display.mainClass)) {
            return true;
         }
      }

      return false;
   }

   private static String getDocumentationUrl(Product addon) {
      EditorAddonsLayout.AddonDisplay display = EditorAddonsLayout.AddonDisplay.bySlug(getSlug(addon));
      return display != null ? "https://phoenx.wiki/phoenix-crates/add-ons/" + display.documentationPath : "https://phoenx.wiki/phoenix-crates";
   }

   private static String getSlug(Product addon) {
      String url = addon.getUrl();
      return url != null && !url.isEmpty() ? url.substring(url.lastIndexOf(47) + 1) : "";
   }

   private static boolean isAnimation(Product addon) {
      return addon.getName() != null && addon.getName().toLowerCase().contains("animation");
   }

   private void prepareCachedAddons() {
      if (!ADDONS_FETCHED.get()) {
         try {
            synchronized (CACHED_ADDONS) {
               for (Product addon : PhoenixPluginsAPI.fetchProductAddons(this.getPlugin().getPlatform().getProductId())) {
                  if (addon.getName() == null || !addon.getName().toLowerCase().contains("(discontinued)")) {
                     CACHED_ADDONS.add(addon);
                  }
               }

               Collections.shuffle(CACHED_ADDONS);
            }

            ADDONS_FETCHED.set(true);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   private enum AddonDisplay {
      CRATE_CLAIM("crate-claim", XMaterial.ENDER_CHEST, "crate-claim", "id.seria.gacha.addons.claim.CrateClaimAddon"),
      MASS_OPENING("mass-opening", XMaterial.RESIN_BRICK, "mass-opening", "id.seria.gacha.addons.massopening.CrateMassOpeningAddon"),
      RARITIES("rarities", XMaterial.DIAMOND, "rarity", "id.seria.gacha.addons.rarity.CrateRarityAddon"),
      LOOTBOXES("lootboxes", XMaterial.RED_SHULKER_BOX, "lootbox", "id.seria.gacha.addons.lootbox.CrateLootboxAddon"),
      LAST_WINNER("last-winner", XMaterial.NAME_TAG, "last-winner", "id.seria.gacha.addons.lastwinner.LastWinnerAddon"),
      LEADERBOARD("leaderboard", XMaterial.EMERALD, "leaderboard", "id.seria.gacha.addons.leaderboard.LeaderboardAddon"),
      REDIS("redis", XMaterial.REDSTONE_BLOCK, "redis", "id.seria.gacha.addons.redis.RedisAddon"),
      REWARD_ON_LOOP("reward-on-loop", XMaterial.CLOCK, "reward-on-loop", "id.seria.gacha.addons.rewardonloop.RewardOnLoopAddon"),
      IP_LIMIT("iplimit", XMaterial.REDSTONE, "ip-limit", "id.seria.gacha.addons.iplimit.IpLimitAddon"),
      DELUXE_ANIMATIONS(
         "deluxe-animations",
         XMaterial.GOLD_INGOT,
         "animations/deluxe-animations",
         "id.seria.gacha.addons.deluxeanimations.DeluxeAnimationsAddon"
      ),
      HALLOWEEN_ANIMATIONS(
         "halloween-crate-animations",
         XMaterial.PUMPKIN_PIE,
         "animations/halloween-crate-animations",
         "id.seria.gacha.addons.halloween.v1.PhoenixHalloweenAddon"
      ),
      CHRISTMAS_ANIMATIONS(
         "christmas-crate-animations-v1",
         XMaterial.SWEET_BERRIES,
         "animations/christmas-crate-animations",
         "id.seria.gacha.addons.christmas.v1.PhoenixChristmasAddon"
      );

      private static final String SLUG_SUFFIX = "-addon";
      private final String slug;
      private final XMaterial material;
      private final String documentationPath;
      private final String mainClass;

      AddonDisplay(final String slug, final XMaterial material, final String documentationPath, final String mainClass) {
         this.slug = slug;
         this.material = material;
         this.documentationPath = documentationPath;
         this.mainClass = mainClass;
      }

      public static EditorAddonsLayout.AddonDisplay bySlug(String slug) {
         String normalized = slug.endsWith("-addon") ? slug.substring(0, slug.length() - "-addon".length()) : slug;

         for (EditorAddonsLayout.AddonDisplay display : values()) {
            if (display.slug.equals(normalized)) {
               return display;
            }
         }

         return null;
      }
   }

   public class AddonsListComponent extends AsyncPaginatedList<Product> {
      public AddonsListComponent() {
         super(SlotCompound.fromGrid(10, 2, 7), new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 4), SlotCompound.from(9, 4)));
      }

      @Override
      public CompletableFuture<List<Product>> onPrepareItems(ContainerView view, int startIndex, int endIndex) {
         return CompletableFuture.supplyAsync(
            () -> {
               List<Product> addons = EditorAddonsLayout.this.getFilteredAddons(view);
               return addons.isEmpty()
                  ? Collections.emptyList()
                  : new ArrayList<>(addons.subList(Math.min(startIndex, addons.size()), Math.min(endIndex, addons.size())));
            },
            Utilities.getAsyncService()
         );
      }

      @Override
      public CompletableFuture<Integer> countMaxItems(ContainerView view) {
         return CompletableFuture.supplyAsync(() -> EditorAddonsLayout.this.getFilteredAddons(view).size(), Utilities.getAsyncService());
      }

      @Override
      public void onRenderItems(RenderViewContext context) {
         int maxItems = (Integer)context.getContainerView().getMetadata(String.format(MAX_ITEMS_KEY, this.id));
         if (maxItems == 0) {
            VirtualItem.from(XMaterial.BARRIER)
               .withSlots(SlotCompound.from(5, 3))
               .withDisplayName(EditorAddonsLayout.this.t("editor.addons.empty.title")[0])
               .withLore(EditorAddonsLayout.this.t("editor.addons.empty.description"))
               .render(context);
         } else {
            super.onRenderItems(context);
         }
      }

      public void onRenderItem(RenderViewContext context, int index, VirtualItem virtualItem, Product addon) {
         String displayedPrice = null;
         if (addon.getOriginalPrice() == 0.0) {
            displayedPrice = EditorAddonsLayout.this.t("labels.free")[0];
         } else {
            displayedPrice = "\u00a7a" + Constants.DECIMAL2_FORMAT.format(Math.min(addon.getOriginalPrice(), addon.getCurrentPrice())) + "\u20ac";
            if (addon.getCurrentPrice() < addon.getOriginalPrice()) {
               displayedPrice = displayedPrice
                  + " \u00a7c\u00a7m"
                  + Constants.DECIMAL2_FORMAT.format(Math.min(addon.getOriginalPrice(), addon.getCurrentPrice()))
                  + "\u20ac";
            }
         }

         List<String> lore = new ArrayList<>(
            Arrays.asList(
               EditorAddonsLayout.this.t(
                  "editor.addons.addon.description",
                  "%description%",
                  StringUtil.divideString(addon.getDescription(), 35),
                  "%price%",
                  displayedPrice,
                  "%installed%",
                  EditorAddonsLayout.this.t(EditorAddonsLayout.this.isInstalled(addon) ? "labels.yes-word" : "labels.no-word")[0]
               )
            )
         );
         if (addon.getCurrentPrice() < addon.getOriginalPrice()) {
            lore.add("\u00a7d\u00a7l" + EditorAddonsLayout.this.t("labels.limited-time-offer")[0].toUpperCase() + "!");
         }

         virtualItem.withItem(EditorAddonsLayout.getDisplayMaterial(addon))
            .withDisplayName(EditorAddonsLayout.this.t("editor.addons.addon.title", "%name%", addon.getName())[0])
            .withLore(
               ClickActionsFacade.applyActions(
                  EditorAddonsLayout.this,
                  lore,
                  new ClickAction(ClickAction.ClickType.LEFT, EditorAddonsLayout.this.label("see-more")),
                  new ClickAction(ClickAction.ClickType.DROP, EditorAddonsLayout.this.label("documentation"))
               )
            )
            .withFlag(Constants.ITEM_FLAGS)
            .whenClick(
               ctx -> {
                  if (ctx.getClick() == ClickType.DROP || ctx.getClick() == ClickType.CONTROL_DROP) {
                     PlayerUtil.sendClickableLink(
                        ctx.getViewer(),
                        EditorAddonsLayout.this.t("editor.addons.addon.documentation", "%name%", addon.getName())[0],
                        EditorAddonsLayout.getDocumentationUrl(addon)
                     );
                  } else if (ctx.isLeftClick()) {
                     PlayerUtil.sendClickableLink(
                        ctx.getViewer(),
                        EditorAddonsLayout.this.t("editor.addons.addon.link", "%name%", addon.getName())[0],
                        "https://www.phoenixplugins.com/product/" + EditorAddonsLayout.getSlug(addon)
                     );
                  }
               }
            );
      }

      public void onItemClick(ClickViewContext context, int index, Product product) {
      }

      @Override
      public VirtualItem prepareLoadingSlot() {
         return new VirtualItem()
            .withItem(XMaterial.GRAY_DYE)
            .withDisplayName(EditorAddonsLayout.this.t("editor.addons.loading.title")[0])
            .withLore(EditorAddonsLayout.this.t("editor.addons.loading.description"));
      }
   }

   private enum CategoryType {
      ALL("all"),
      ANIMATIONS("animations"),
      FEATURES("features"),
      INSTALLED("installed"),
      NOT_INSTALLED("not-installed");

      private final String key;

      CategoryType(final String key) {
         this.key = key;
      }

      public Translatable getTitle() {
         return Translatable.key("editor.addons.category." + this.key);
      }
   }

   private enum FilterType {
      ALL("all"),
      FREE("free"),
      PREMIUM("premium");

      private final String key;

      FilterType(final String key) {
         this.key = key;
      }

      public Translatable getTitle() {
         return Translatable.key("editor.addons.filter." + this.key);
      }
   }
}
