package id.seria.gacha.lib.common.services.services.animator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class TimelineFrame {
   private final Map<String, Vector> positionFrames = new HashMap<>();
   private final Map<String, EulerAngle> rotationFrames = new HashMap<>();
   private final List<String> effectFrames = new ArrayList<>();

   public TimelineFrame() {
   }

   public void addPositionFrame(String key, Vector value) {
      this.positionFrames.put(key, value);
   }

   public void addRotationFrame(String key, EulerAngle value) {
      this.rotationFrames.put(key, value);
   }

   public void addScriptFrame(String instruction) {
      this.effectFrames.add(instruction);
   }

   @Generated
   public Map<String, Vector> getPositionFrames() {
      return this.positionFrames;
   }

   @Generated
   public Map<String, EulerAngle> getRotationFrames() {
      return this.rotationFrames;
   }

   @Generated
   public List<String> getEffectFrames() {
      return this.effectFrames;
   }
}
