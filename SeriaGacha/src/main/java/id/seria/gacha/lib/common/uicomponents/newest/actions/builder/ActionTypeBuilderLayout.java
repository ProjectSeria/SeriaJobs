package id.seria.gacha.lib.common.uicomponents.newest.actions.builder;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public abstract class ActionTypeBuilderLayout<H> extends ContainerLayout {
   public static final Translatable ACTION_LABEL = Translatable.key("layout.actions.builder.action-label");
   protected final ActionBuilderLayout builder;
   protected final ActionType type;
   private H holder;

   public ActionTypeBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(
         builder.getPlugin(),
         builder.getTranslationProvider(),
         Translatable.key("layout.actions.builder.type.menu-title", "%type%", StringUtil.humanize(type.getName())),
         builder
      );
      this.builder = builder;
      this.type = type;
   }

   @Override
   public final void onPrepare(ContainerView view) {
      H holder = this.holder();
      this.onPrepareBuilder(view, holder);
      boolean allowCreation = this.canCreate(holder);
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(7, this.getNeededRows(view)),
               this.createData(
                  allowCreation ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE,
                  Translatable.key("layout.actions.builder.create.title"),
                  Translatable.key("layout.actions.builder.create.description")
               )
            )
            .setClickAction(ClickAction.CREATE(ClickAction.ClickType.LEFT, ACTION_LABEL), context -> this.create(context.getViewer(), holder))
            .addError(!allowCreation, Translatable.key("layout.actions.builder.incomplete"))
            .setFixedPosition(true)
      );
   }

   public abstract void onPrepareBuilder(ContainerView var1, H var2);

   public abstract H createHolder();

   public abstract boolean canCreate(H var1);

   protected abstract String[] arguments(H var1);

   protected List<Action> createActions(H holder) {
      return Collections.singletonList(this.type.createAction(this.arguments(holder)));
   }

   protected H holder() {
      if (this.holder == null) {
         this.holder = this.createHolder();
      }

      return this.holder;
   }

   protected ComponentData createData(ItemStack item, Translatable title, Translatable description) {
      return new ComponentData(0, ItemBuilder.of(item).addItemFlags(ItemFlag.values()).build(), title, description);
   }

   protected ComponentData createData(XMaterial material, Translatable title, Translatable description) {
      return this.createData(material.or(XMaterial.BARRIER).parseItem(), title, description);
   }

   protected Translatable typeName() {
      return Translatable.literal(StringUtil.humanize(this.type.getName()).toLowerCase());
   }

   private void create(Player player, H holder) {
      if (this.canCreate(holder)) {
         this.builder.submit(player, this.createActions(holder));
      }
   }

   @Generated
   public ActionBuilderLayout getBuilder() {
      return this.builder;
   }

   @Generated
   public ActionType getType() {
      return this.type;
   }
}
