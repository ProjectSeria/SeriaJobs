package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.PaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditablePopupList<T> extends EditableObject<T> {
   private static final String FILTER_KEY = "components.%s.filter";
   protected final List<T> options;
   private boolean filterable = true;
   private Function<T, EditablePopupList.ListItem<T>> parser = value -> new EditablePopupList.ListItem<>(
      value, XMaterial.PAPER, value.toString(), Lists.newArrayList()
   );

   public EditablePopupList<T> setFilterable(boolean filterable) {
      this.filterable = filterable;
      return this;
   }

   @Deprecated
   public EditablePopupList(ComponentData data, @NonNull Translatable displayType, List<T> options, Supplier<T> currentValue) {
      this(new SlotCompound(data.getSlot()), data, displayType, options, currentValue.get());
      if (displayType == null) {
         throw new NullPointerException("displayType is marked non-null but is null");
      }
   }

   public EditablePopupList(SlotCompound slot, ComponentData data, @NonNull Translatable displayType, List<T> options, T currentValue) {
      super(slot, data, displayType, currentValue);
      if (displayType == null) {
         throw new NullPointerException("displayType is marked non-null but is null");
      }

      this.options = options;
      this.options.removeIf(Objects::isNull);
      this.setClickAction(ClickAction.CHOOSE(ClickAction.ClickType.LEFT, displayType), context -> {
         Player viewer = context.getViewer();
         List<EditablePopupList.ListItem<T>> items = Lists.newArrayList();

         for (T value : this.options) {
            EditablePopupList.ListItem<T> item = this.parser.apply(value);
            if (item != null) {
               items.add(item);
            }
         }

         EditablePopupList<T>.PopupSelectionMenu selection = new EditablePopupList.PopupSelectionMenu(items, valuex -> {
            try {
               this.setValue((T)valuex);
               this.getContainerLayout().open(viewer);
               return true;
            } catch (Exception e) {
               viewer.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
               return false;
            }
         });
         selection.open(viewer);
      });
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(this._t("layout.displays.value", "%type%", this.displayType, "%value%", this.formatter.apply(this.getValue().get())));
   }

   public static String highlightFilter(String text, String filter, ChatColor reset) {
      if (filter == null) {
         return text;
      }

      int filterLen = filter.length();
      StringBuilder highlighted = new StringBuilder(text.length() + filterLen * 2);

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (i <= text.length() - filterLen && text.substring(i, i + filterLen).equalsIgnoreCase(filter)) {
            highlighted.append("\u00a7e");
            highlighted.append(text.substring(i, i + filterLen));
            highlighted.append(reset);
            i += filterLen - 1;
         } else {
            highlighted.append(c);
         }
      }

      return highlighted.toString();
   }

   @Generated
   public List<T> getOptions() {
      return this.options;
   }

   @Generated
   public boolean isFilterable() {
      return this.filterable;
   }

   @Generated
   public Function<T, EditablePopupList.ListItem<T>> getParser() {
      return this.parser;
   }

   @Generated
   public EditablePopupList<T> setParser(Function<T, EditablePopupList.ListItem<T>> parser) {
      this.parser = parser;
      return this;
   }

   public static class ListItem<T> {
      private final T object;
      private final ItemStack display;
      private final String title;
      private final List<String> description;

      public ListItem(T object, XMaterial material, String title, List<String> description) {
         this.object = object;
         this.display = material.parseItem();
         this.title = title;
         this.description = description;
      }

      public ListItem(T object, ItemStack display, String title, List<String> description) {
         this.object = object;
         this.display = display;
         this.title = title;
         this.description = description;
      }

      @Generated
      public T getObject() {
         return this.object;
      }

      @Generated
      public ItemStack getDisplay() {
         return this.display;
      }

      @Generated
      public String getTitle() {
         return this.title;
      }

      @Generated
      public List<String> getDescription() {
         return this.description;
      }
   }

   private class PopupSelectionMenu extends ContainerLayout {
      private final List<EditablePopupList.ListItem<T>> preservedItems;
      private final Function<T, Boolean> whenSelect;
      private boolean selected;
      private boolean ignoreNextInventoryClose;

      public PopupSelectionMenu(List<EditablePopupList.ListItem<T>> preservedItems, Function<T, Boolean> whenSelect) {
         super(
            EditablePopupList.this.getPlugin(),
            EditablePopupList.this.getContainerLayout().getTranslationProvider(),
            Translatable.key("layout.actions.description.select", "%value%", EditablePopupList.this.displayType),
            EditablePopupList.this.getContainerLayout()
         );
         this.preservedItems = new ArrayList<>(preservedItems);
         this.whenSelect = whenSelect;
      }

      @Override
      public void onPrepare(ContainerView view) {
         view.setContainerType(ContainerType.CONTAINER_9X6);
         this.addComponent(
            view,
            (new PaginatedList<EditablePopupList.ListItem<T>>(
                  SlotCompound.fromGrid(10, 3, 7), new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3))
               ) {
                  private int cachedMaxItems;

                  @Override
                  public List<EditablePopupList.ListItem<T>> onPrepareItems(RenderViewContext context, int startIndex, int endIndex) {
                     String filter = PopupSelectionMenu.this.getFilter(this.getContainerView());
                     List<EditablePopupList.ListItem<T>> list = new ArrayList<>(PopupSelectionMenu.this.preservedItems);
                     if (filter != null) {
                        list.removeIf(material -> !material.getTitle().toLowerCase().contains(filter.toLowerCase()));
                     }

                     this.cachedMaxItems = list.size();
                     if (list.isEmpty()) {
                        return new ArrayList<>();
                     }

                     int min = Math.min(startIndex, endIndex);
                     int max = Math.max(startIndex, endIndex);
                     return list.subList(Math.min(Math.max(0, min), list.size()), Math.min(list.size(), max));
                  }

                  @Override
                  public int countMaxItems() {
                     return this.cachedMaxItems;
                  }

                  @Override
                  public void onRender(RenderViewContext context) {
                     if (this.getPreparedItems().isEmpty()) {
                        context.setSlot(
                           5,
                           3,
                           ItemBuilder.of(XMaterial.BARRIER)
                              .setDisplayName(this._t("layout.extras.no-data-found.title")[0])
                              .setLore(this._t("layout.extras.no-data-found.description"))
                        );
                     } else {
                        super.onRender(context);
                     }

                     for (int x = 1; x <= 9; x++) {
                        context.setSlot(x, context.getContainerType().getRows(), ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72"));
                     }

                     if (EditablePopupList.this.getContainerLayout() != null) {
                        context.setSlot(
                           5,
                           context.getContainerType().getRows(),
                           ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + this._t("labels.go-back")[0]),
                           __ -> EditablePopupList.this.getContainerLayout().open(context.getViewer())
                        );
                     }

                     if (EditablePopupList.this.filterable) {
                        String filter = PopupSelectionMenu.this.getFilter(context.getContainerView());
                        context.setSlot(
                           7,
                           context.getContainerType().getRows(),
                           ItemBuilder.of(XMaterial.HOPPER)
                              .setDisplayName(this._t("layout.extras.filter.title")[0])
                              .setLore(this._t("layout.extras.filter.description", "%filter%", filter == null ? "None" : "\"" + filter + "\"")),
                           click -> {
                              if (click.isLeftClick()) {
                                 PopupSelectionMenu.this.ignoreNextInventoryClose = true;
                                 Player player = context.getViewer();
                                 player.sendMessage(String.format("\u00a7eWrite the new %s in the chat.", "filter"));
                                 player.closeInventory();
                                 TextReader.askForInput(player, input -> {
                                    if (input != null) {
                                       try {
                                          PopupSelectionMenu.this.setFilter(context.getContainerView(), ChatColor.stripColor(input));
                                          player.sendMessage(String.format("\u00a7a%s successfully updated to \"%s\u00a7a\"!", "Filter", input));
                                       } catch (Exception e) {
                                          player.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                                          return;
                                       }
                                    }

                                    PopupSelectionMenu.this.ignoreNextInventoryClose = true;
                                    PopupSelectionMenu.this.open(context.getContainerView());
                                    PopupSelectionMenu.this.ignoreNextInventoryClose = false;
                                 });
                              } else if (click.isRightClick()) {
                                 PopupSelectionMenu.this.setFilter(context.getContainerView(), null);
                                 PopupSelectionMenu.this.ignoreNextInventoryClose = true;
                                 PopupSelectionMenu.this.updateView(context.getContainerView());
                                 PopupSelectionMenu.this.ignoreNextInventoryClose = false;
                              }
                           }
                        );
                     }
                  }
               })
               .setOnItemRender(
                  (context, index, display, item) -> {
                     String filter = this.getFilter(view);
                     display.withItem(item.getDisplay())
                        .withDisplayName("\u00a7a" + EditablePopupList.highlightFilter(item.getTitle(), filter, ChatColor.GREEN))
                        .withLore(
                           ClickActionsFacade.applyActions(
                              EditablePopupList.this.getContainerLayout(),
                              item.getDescription(),
                              ClickAction.SELECT(ClickAction.ClickType.LEFT, EditablePopupList.this.displayType)
                           )
                        )
                        .whenClick(clickContext -> {
                           if (!this.selected) {
                              if (this.whenSelect.apply((T)item.getObject())) {
                                 this.selected = true;
                              }
                           }
                        });
                  }
               )
               .setFallbackItem(ctx -> VirtualItem.from(XMaterial.GRAY_STAINED_GLASS_PANE).withDisplayName("\u00a77"))
         );
      }

      public String getFilter(ContainerView view) {
         return view.getMetadataOrNull(String.format("components.%s.filter", EditablePopupList.this.id), String.class);
      }

      public void setFilter(ContainerView view, String filter) {
         view.setMetadata(String.format("components.%s.filter", EditablePopupList.this.id), filter, true);
      }
   }
}
