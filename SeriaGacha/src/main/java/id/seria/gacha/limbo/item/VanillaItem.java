package id.seria.gacha.limbo.item;

import id.seria.gacha.lib.common.utils.configuration.adapter.Adaptable;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.adapter.BaseItemAdapter;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

@Adaptable(adapter = BaseItemAdapter.class)
public class VanillaItem extends BaseItem {
   private final XMaterial material;

   @Override
   public RItemStack createDefault() {
      return RItemStack.of(this.material);
   }

   @NotNull
   @Override
   public BaseItem clone() {
      return new VanillaItem(this.material);
   }

   @Override
   public String getNamespace() {
      return "vanilla";
   }

   @Generated
   public VanillaItem(XMaterial material) {
      this.material = material;
   }
}
