package id.seria.gacha.managers.crates.animations;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.OpeningAnimationHandler;
import id.seria.gacha.api.crate.animation.PhaseRunner;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.PlayerUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockEngineData;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import org.bukkit.entity.Player;

public class OpeningAnimationHandlerImpl implements OpeningAnimationHandler {
   protected final Player player;
   protected final CrateOpeningSession session;
   protected final Crate crate;
   private int currentPhaseIndex;
   private PhaseRunner currentPhase;
   private final List<PhaseRunner> phases = new CopyOnWriteArrayList<>();
   private boolean hiddenCrate;
   private boolean openedCrate;
   private boolean done;
   @Deprecated
   private List<? extends BaseReward> generatedRewards;

   public OpeningAnimationHandlerImpl(CrateOpeningSession session, Player player) {
      this.session = session;
      this.crate = session.getCrate();
      this.player = player;
   }

   public void init() {
      OpeningAnimation animation = MathUtil.random(this.crate.getType().getOpeningAnimations().list());

      for (OpeningPhase phase : animation.map().values()) {
         PhaseRunner phaseAnimation = phase.getPhase().createRunner(this, phase.getProperties());
         if (phaseAnimation != null) {
            this.createPhase(phaseAnimation);
         }
      }
   }

   public void tick() {
      if (!this.done) {
         if (this.currentPhase != null) {
            this.currentPhase.baseTick();
         }
      }
   }

   public void unload() {
      if (!this.done && this.currentPhase != null) {
         this.currentPhase.baseUnload();
      }

      if (!(this.session.getVirtualDisplayComponent() instanceof ModelEngineDisplayComponent)) {
         this.closeCrate(false);
         this.showCrate();
      }
   }

   @Override
   public void nextPhase() {
      if (!this.done) {
         this.currentPhase.baseUnload();
         if (this.currentPhaseIndex + 1 < this.phases.size()) {
            (this.currentPhase = this.phases.get(this.currentPhaseIndex + 1)).baseLoad();
            this.session.getVirtualDisplayComponent().playAnimation(this.currentPhase.getPhase().getType());
            this.currentPhaseIndex++;
         } else {
            this.done = true;
            this.unload();
         }
      }
   }

   public void createPhase(PhaseRunner newPhase) {
      synchronized (this.phases) {
         if (this.phases.isEmpty()) {
            this.currentPhase = newPhase;
            newPhase.baseLoad();
            this.session.getVirtualDisplayComponent().playAnimation(newPhase.getPhase().getType());
         }

         this.phases.add(newPhase);
      }
   }

   public List<? extends BaseReward> generateRewards() {
      if (this.generatedRewards == null) {
         return this.session.isReplaying()
            ? (this.generatedRewards = this.session.getRewardsGiven())
            : (this.generatedRewards = this.session.getRewardsGenerator().generateRewards());
      } else {
         return this.generatedRewards;
      }
   }

   public void hideCrate() {
      if (!this.hiddenCrate) {
         this.hiddenCrate = true;
         this.openedCrate = false;
         this.crate.getDisplayComponent().hide(this.getAudience());
         this.closeCrate(false);
      }
   }

   public void showCrate() {
      if (this.hiddenCrate) {
         this.hiddenCrate = false;
         this.crate.getDisplayComponent().show(this.getAudience());
         PlayerUtil.playSound(this.getAudience(), XSound.BLOCK_WOOD_PLACE, 1.0F, 1.0F);
         ParticleEffect.BLOCK_CRACK
            .display(
               this.crate.getLocation().clone().add(0.5, 0.5, 0.5),
               0.5F,
               0.5F,
               0.5F,
               0.3F,
               60,
               new BlockTexture(this.crate.getType().getBlockType().getBukkitMaterial()),
               this.getAudience()
            );
      }
   }

   public boolean isBlockOpeningAnimationEnabled() {
      Object engineData = this.crate.getType().getEngineData();
      return !(engineData instanceof VanillaBlockEngineData) || ((VanillaBlockEngineData)engineData).isBlockOpeningAnimationEnabled();
   }

   public void openCrate(boolean silent) {
      if (!this.openedCrate && this.isBlockOpeningAnimationEnabled()) {
         this.openedCrate = true;
         Utilities.getNMSFactory().changeChestState(this.getAudience(), this.crate.getLocation(), true, silent);
      }
   }

   public void closeCrate(boolean silent) {
      if (this.openedCrate) {
         this.openedCrate = false;
         Utilities.getNMSFactory().changeChestState(this.getAudience(), this.crate.getLocation(), false, silent);
      }
   }

   public PhoenixCrates getPlugin() {
      return this.crate.getPlugin();
   }

   public List<Player> getAudience() {
      return this.getCrate().getType().isSimultaneousOpeningsEnabled() ? Lists.newArrayList(new Player[]{this.player}) : this.crate.getNearbyPlayers();
   }

   @Generated
   @Override
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public CrateOpeningSession getSession() {
      return this.session;
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   @Override
   public int getCurrentPhaseIndex() {
      return this.currentPhaseIndex;
   }

   @Generated
   @Override
   public PhaseRunner getCurrentPhase() {
      return this.currentPhase;
   }

   @Generated
   @Override
   public List<PhaseRunner> getPhases() {
      return this.phases;
   }

   @Generated
   public boolean isHiddenCrate() {
      return this.hiddenCrate;
   }

   @Generated
   public boolean isOpenedCrate() {
      return this.openedCrate;
   }

   @Generated
   public boolean isDone() {
      return this.done;
   }

   @Deprecated
   @Generated
   public List<? extends BaseReward> getGeneratedRewards() {
      return this.generatedRewards;
   }
}
