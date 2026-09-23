package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.PlaceholderUtil;
import id.seria.gacha.lib.common.utils.utility.Validate;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;

public abstract class DisplayableComponent {
   public static AtomicInteger DEFAULT_INCREMENT = new AtomicInteger();
   protected String id = String.valueOf(DEFAULT_INCREMENT.getAndIncrement());
   private ContainerView view;
   private SlotCompound slot;
   private ComponentData data;
   private boolean fixedPosition;
   private boolean visible;

   protected DisplayableComponent(SlotCompound slot, ComponentData data) {
      this.slot = slot;
      this.data = data;
   }

   public void linkView(ContainerView view, String id) {
      Validate.isTrue(this.view == null || this.view.equals(view), "View already linked");
      Validate.isTrue(
         view.getContainer() instanceof ContainerLayout,
         "The view is not suitable for dealing with displayable components. (Expected container ContainerLayout)"
      );
      this.view = view;
      this.id = id;
   }

   public ContainerView getContainerView() {
      return Objects.requireNonNull(this.view, "Container View is not linked");
   }

   public ContainerLayout getContainerLayout() {
      Validate.isTrue(
         this.getContainerView().getContainer() instanceof ContainerLayout,
         "The container view is not suitable for dealing with displayable components. (Expected ContainerLayout)"
      );
      return (ContainerLayout)this.view.getContainer();
   }

   public ServerPlugin getPlugin() {
      return this.view.getPlugin();
   }

   public abstract void onInitialize(ContainerView var1);

   public abstract void onPrepare(ContainerView var1);

   public void onPreRender(RenderViewContext context) {
   }

   public abstract void onRender(RenderViewContext var1);

   public abstract List<ClickAction> getDisplayActions();

   @Deprecated
   protected String _r(String message, Object... values) {
      return PlaceholderUtil.replacePlaceholdersAndColors(this.getPlugin(), message, values);
   }

   @Deprecated
   protected List<String> _r(List<String> message, Object... values) {
      return PlaceholderUtil.replacePlaceholdersAndColors(this.getPlugin(), message, values);
   }

   @Deprecated
   protected String[] _t(String key, Object... arguments) {
      return this.getContainerLayout().getTranslationProvider().translate(Translatable.key(key, arguments));
   }

   @Deprecated
   protected String[] _t(Translatable key, Object... arguments) {
      return this.getContainerLayout().getTranslationProvider().translate(key.arguments(arguments));
   }

   @Deprecated
   protected String[] _t(Translatable key) {
      return this.getContainerLayout().getTranslationProvider().translate(key);
   }

   @Deprecated
   protected String _t(boolean state) {
      String key = state ? "labels.yes-word" : "labels.no-word";
      return this.getContainerLayout().getTranslationProvider().translate(Translatable.key(key))[0];
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public SlotCompound getSlot() {
      return this.slot;
   }

   @Generated
   public ComponentData getData() {
      return this.data;
   }

   @Generated
   public boolean isFixedPosition() {
      return this.fixedPosition;
   }

   @Generated
   public boolean isVisible() {
      return this.visible;
   }

   @Generated
   public DisplayableComponent setSlot(SlotCompound slot) {
      this.slot = slot;
      return this;
   }

   @Generated
   public DisplayableComponent setData(ComponentData data) {
      this.data = data;
      return this;
   }

   @Generated
   public DisplayableComponent setFixedPosition(boolean fixedPosition) {
      this.fixedPosition = fixedPosition;
      return this;
   }

   @Generated
   public DisplayableComponent setVisible(boolean visible) {
      this.visible = visible;
      return this;
   }
}
