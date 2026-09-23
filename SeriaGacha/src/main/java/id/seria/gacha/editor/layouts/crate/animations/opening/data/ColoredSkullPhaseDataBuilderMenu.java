package id.seria.gacha.editor.layouts.crate.animations.opening.data;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.mojang.MojangTexture;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredSkullOpeningPhaseData;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@Deprecated
public class ColoredSkullPhaseDataBuilderMenu extends CustomInventory {
   private final CrateType crateType;
   private final ColoredSkullOpeningPhaseData initialData;
   private final Consumer<Player> onCancel;
   private final Consumer<ColoredSkullOpeningPhaseData> onCreate;
   private boolean ignoreNextInventoryClose;

   public ColoredSkullPhaseDataBuilderMenu(
      PhoenixCrates plugin,
      CrateType crateType,
      ColoredSkullOpeningPhaseData initialData,
      Consumer<Player> onCancel,
      Consumer<ColoredSkullOpeningPhaseData> onCreate
   ) {
      super(plugin, Translations.t("editor.titles.crate.opening-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 4);
      this.crateType = crateType;
      this.initialData = initialData;
      this.onCancel = onCancel;
      this.onCreate = onCreate;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      ColoredSkullPhaseDataBuilderMenu.EditorColoredSkullOpeningPhaseHolder holder = (ColoredSkullPhaseDataBuilderMenu.EditorColoredSkullOpeningPhaseHolder)inventory.getHolder();

      for (int i = 1; i <= 4; i++) {
         this.setItem(i, 4, ItemBuilder.of(XMaterial.RED_STAINED_GLASS_PANE).setDisplayName("\u00a7cCancel"), __ -> {
            player.closeInventory();
            this.onCancel.accept(player);
         });
      }

      this.setItem(5, 4, ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a7r"));

      for (int i = 6; i <= 9; i++) {
         this.setItem(i, 4, ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE).setDisplayName("\u00a7aCreate"), __ -> {
            player.closeInventory();
            this.onCreate.accept(new ColoredSkullOpeningPhaseData(holder.getColor(), holder.getTextureSkull()));
         });
      }

      this.setItem(
         4,
         2,
         ItemBuilder.of(XMaterial.WHITE_DYE.or(XMaterial.BONE_MEAL))
            .setDisplayName("\u00a7aColor")
            .setLore(
               "\u00a77By clicking here you will be able",
               "\u00a77to change the color of this phase",
               "",
               "\u00a7fColor: "
                  + this.applyHexColor(holder.color)
                  + ColorUtil.getColorDisplayName(holder.color)
                  + " \u00a77("
                  + ColorUtil.getHexColor(holder.color)
                  + ")",
               "",
               "\u00a76Left-Click: \u00a7eEdit color."
            ),
         __ -> {
            List<SelectOptionMenu.OptionItem<Color>> options = Lists.newArrayList();

            for (Color color : ColorUtil.getColors()) {
               options.add(
                  new SelectOptionMenu.OptionItem<>(
                     color,
                     XMaterial.BOOK,
                     Translations.t("editor.edit-standby-effect.color-select.title", "%name%", this.applyHexColor(color) + ColorUtil.getColorDisplayName(color))[0],
                     Translations.t("editor.edit-standby-effect.color-select.description")
                  )
               );
            }

            SelectOptionMenu<Color> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, object -> {
               holder.setColor(object);
               this.open(player, holder);
            });
            optionSelector.setBackConsumer(event -> this.open(player, holder));
            this.ignoreNextInventoryClose = true;
            optionSelector.open(player);
         }
      );
      String displayTextureUrl = holder.textureSkull;
      if (displayTextureUrl.length() > 60) {
         displayTextureUrl = displayTextureUrl.substring(0, 57) + "...";
      }

      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.PAPER)
            .setDisplayName("\u00a7aTexture Url")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to change the texture of this npc",
                        "&7from a &ftextures.minecraft.net &7url.",
                        "",
                        "&fCurrent Texture:",
                        "&7%texture_url%",
                        "",
                        "&a&lTIP: &7You can use &fminecraft-heads.com",
                        "       &7to find your favorite texture url.",
                        "",
                        "&6Left-Click: &eEdit with texture url.",
                        "&6Right-Click: &eEdit with player username."
                     }
                  ),
                  "%texture_url%",
                  displayTextureUrl
               )
            ),
         event -> {
            this.ignoreNextInventoryClose = true;
            Translations.send(player, Translations.t("editor.messages.write-in-chat"));
            Translations.send(player, "\u00a7aIf you are using texture url id make sure to put only the id.");
            Translations.send(player, "\u00a7aExample: \u00a7e28dc3c84491c4ff103de6ea01880e2d83432cd4878b84d6b43253cc51fd978e7");
            player.closeInventory();
            if (event.isLeftClick()) {
               TextReader.askForInput(player, input -> {
                  if (input != null) {
                     if (input.replace(" ", "").isEmpty()) {
                        Translations.send(player, Translations.t("editor.errors.invalid-input"));
                     } else {
                        String[] split = input.split("/");
                        holder.textureSkull = split[split.length - 1];
                     }
                  }

                  this.open(player, holder);
               });
            } else if (event.isRightClick()) {
               TextReader.askForInput(player, input -> {
                  if (input != null) {
                     if (input.replace(" ", "").isEmpty()) {
                        Translations.send(player, Translations.t("editor.errors.invalid-input"));
                     } else {
                        Executors.newCachedThreadPool().execute(() -> {
                           Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(player), "\u00a7a", "\u00a7aRequesting Texture...", 0, Integer.MAX_VALUE, 0);

                           try {
                              Optional<MojangTexture> optional = MojangTexture.fetchTextureFromUsername(input).get(5L, TimeUnit.SECONDS);
                              if (!optional.isPresent()) {
                                 throw new IllegalStateException(Translations.t("editor.username-not-found", "%username%", input)[0]);
                              }

                              MojangTexture texture = optional.get();
                              String textureUrl = texture.extractTextureUrl();
                              String[] split = textureUrl.split("/");
                              holder.textureSkull = split[split.length - 1];
                           } catch (IllegalStateException e) {
                              Translations.send(player, e.getMessage());
                           } catch (Exception e) {
                              Translations.error(player, e);
                           }

                           Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(player), "\u00a7a", "\u00a7a", 0, 10, 0);
                        });
                     }
                  }

                  this.open(player, holder);
               });
            }
         }
      );
   }

   private String applyHexColor(Color color) {
      return Utilities.getNMSFactory().translateColorCodes("&" + ColorUtil.getHexColor(color));
   }

   @Override
   public void open(Player player) {
      this.open(player, new ColoredSkullPhaseDataBuilderMenu.EditorColoredSkullOpeningPhaseHolder(this, this.initialData));
   }

   @EventHandler
   @Override
   public void onMenuClose(InventoryCloseEvent event) {
      if (this.ignoreNextInventoryClose) {
         this.ignoreNextInventoryClose = false;
      } else {
         Player player = (Player)event.getPlayer();
         this.onCancel.accept(player);
      }
   }

   private boolean isMalformedUrl(String input) {
      try {
         new URL(input);
         return false;
      } catch (MalformedURLException e) {
         return true;
      }
   }

   public static class EditorColoredSkullOpeningPhaseHolder extends CustomInventory.PaginationWrapper {
      private Color color = Color.WHITE;
      private String textureSkull = "f98bc63f05f6378bf29ef10e3d82acb3ceb73a720bf80f30bc576d0ad8c40cfb";

      public EditorColoredSkullOpeningPhaseHolder(CustomInventory sustainer, ColoredSkullOpeningPhaseData data) {
         super(sustainer);
         if (data != null) {
            this.color = data.getColor();
            this.textureSkull = data.getTextureSkull();
         }
      }

      @Generated
      public Color getColor() {
         return this.color;
      }

      @Generated
      public String getTextureSkull() {
         return this.textureSkull;
      }

      @Generated
      public void setColor(Color color) {
         this.color = color;
      }

      @Generated
      public void setTextureSkull(String textureSkull) {
         this.textureSkull = textureSkull;
      }
   }
}
