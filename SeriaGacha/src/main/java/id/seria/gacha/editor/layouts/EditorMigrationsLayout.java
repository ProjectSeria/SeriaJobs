package id.seria.gacha.editor.layouts;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.thirdparty.impl.AdvancedCratesMigration;
import id.seria.gacha.thirdparty.impl.AquaticCratesMigration;
import id.seria.gacha.thirdparty.impl.CrazyCratesMigration;
import id.seria.gacha.thirdparty.impl.CustomCratesMigration;
import id.seria.gacha.thirdparty.impl.ExcellentCratesMigration;
import id.seria.gacha.thirdparty.impl.SpecializedCratesMigration;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

public class EditorMigrationsLayout extends EditorFacade.EditorLayout {
   public EditorMigrationsLayout(EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.migrations"), parent);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         this.createMigrationComponent(
            2,
            2,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFmN2NkZmVhMmQyMWNkNWY2ZWJiZjQ4NDgxNzYxYzZjYmRmMzZkMDBmZTY0MDgzNjg2ZTlhZWFhM2YxZjIxNyJ9fX0=",
            "Excellent Crates",
            "(6.0.1+)",
            "(" + this.t("labels.free")[0] + ")",
            Lists.newArrayList(new String[]{"crates/*.yml", "keys/*.yml"}),
            false,
            click -> {
               try {
                  new ExcellentCratesMigration().convert(click.getViewer());
                  Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
               } catch (Exception e) {
                  Translations.error(click.getViewer(), e);
               }
            }
         )
      );
      this.addComponent(
         view,
         this.createMigrationComponent(
            3,
            2,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y2NmY3ZjAzMTI1Y2Y1NDczMzY5NmYzNjMyZjBkOWU2NDcwYmFhYjg0OTg0N2VhNWVhMmQ3OTE1NmFkMGY0MCJ9fX0=",
            "Crazy Crates",
            "(4.6.3+)",
            "(" + this.t("labels.free")[0] + ")",
            Lists.newArrayList(new String[]{"crates/*.yml"}),
            false,
            click -> {
               try {
                  new CrazyCratesMigration().convert(click.getViewer());
                  Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
               } catch (Exception e) {
                  Translations.error(click.getViewer(), e);
               }
            }
         )
      );
      this.addComponent(
         view,
         this.createMigrationComponent(
            4,
            2,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE3NmMxYWU3MTMwN2U4NTQ3MDA2OTI3ZTk3MWU1MzBkYzBjMzQ4YmRhYjZkYjc0YWJlYWQ3MDFhNDBiMWIwIn19fQ==",
            "Custom Crates",
            "",
            "(" + this.t("labels.free")[0] + ")",
            Lists.newArrayList(new String[]{"crates/*.yml"}),
            false,
            click -> {
               try {
                  new CustomCratesMigration().convert(click.getViewer());
                  Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
               } catch (Exception e) {
                  Translations.error(click.getViewer(), e);
               }
            }
         )
      );
      this.addComponent(
         view,
         this.createMigrationComponent(
            6,
            2,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQ0NDk4YTBmZTI3ODk1NmUzZDA0MTM1ZWY0YjEzNDNkMDU0OGE3ZTIwOGM2MWIxZmI2ZjNiNGRiYzI0MGRhOCJ9fX0=",
            "Specialized Crates",
            "(3.3.9+)",
            "(" + this.t("labels.paid")[0] + ")",
            Lists.newArrayList(new String[]{"crates/*.yml"}),
            false,
            click -> {
               try {
                  new SpecializedCratesMigration().convert(click.getViewer());
                  Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
               } catch (Exception e) {
                  Translations.error(click.getViewer(), e);
               }
            }
         )
      );
      this.addComponent(
         view,
         this.createMigrationComponent(
            7,
            2,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODdhY2I4Yjk5ZDQ3OGJhMzUwNTNlOWYyMTJhY2I1YzU1Y2MxNDQ4NDA0NjhmZDAyNDJiMzlmNWJkNzVhY2I0MSJ9fX0=",
            "Advanced Crates",
            "(4.4.0+)",
            "(" + this.t("labels.paid")[0] + ")",
            Lists.newArrayList(new String[]{"crates/*.yml"}),
            false,
            click -> {
               try {
                  new AdvancedCratesMigration().convert(click.getViewer());
                  Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
               } catch (Exception e) {
                  Translations.error(click.getViewer(), e);
               }
            }
         )
      );
      this.addComponent(
         view,
         this.createMigrationComponent(
               8,
               2,
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE3OTQ1MTk5ZjlhNWZlNjc5ZWE3YWQxZmU5ZTI1YzYwNzk5NWVhMGM4MTRjNTgxYmZjYTI3ZTcxMDRiYzA5YSJ9fX0=",
               "Aquatic Crates 3",
               "(3.2.2+)",
               "(" + this.t("labels.paid")[0] + ")",
               Lists.newArrayList(new String[]{"crates/*.yml"}),
               false,
               click -> {
                  try {
                     new AquaticCratesMigration().convert(click.getViewer());
                     Translations.send(click.getViewer(), this.t("editor.messages.migration-completed")[0]);
                  } catch (Exception e) {
                     Translations.error(click.getViewer(), e);
                  }
               }
            )
            .addWarning("\u00a7e\u00a7nOnly reward item stacks will be migrated!")
      );
   }

   private ClickableComponent createMigrationComponent(
      int x,
      int y,
      String base64,
      String pluginName,
      String requiredVersion,
      String pluginUrl,
      List<String> filesSupported,
      boolean databaseSupported,
      Consumer<ClickViewContext> clickConsumer
   ) {
      return this.createComponent(
            SlotCompound.from(x, y),
            this.createData(
               Utilities.getNMSFactory().createTexturedSkullFromBase64(base64),
               Translatable.key("editor.migrations.plugin.title", "%name%", pluginName + " " + requiredVersion),
               Translatable.key(
                  "editor.migrations.plugin.description",
                  "%url%",
                  pluginUrl,
                  "%name%",
                  pluginName,
                  "%files%",
                  filesSupported,
                  "%database%",
                  this.t(databaseSupported)
               )
            ),
            Lists.newArrayList(new ClickAction[]{new ClickAction(ClickAction.ClickType.LEFT, this.label("migrate-files"))}),
            click -> new ConfirmationMenu(this.getPlugin(), "Migrate?", () -> {
               clickConsumer.accept(click);
               new EditorCratesLayout(this).open(click.getViewer());
            }, () -> this.open(click.getViewer())).open(click.getViewer())
         )
         .addWarning(() -> !Bukkit.getPluginManager().isPluginEnabled(pluginName.replace(" ", "")), pluginName + " not installed!")
         .addWarning(() -> !databaseSupported, "\u00a7eWarning: Player Data will not be migrated. (database)")
         .setLocked(__ -> !Bukkit.getPluginManager().isPluginEnabled(pluginName.replace(" ", "")));
   }
}
