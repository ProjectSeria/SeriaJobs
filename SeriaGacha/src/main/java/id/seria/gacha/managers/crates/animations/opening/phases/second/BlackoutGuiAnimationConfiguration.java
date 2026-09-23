package id.seria.gacha.managers.crates.animations.opening.phases.second;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.List;
import lombok.Generated;

public class BlackoutGuiAnimationConfiguration extends Configuration {
   @ConfigField(path = "title")
   private String title = "&8&lOpening...";
   @ConfigField(path = "marker.normal")
   private CSGOGuiAnimationConfiguration.ItemAppearance markerNormalItem = new CSGOGuiAnimationConfiguration.ItemAppearance(
      new ComplexMaterial(XMaterial.LIME_STAINED_GLASS_PANE), 1, 0, "", false, false, "&r", Lists.newArrayList(), true
   );
   @ConfigField(path = "marker.flicker")
   private CSGOGuiAnimationConfiguration.ItemAppearance markerFlickerItem = new CSGOGuiAnimationConfiguration.ItemAppearance(
      new ComplexMaterial(XMaterial.YELLOW_STAINED_GLASS_PANE), 1, 0, "", false, false, "&r", Lists.newArrayList(), true
   );
   @ConfigField(path = "reward-item.chance-lore")
   private List<String> chanceLore = Lists.newArrayList(new String[]{"", "&eYour chance: &6%chance%%"});

   public BlackoutGuiAnimationConfiguration() {
   }

   public BlackoutGuiAnimationConfiguration clone() {
      return new BlackoutGuiAnimationConfiguration();
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public CSGOGuiAnimationConfiguration.ItemAppearance getMarkerNormalItem() {
      return this.markerNormalItem;
   }

   @Generated
   public CSGOGuiAnimationConfiguration.ItemAppearance getMarkerFlickerItem() {
      return this.markerFlickerItem;
   }

   @Generated
   public List<String> getChanceLore() {
      return this.chanceLore;
   }
}
