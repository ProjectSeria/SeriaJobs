package id.seria.gacha.registries.animation;

import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import java.util.function.Supplier;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AddonLockedAnimationPhaseImpl extends AddonAnimationPhaseImpl {
   public AddonLockedAnimationPhaseImpl(
      Edition edition,
      PhaseType type,
      String id,
      String displayName,
      ItemStack displayItem,
      ServerVersion requiredVersion,
      AnimationPhaseImpl.OpeningPhaseSupplier unsafePhaseSupplier,
      Supplier<OpeningPhaseData> legacyDataSupplier,
      String addonName,
      String addonUrl
   ) {
      super(edition, type, id, displayName, displayItem, requiredVersion, unsafePhaseSupplier, legacyDataSupplier, addonName, addonUrl);
   }

   public void sendUnlockMessage(Player player) {
      TextComponent textComponent = new TextComponent("Click here to unlock " + this.getAddonName() + ": " + this.getAddonUrl());
      textComponent.setColor(ChatColor.YELLOW);
      textComponent.setClickEvent(new ClickEvent(Action.OPEN_URL, this.getAddonUrl()));
      textComponent.setHoverEvent(
         new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open " + this.getAddonUrl()).create())
      );
      player.closeInventory();
      player.spigot().sendMessage(textComponent);
   }
}
