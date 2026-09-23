package id.seria.gacha.lib.common.uicomponents.newest.actions.builder;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class ActionBuilderLayout extends ContainerLayout {
   private final ActionBuilderRegistry registry;
   private final Consumer<Action> consumer;

   public ActionBuilderLayout(ContainerLayout parent, ActionBuilderRegistry registry, Consumer<Action> consumer) {
      super(parent.getPlugin(), parent.getTranslationProvider(), Translatable.key("layout.actions.builder.title"), parent);
      this.registry = registry;
      this.consumer = consumer;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new SimplePaginatedList<>(
               SlotCompound.fromGrid(10, 3, 7),
               new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
               ignored -> this.registry.getBuildableTypes()
            )
            .setOnItemRender(
               (context, index, display, type) -> {
                  String name = StringUtil.humanize(type.getName());
                  display.withItem(type.getIcon() == null ? XMaterial.STONE_BUTTON : type.getIcon())
                     .withFlag(ItemFlag.values())
                     .withDisplayName(this.t("layout.actions.builder.type.title", "%type%", name)[0])
                     .withLore(
                        ClickActionsFacade.applyActions(
                           this,
                           Arrays.asList(this.t("layout.actions.builder.type.description", "%type%", name.toLowerCase())),
                           ClickAction.SELECT(ClickAction.ClickType.LEFT, Translatable.key("layout.actions.builder.action-label"))
                        )
                     )
                     .whenClick(click -> this.select(click.getViewer(), type));
               }
            )
            .setFallbackItem(ignored -> VirtualItem.from(XMaterial.GRAY_STAINED_GLASS_PANE).withDisplayName("\u00a77"))
      );
   }

   private void select(Player player, ActionType type) {
      if (this.registry.isInstant(type.getName())) {
         this.submit(player, Collections.singletonList(type.createAction(new String[0])));
      } else {
         ActionBuilderFactory factory = this.registry.getBuilderOrNull(type.getName());
         if (factory != null) {
            factory.create(this, type).open(player);
         }
      }
   }

   public void submit(Player player, List<Action> actions) {
      for (Action action : actions) {
         this.consumer.accept(action);
      }

      this.getParent().open(player);
   }

   public static List<String> formatActions(ContainerLayout layout, List<String> serializedActions) {
      List<String> display = new ArrayList<>();

      for (String action : serializedActions) {
         int typeEnd = action.indexOf(93);
         if (typeEnd == -1) {
            display.add("\u00a7f" + action);
         } else {
            display.add("\u00a7a" + action.substring(0, typeEnd + 1) + "\u00a7f" + action.substring(typeEnd + 1));
         }
      }

      if (display.isEmpty()) {
         display.add(layout.getTranslationProvider().translate(Translatable.key("labels.empty"))[0]);
      }

      return display;
   }

   @Generated
   public ActionBuilderRegistry getRegistry() {
      return this.registry;
   }

   @Generated
   public Consumer<Action> getConsumer() {
      return this.consumer;
   }
}
