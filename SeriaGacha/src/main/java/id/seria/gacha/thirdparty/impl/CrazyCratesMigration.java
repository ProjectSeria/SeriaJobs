package id.seria.gacha.thirdparty.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.type.CrateBlockType;
import id.seria.gacha.managers.crates.type.list.OpeningAnimationWrapper;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.reward.CustomAlternativeReward;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.thirdparty.Migration;
import id.seria.gacha.utilities.ReflectionNavigator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrazyCratesMigration extends Migration {
   private final Object plugin = Bukkit.getPluginManager().getPlugin("CrazyCrates");

   public CrazyCratesMigration() {
   }

   @Override
   public boolean isAvailable() {
      return this.plugin != null;
   }

   @Override
   public String getPluginName() {
      return "CrazyCrates";
   }

   @Override
   public void convert(Player owner) throws Exception {
      this.convertKeys();
      this.convertCrates();
   }

   private Object getCrateManager() {
      return ReflectionNavigator.of(this.plugin).method("getPlatform").method("getCrateManager").get();
   }

   private List getCrates() {
      return ReflectionNavigator.of(this.getCrateManager()).method("getCrates").list();
   }

   public void convertKeys() throws Exception {
      for (Object crate : this.getCrates()) {
         String id = ReflectionNavigator.of(crate).method("getFileName").getString();
         ItemStack key = null;

         try {
            key = (ItemStack)ReflectionNavigator.of(crate).method("getKey").get();
         } catch (Exception ignored) {
            key = ItemBuilder.of(Material.TRIPWIRE_HOOK)
               .setDisplayName("\u00a7aCrate Key")
               .setLore("\u00a77This is a lore line!", "", "\u00a7eUse this key in \u00a76%crate%\u00a7e!")
               .build();
         }

         Key crateKey = new Key(true, id, false, false, BaseItem.from(key));
         this.registerKey(crateKey);
      }
   }

   public void convertCrates() throws Exception {
      for (Object crate : this.getCrates()) {
         List<IdleEffect> idleEffects = Lists.newArrayList();
         idleEffects.add(new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.RED, 0.0, 0.0, 0.0, 1.0, 0.1, 2)));
         idleEffects.add(new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.FLAME, Color.WHITE, 0.0, 0.0, 0.0, 2.0, 0.1, 2)));
         idleEffects.add(new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.0, 2)));
         OpeningAnimationWrapper openingAnimations = new OpeningAnimationWrapper(
            new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "DEFAULT").get(), new EmptyOpeningPhaseData()),
            new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "NONE").get(), new EmptyOpeningPhaseData()),
            new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "DEFAULT").get(), new ColoredOpeningPhaseData(Color.WHITE))
         );
         List<CustomReward> registeredRewards = new ArrayList<>();
         double totalWeight = 0.0;
         Object prizes = ReflectionNavigator.of(crate).method("getPrizes").list();
         if (prizes != null) {
            for (Object rawReward : (List)prizes) {
               totalWeight += ReflectionNavigator.of(rawReward).method("getWeight").getDouble();
            }

            int i = 0;

            for (Object rawReward : (List)prizes) {
               List<ItemStack> items = new ArrayList<>();

               for (Object builder : ReflectionNavigator.of(rawReward).method("getItems").list()) {
                  items.add((ItemStack)ReflectionNavigator.of(builder).method("asItemStack").get());
               }

               ItemStack displayItem = (ItemStack)ReflectionNavigator.of(rawReward).method("getDisplayItem", crate).get();
               if (items.isEmpty()) {
                  items.add(displayItem.clone());
               }

               List<String> commands = new ArrayList<>(ReflectionNavigator.of(rawReward).method("getCommands").list());
               double weight = ReflectionNavigator.of(rawReward).method("getWeight").getDouble();
               double chance = 0.0;
               if (weight > 0.0) {
                  chance = weight / totalWeight * 100.0;
               }

               CustomReward reward = new CustomReward(
                  true,
                  true,
                  "reward_" + i++,
                  BaseItem.from(displayItem),
                  items.stream().map(BaseItem::from).collect(Collectors.toList()),
                  chance,
                  false,
                  true,
                  false,
                  commands,
                  -1,
                  -1,
                  0L,
                  0L,
                  0L,
                  0L,
                  -1,
                  -1,
                  new ArrayList<>((List)ReflectionNavigator.of(rawReward).field("permissions").get()),
                  new CustomAlternativeReward()
               );
               registeredRewards.add(reward);
            }
         }

         List<String> hologramLines = Lists.newArrayList(
            new String[]{"&e&l%name%", "", Ui.smallCaps("&aRight &7Click to Open"), Ui.smallCaps("&cLeft &7Click to Preview")}
         );
         ItemStack key = null;

         try {
            key = (ItemStack)ReflectionNavigator.of(crate).method("getKey").get();
         } catch (Exception ignored) {
            key = ItemBuilder.of(Material.TRIPWIRE_HOOK)
               .setDisplayName("\u00a7aCrate Key")
               .setLore("\u00a77This is a lore line!", "", "\u00a7eUse this key in \u00a76%crate%\u00a7e!")
               .build();
         }

         String id = ReflectionNavigator.of(crate).method("getFileName").getString();
         CrateType crateType = CrateFacade.buildDefault(id);
         crateType.setDisplayName("\u00a7a" + id);
         crateType.setBlockType(new CrateBlockType(new ComplexMaterial(XMaterial.CHEST)));
         crateType.setIdleEffects(idleEffects);
         crateType.setOpeningAnimations(openingAnimations);
         crateType.setLinkedKeysIds(Sets.newHashSet(new String[]{id}));
         crateType.setRegisteredRewards(registeredRewards);
         crateType.setGuaranteedRewardType(GuaranteedRewardType.DISABLED);
         this.registerCrate(crateType);
      }
   }
}
