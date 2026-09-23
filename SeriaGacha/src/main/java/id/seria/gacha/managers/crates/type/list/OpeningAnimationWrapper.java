package id.seria.gacha.managers.crates.type.list;

import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

@Deprecated
public class OpeningAnimationWrapper implements Cloneable {
   private final List<OpeningAnimation> animations = new ArrayList<>();

   public OpeningAnimationWrapper() {
      this.animations.add(OpeningAnimation.defaultAnimation());
   }

   public OpeningAnimationWrapper(OpeningPhase preOpenPhase, OpeningPhase openPhase, OpeningPhase displayPhase) {
      OpeningAnimation animation = new OpeningAnimation(preOpenPhase, openPhase, displayPhase);
      this.animations.add(animation);
   }

   public OpeningAnimationWrapper(OpeningAnimation animation) {
      this.animations.add(animation);
   }

   public OpeningAnimationWrapper(Collection<OpeningAnimation> animations) {
      Validate.isTrue(!animations.isEmpty(), "There should be at least one opening animation", new Object[0]);
      this.animations.addAll(animations);
   }

   public void add(OpeningAnimation animation) {
      this.animations.add(animation);
   }

   public void remove(OpeningAnimation animation) {
      this.animations.remove(animation);
   }

   public List<OpeningAnimation> list() {
      if (this.animations.isEmpty()) {
         this.animations.add(OpeningAnimation.defaultAnimation());
      }

      return this.animations;
   }

   public OpeningAnimationWrapper clone() {
      OpeningAnimationWrapper clonedList = new OpeningAnimationWrapper();
      clonedList.animations.addAll(this.animations.stream().map(OpeningAnimation::clone).collect(Collectors.toList()));
      return clonedList;
   }
}
