package id.seria.gacha.lib.xseries.profiles.objects;

import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.ProfilesCore;
import id.seria.gacha.lib.xseries.profiles.exceptions.InvalidProfileContainerException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class ProfileContainer<T> implements Profileable {
   public ProfileContainer() {
   }

   @NotNull
   public abstract void setProfile(@Nullable MojangGameProfile var1);

   @NotNull
   public abstract T getObject();

   @Override
   public boolean isReady() {
      return true;
   }

   @Override
   public final String toString() {
      return this.getClass().getSimpleName() + '[' + this.getObject() + ']';
   }

   public static final class BlockProfileContainer extends ProfileContainer<Block> implements DelegateProfileable {
      private final Block block;

      public BlockProfileContainer(Block block) {
         this.block = Objects.requireNonNull(block, "Block is null");
      }

      private Skull getBlockState() {
         BlockState state = this.block.getState();
         if (!(state instanceof Skull)) {
            throw new InvalidProfileContainerException(this.block, "Block can't contain texture: " + this.block);
         } else {
            return (Skull)state;
         }
      }

      @Override
      public void setProfile(@Nullable MojangGameProfile profile) {
         Skull state = this.getBlockState();
         new ProfileContainer.BlockStateProfileContainer(state).setProfile(profile);
         state.update(true);
      }

      public Block getObject() {
         return this.block;
      }

      @Override
      public Profileable getDelegateProfile() {
         return new ProfileContainer.BlockStateProfileContainer(this.getBlockState());
      }
   }

   public static final class BlockStateProfileContainer extends ProfileContainer<Skull> {
      private final Skull state;

      public BlockStateProfileContainer(Skull state) {
         this.state = Objects.requireNonNull(state, "Skull BlockState is null");
      }

      @Override
      public void setProfile(@Nullable MojangGameProfile profile) {
         try {
            ProfilesCore.CraftSkull_profile$setter.invoke((Skull)this.state, (Object)PlayerProfiles.toResolvableProfile(profile));
         } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to set profile " + profile + " to " + this.state, throwable);
         }
      }

      public Skull getObject() {
         return this.state;
      }

      @Override
      public MojangGameProfile getProfile() {
         try {
            return PlayerProfiles.fromResolvableProfile((Object)ProfilesCore.CraftSkull_profile$getter.invoke((Skull)this.state));
         } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to get profile fr om blockstate: " + this.state, throwable);
         }
      }
   }

   public static final class ItemMetaProfileContainer extends ProfileContainer<ItemMeta> {
      private final ItemMeta meta;

      public ItemMetaProfileContainer(SkullMeta meta) {
         this.meta = Objects.requireNonNull(meta, "ItemMeta is null");
      }

      @Override
      public void setProfile(@Nullable MojangGameProfile profile) {
         try {
            ProfilesCore.CraftMetaSkull_profile$setter.invoke((ItemMeta)this.meta, (Object)PlayerProfiles.toResolvableProfile(profile));
         } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to set profile " + profile + " to " + this.meta, throwable);
         }
      }

      public ItemMeta getObject() {
         return this.meta;
      }

      @Override
      public MojangGameProfile getProfile() {
         try {
            return PlayerProfiles.fromResolvableProfile((Object)ProfilesCore.CraftMetaSkull_profile$getter.invoke((SkullMeta)((SkullMeta)this.meta)));
         } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get profile from item meta: " + this.meta, throwable);
         }
      }
   }

   public static final class ItemStackProfileContainer extends ProfileContainer<ItemStack> implements DelegateProfileable {
      private final ItemStack itemStack;

      public ItemStackProfileContainer(ItemStack itemStack) {
         this.itemStack = Objects.requireNonNull(itemStack, "ItemStack is null");
      }

      private ProfileContainer.ItemMetaProfileContainer getMetaContainer(ItemMeta meta) {
         if (!(meta instanceof SkullMeta)) {
            throw new InvalidProfileContainerException(this.itemStack, "Item can't contain texture: " + this.itemStack);
         } else {
            return new ProfileContainer.ItemMetaProfileContainer((SkullMeta)meta);
         }
      }

      @Override
      public void setProfile(@Nullable MojangGameProfile profile) {
         ItemMeta meta = this.itemStack.getItemMeta();
         this.getMetaContainer(meta).setProfile(profile);
         this.itemStack.setItemMeta(meta);
      }

      public ItemStack getObject() {
         return this.itemStack;
      }

      @Override
      public Profileable getDelegateProfile() {
         return this.getMetaContainer(this.itemStack.getItemMeta());
      }
   }
}
