package id.seria.gacha.lib.common.uicomponents.newest.layout;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.DisplayableComponent;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.Generated;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public abstract class ContainerLayout extends AbstractContainer {
   public static String DISPLAYABLE_COMPONENTS_LIST_KEY = "components";
   public static String DISPLAYABLE_COMPONENTS_COUNTER_KEY = "components-counter";
   public static AtomicInteger DEFAULT_INCREMENT = new AtomicInteger();
   protected String id = String.valueOf(DEFAULT_INCREMENT.getAndIncrement());
   protected PluginTranslationsProvider translationProvider;
   protected Translatable title;
   private final ContainerLayout parent;
   @Deprecated
   private boolean viewPersistentOnGoBack;

   public ContainerLayout(ServerPlugin plugin, PluginTranslationsProvider translationProvider, Translatable title, ContainerLayout parent) {
      super(plugin);
      this.translationProvider = translationProvider;
      this.title = title;
      this.parent = parent;
   }

   @Override
   public final void onInitialize(ContainerView view) {
      view.setMetadata(DISPLAYABLE_COMPONENTS_LIST_KEY, Collections.synchronizedMap(new LinkedHashMap()), true);
      view.setMetadata(DISPLAYABLE_COMPONENTS_COUNTER_KEY, 1, true);
      this.onPrepare(view);

      for (DisplayableComponent component : this.getComponentsSnapshot(view)) {
         component.onInitialize(view);
         component.onPrepare(view);
      }
   }

   public abstract void onPrepare(ContainerView var1);

   @Override
   public void onOpen(OpenViewContext context) {
      context.setContainerTitle(this.t(this.getTitle())[0]);
      context.setContainerType(ContainerType.fromRows(this.getNeededRows(context.getContainerView())));
   }

   protected int getNeededRows(ContainerView view) {
      int maxRows = 1;

      for (DisplayableComponent component : this.getComponentsSnapshot(view)) {
         if (!component.isFixedPosition()) {
            int y = InventoryUtil.getXYPosition(component.getSlot().getFirst())[1];
            int height = component.getSlot().getSize().height;
            if (height != 0) {
               y += height - 1;
               maxRows = Math.max(maxRows, Math.min(y, 6));
            }
         }
      }

      int rows = maxRows + 1;
      if (this.parent != null) {
         rows++;
      }

      return MathUtil.clamp(rows, 1, 6);
   }

   @Override
   public void onRender(RenderViewContext context) {
      List<DisplayableComponent> componentsSnapshot = this.getComponentsSnapshot(context.getContainerView());
      if (!componentsSnapshot.isEmpty()) {
         if (this.parent != null) {
            for (int x = 1; x <= 9; x++) {
               context.setSlot(x, context.getContainerType().getRows(), ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72"));
               context.setSlot(
                  5, context.getContainerType().getRows(), ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + this.t("labels.go-back")[0]), __ -> {
                     if (this.viewPersistentOnGoBack) {
                        this.parent.open(context.getContainerView());
                     } else {
                        this.parent.open(context.getViewer());
                     }
                  }
               );
            }
         }

         for (DisplayableComponent component : componentsSnapshot) {
            if (!component.isVisible()) {
               component.onPreRender(context);
               if (!component.isVisible()) {
                  component.onRender(context);
               }
            }
         }
      }
   }

   @Override
   public void onClick(ClickViewContext context) {
      Consumer<ClickViewContext> consumer = context.getContainerView().getConsumers()[context.getRawSlot()];
      if (consumer == null) {
         List<DisplayableComponent> copy = Lists.newArrayList(this.getComponentsSnapshot(context.getContainerView()));
         Collections.reverse(copy);

         for (DisplayableComponent component : copy) {
            if (component instanceof ClickableComponent) {
               ClickableComponent cast = (ClickableComponent)component;
               if (cast.isClicking(context)) {
                  cast.onClick(context);
                  this.getPlugin().getScheduler().runSync(() -> context.getViewer().updateInventory(), 1L);
                  break;
               }
            }
         }
      }
   }

   @Override
   public void updateView(ContainerView view) {
      this.clearNonPersistentMetadata(view);
      if (this.getNeededRows(view) != view.getContainerType().getRows()) {
         this.open(view);
      } else {
         super.updateView(view);
      }
   }

   protected int getSlot(int x, int y) {
      return InventoryUtil.getPositionRaw(x, y);
   }

   @Override
   public void open(Player viewer) {
      this.open(new ContainerLayout.CustomLayoutView(this, viewer));
   }

   @Deprecated
   public ClickableComponent<?> createComponent(ComponentData data) {
      return this.createComponent(new SlotCompound(data.getSlot()), data, new ArrayList<>(), click -> {});
   }

   public ClickableComponent<?> createComponent(SlotCompound slot, ComponentData data) {
      return this.createComponent(slot, data, new ArrayList<>(), click -> {});
   }

   public ClickableComponent<?> createComponent(int slot, ComponentData data, List<ClickAction> actions, Consumer<ClickViewContext> clickConsumer) {
      return this.createComponent(slot, data, actions, clickConsumer);
   }

   @Deprecated
   public ClickableComponent<?> createComponent(ComponentData data, List<ClickAction> actions, Consumer<ClickViewContext> clickConsumer) {
      return this.createComponent(new SlotCompound(data.getSlot()), data, actions, clickConsumer);
   }

   public <T extends ClickableComponent<T>> ClickableComponent<?> createComponent(
      SlotCompound slot, ComponentData data, final List<ClickAction> actions, final Consumer<ClickViewContext> clickConsumer
   ) {
      Validate.notNull(actions);
      Validate.notNull(clickConsumer);
      return new ClickableComponent<T>(slot, data) {
         {
            for (ClickAction action : actions) {
               this.clickActions.put(action, (__, context) -> clickConsumer.accept(context));
            }
         }

         @Override
         public List<String> getRenderDisplay(RenderViewContext context) {
            return new ArrayList<>();
         }

         @Override
         public List<ClickAction> getDisplayActions() {
            return actions;
         }
      };
   }

   protected String[] t(String key, Object... arguments) {
      return this.translationProvider.translate(Translatable.key(key, arguments));
   }

   protected String[] t(Translatable key) {
      return this.translationProvider.translate(key);
   }

   protected String t(boolean state) {
      String key = state ? "labels.yes-word" : "labels.no-word";
      return this.translationProvider.translate(Translatable.key(key))[0];
   }

   public void addComponent(ContainerView view, DisplayableComponent component) {
      int counter = view.getMetadata(DISPLAYABLE_COMPONENTS_COUNTER_KEY, 1);
      String id = this.id + "." + counter++;
      view.setMetadata(DISPLAYABLE_COMPONENTS_COUNTER_KEY, counter, true);
      component.linkView(view, id);
      this.getComponents(view).put(id, component);
   }

   public void addComponent(ContainerView view, String key, DisplayableComponent component) {
      component.linkView(view, key);
      this.getComponents(view).put(key, component);
   }

   public Map<String, DisplayableComponent> getComponents(ContainerView view) {
      Map rawMap = view.getMetadataOrNull(DISPLAYABLE_COMPONENTS_LIST_KEY, Map.class);
      if (rawMap == null) {
         rawMap = Collections.synchronizedMap(new LinkedHashMap());
         view.setMetadata(DISPLAYABLE_COMPONENTS_LIST_KEY, rawMap, true);
      }

      return rawMap;
   }

   private List<DisplayableComponent> getComponentsSnapshot(ContainerView view) {
      Map<String, DisplayableComponent> components = this.getComponents(view);
      synchronized (components) {
         return new ArrayList<>(components.values());
      }
   }

   public DisplayableComponent getComponentOrNull(ContainerView view, String key) {
      return this.getComponents(view).get(key);
   }

   public ContainerLayout.ContainerViewWrapper withData(String key, Object value) {
      return new ContainerLayout.ContainerViewWrapper(this, Collections.singletonMap(key, value));
   }

   public ContainerLayout.ContainerViewWrapper withData(String key1, Object value1, String key2, Object value2) {
      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put(key1, value1);
      metadata.put(key2, value2);
      return new ContainerLayout.ContainerViewWrapper(this, metadata);
   }

   public ContainerLayout.ContainerViewWrapper withData(Map<String, Object> metadata) {
      Validate.notNull(metadata, "metadata cannot be null");
      return new ContainerLayout.ContainerViewWrapper(this, metadata);
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public PluginTranslationsProvider getTranslationProvider() {
      return this.translationProvider;
   }

   @Generated
   public Translatable getTitle() {
      return this.title;
   }

   @Generated
   public ContainerLayout getParent() {
      return this.parent;
   }

   @Deprecated
   @Generated
   public boolean isViewPersistentOnGoBack() {
      return this.viewPersistentOnGoBack;
   }

   @Deprecated
   @Generated
   public void setViewPersistentOnGoBack(boolean viewPersistentOnGoBack) {
      this.viewPersistentOnGoBack = viewPersistentOnGoBack;
   }

   public static class ContainerViewWrapper {
      private final ContainerLayout layout;
      private final Map<String, Object> metadata;

      public ContainerViewWrapper(ContainerLayout layout, Map<String, Object> metadata) {
         this.layout = layout;
         this.metadata = new LinkedHashMap<>(metadata);
      }

      public ContainerView open(Player player) {
         ContainerLayout.CustomLayoutView view = new ContainerLayout.CustomLayoutView(this.layout, player);

         for (Entry<String, Object> entry : this.metadata.entrySet()) {
            view.setMetadata(entry.getKey(), entry.getValue(), true);
         }

         this.layout.open(view);
         return view;
      }
   }

   public static class CustomLayoutView extends ContainerView {
      public CustomLayoutView(ContainerLayout container, Player viewer) {
         super(container, viewer);
      }

      public ContainerLayout getContainer() {
         return (ContainerLayout)super.getContainer();
      }
   }
}
