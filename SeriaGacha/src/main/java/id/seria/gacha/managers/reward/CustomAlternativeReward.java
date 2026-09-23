package id.seria.gacha.managers.reward;

import id.seria.gacha.Crates;
import id.seria.gacha.api.reward.AlternativeReward;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class CustomAlternativeReward implements AlternativeReward, Cloneable {
   @ConfigField(path = "enabled")
   private boolean enabled = false;
   @ConfigField(path = "reward.item")
   private BaseItem item = BaseItem.from(XMaterial.DIAMOND_PICKAXE);
   @ConfigField(path = "reward.virtual")
   private boolean virtual = false;
   @ConfigField(path = "reward.broadcast")
   private boolean broadcast = false;
   @ConfigField(path = "reward.allow-win-message")
   private boolean allowWinMessage = true;
   @ConfigField(path = "reward.share-players-online")
   private boolean sharePlayersOnline = false;
   @ConfigField(path = "reward.commands")
   private List<String> winCommands = new ArrayList<>();

   @Nullable
   public BaseItem getItem() {
      return this.item != null && this.item.getType() != Material.AIR ? this.item : null;
   }

   @Override
   public boolean isSharePlayersOnline() {
      return Crates.getPlugin().getEdition() != Edition.LITE && this.sharePlayersOnline;
   }

   public CustomAlternativeReward clone() {
      return new CustomAlternativeReward(
         this.enabled, this.item.clone(), this.virtual, this.broadcast, this.allowWinMessage, this.sharePlayersOnline, new ArrayList<>(this.winCommands)
      );
   }

   @Generated
   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   @Override
   public boolean isVirtual() {
      return this.virtual;
   }

   @Generated
   @Override
   public boolean isBroadcast() {
      return this.broadcast;
   }

   @Generated
   @Override
   public boolean isAllowWinMessage() {
      return this.allowWinMessage;
   }

   @Generated
   @Override
   public List<String> getWinCommands() {
      return this.winCommands;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setItem(BaseItem item) {
      this.item = item;
   }

   @Generated
   public void setVirtual(boolean virtual) {
      this.virtual = virtual;
   }

   @Generated
   public void setBroadcast(boolean broadcast) {
      this.broadcast = broadcast;
   }

   @Generated
   public void setAllowWinMessage(boolean allowWinMessage) {
      this.allowWinMessage = allowWinMessage;
   }

   @Generated
   public void setSharePlayersOnline(boolean sharePlayersOnline) {
      this.sharePlayersOnline = sharePlayersOnline;
   }

   @Generated
   public void setWinCommands(List<String> winCommands) {
      this.winCommands = winCommands;
   }

   @Generated
   public CustomAlternativeReward(
      boolean enabled, BaseItem item, boolean virtual, boolean broadcast, boolean allowWinMessage, boolean sharePlayersOnline, List<String> winCommands
   ) {
      this.enabled = enabled;
      this.item = item;
      this.virtual = virtual;
      this.broadcast = broadcast;
      this.allowWinMessage = allowWinMessage;
      this.sharePlayersOnline = sharePlayersOnline;
      this.winCommands = winCommands;
   }

   @Generated
   public CustomAlternativeReward() {
   }
}
