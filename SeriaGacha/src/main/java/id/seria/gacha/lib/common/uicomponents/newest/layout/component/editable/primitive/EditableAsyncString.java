package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectValueModifier;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.entity.Player;

public class EditableAsyncString<T> extends EditableObject<T> {
   public EditableAsyncString(SlotCompound slot, ComponentData data, T object) {
      this(slot, data, data.getTitle(), object);
   }

   public EditableAsyncString(SlotCompound slot, ComponentData data, Translatable displayType, T object) {
      super(slot, data, displayType, object);
   }

   public EditableAsyncString<T> setResolver(ClickAction action, Function<String, CompletableFuture<T>> resolver) {
      return this.setResolver(action, resolver, null);
   }

   public EditableAsyncString<T> setResolver(
      ClickAction action, Function<String, CompletableFuture<T>> resolver, BiFunction<String, Throwable, String[]> errorMessageProvider
   ) {
      this.setClickAction(
         action,
         context -> {
            Player player = context.getViewer();
            player.sendMessage(this._t("editor.write-in-chat"));
            player.closeInventory();
            TextReader.askForInput(
               player,
               input -> {
                  if (input == null) {
                     context.getContainerView().updateView();
                  } else if (input.replace(" ", "").isEmpty()) {
                     player.sendMessage(this._t("editor.invalid-input"));
                     context.getContainerView().updateView();
                  } else {
                     Utilities.getAsyncService()
                        .execute(
                           () -> {
                              Utilities.getNMSFactory()
                                 .sendTitlePacket(Collections.singletonList(player), "\u00a7a", this._t("editor.requesting-data")[0], 0, Integer.MAX_VALUE, 0);

                              try {
                                 T resolved = resolver.apply(input).get(5L, TimeUnit.SECONDS);
                                 this.getPlugin().getScheduler().runSync(() -> {
                                    this.safeSetValue(context.getContainerView(), resolved, false);
                                    player.sendMessage(this._t("editor.field-updated", "%field%", input));
                                    context.getContainerView().updateView();
                                 });
                              } catch (Exception e) {
                                 String[] message = errorMessageProvider != null
                                    ? errorMessageProvider.apply(input, e.getCause() != null ? e.getCause() : e)
                                    : this._t("error-processing", "%message%", e.getMessage());
                                 player.sendMessage(message);
                                 if (errorMessageProvider == null) {
                                    e.printStackTrace();
                                 }

                                 this.getPlugin().getScheduler().runSync(() -> context.getContainerView().updateView());
                              } finally {
                                 Utilities.getNMSFactory().sendTitlePacket(Collections.singletonList(player), "\u00a7a", "\u00a7a", 0, 10, 0);
                              }
                           }
                        );
                  }
               }
            );
         }
      );
      return this;
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(this._t("layout.displays.value", "%type%", this.displayType, "%value%", this.formatter.apply(this.getValue().get())));
   }

   public EditableAsyncString<T> addModifier(ObjectValueModifier<T> processor) {
      this.modifiers.add(processor);
      return this;
   }
}
