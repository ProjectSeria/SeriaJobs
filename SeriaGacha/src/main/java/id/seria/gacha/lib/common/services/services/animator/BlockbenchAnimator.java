package id.seria.gacha.lib.common.services.services.animator;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BlockbenchAnimator {
   private final ServerPlugin plugin;

   public BlockbenchAnimator(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public ArmorStandAnimation importArmorStandAnimation(File file) throws IOException {
      return this.importArmorStandAnimation(new FileReader(file));
   }

   public ArmorStandAnimation importArmorStandAnimation(InputStream stream) throws IOException {
      return this.importArmorStandAnimation(new InputStreamReader(stream, StandardCharsets.UTF_8));
   }

   public ArmorStandAnimation importArmorStandAnimation(Reader rawReader) throws IOException {
      BufferedReader reader = new BufferedReader(rawReader);

      try {
         double engineVersion = -1.0;
         String firstLine = reader.readLine();
         if (firstLine != null && firstLine.startsWith("ENGINE")) {
            engineVersion = Double.valueOf(firstLine.split(" ")[1]);
            firstLine = reader.readLine();
         }

         if (firstLine != null && firstLine.startsWith("ANIMATION")) {
            ArmorStandAnimation animation = new ArmorStandAnimation();
            String[] animationData = this.extractData(firstLine).split(" ");
            if (animationData.length != 3) {
               throw new IOException("Invalid ANIMATION line format. Expected: 'name type durationTicks'");
            }

            animation.setName(animationData[0]);
            animation.setType(AnimationType.valueOf(animationData[1]));
            animation.setDurationTicks(Integer.parseInt(animationData[2]));

            String line;
            while ((line = reader.readLine()) != null) {
               ActionType actionType = this.getActionType(line);
               String data = this.extractData(line);
               switch (actionType) {
                  case ENTITY:
                     this.processEntityData(animation, data);
                     break;
                  case BONE:
                     this.processBoneData(animation, data);
                     break;
                  case FRAME:
                     this.processFrameData(animation, data);
                     break;
                  default:
                     throw new IOException("Unknown action '" + actionType + "'");
               }
            }

            return animation;
         } else {
            throw new IOException("Invalid file format: " + (engineVersion == -1.0 ? "First" : "Second") + " line must start with 'ANIMATION'");
         }
      } finally {
         if (Collections.singletonList(reader).get(0) != null) {
            reader.close();
         }
      }
   }

   private void processEntityData(ArmorStandAnimation animation, String data) throws IOException {
      String[] boneData = data.split(" ");
      if (boneData.length != 2) {
         throw new IOException("Invalid BONE line format. Expected: 'name entity_type'");
      }

      animation.addEntity(new AnimationEntity(boneData[0]));
   }

   private void processBoneData(ArmorStandAnimation animation, String data) throws IOException {
      String[] boneData = data.split(" ");
      if (boneData.length != 2) {
         throw new IOException("Invalid BONE line format. Expected: 'name flag'");
      }

      String rawName = boneData[0];
      BoneFlag boneFlag = BoneFlag.valueOf(boneData[1]);
      if (boneFlag == BoneFlag.ENTITY) {
         if (rawName.contains("\\.")) {
            throw new IllegalStateException("Entity Name can't contain character \".\"");
         }

         animation.addEntity(new AnimationEntity(rawName));
      } else if (animation.getEntities().size() == 1 && rawName.contains("\\.")) {
         AnimationBone bone = new AnimationBone(rawName, boneFlag);
         animation.addBone((String)animation.getEntities().keySet().toArray()[0], bone);
      } else {
         String[] split = rawName.split("\\.");
         if (split.length < 1) {
            throw new IllegalStateException("Expected at least 1 tag, founded " + split.length + " at \"" + rawName + "\"");
         }

         String entityName = split.length >= 2 ? split[0] : "generic";
         String boneName = split.length >= 2 ? split[1] : split[0];
         AnimationBone bone = new AnimationBone(boneName, boneFlag);
         animation.addBone(entityName, bone);
      }
   }

   private void processFrameData(ArmorStandAnimation animation, String data) throws IOException {
      String[] frameData = data.split(" ");
      if (frameData.length < 2) {
         throw new IOException("Invalid FRAME line format. Expected at least 2 data");
      }

      int frame = Integer.parseInt(frameData[0]);
      String type = frameData[1].toLowerCase();
      if ("effect".equalsIgnoreCase(type)) {
         if (frameData.length != 4) {
            throw new IOException("Invalid FRAME line format. Expected: 'frame effect instruction script'");
         }

         String effect = frameData[2];
         if (!"instruction".equalsIgnoreCase(effect)) {
            return;
         }

         String script = frameData[3];
         animation.addEffectFrame(frame, script);
      } else {
         if (!"position".equalsIgnoreCase(type) && !"rotation".equalsIgnoreCase(type)) {
            throw new IOException("Unknown FRAME line format. Expected 'position', 'rotation' or 'effect'. Founded: " + type);
         }

         if (frameData.length != 6) {
            throw new IOException("Invalid FRAME line format. Expected: 'frame <position/rotation> bone_name x y z'");
         }

         String boneName = frameData[2];
         double x = Double.parseDouble(frameData[3]);
         double y = Double.parseDouble(frameData[4]);
         double z = Double.parseDouble(frameData[5]);
         if ("position".equalsIgnoreCase(type)) {
            animation.addPositionFrame(frame, boneName, x, y, z);
         } else if ("rotation".equalsIgnoreCase(type)) {
            animation.addRotationFrame(frame, boneName, x, y, z);
         }
      }
   }

   private ActionType getActionType(String line) throws IOException {
      for (ActionType actionType : ActionType.values()) {
         if (line.startsWith(actionType.name())) {
            return actionType;
         }
      }

      throw new IOException("Invalid action type found in the file '" + line + "'.");
   }

   private String extractData(String line) throws IOException {
      int index = line.indexOf(32);
      if (index == -1) {
         throw new IOException("Invalid line format: No data found after action.");
      } else {
         return line.substring(index + 1);
      }
   }

   public AnimationSession play(Location location, ArmorStandAnimation animation, List<Player> viewers) {
      AnimationSession session = new AnimationSession(this.plugin, location, animation, viewers);
      session.start();
      return session;
   }
}
