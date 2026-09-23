package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

public interface SpeedModifier {
   float DEFAULT_SPEED_MODIFIER = 1.5F;

   double getSpeedModifier();

   void setSpeedModifier(double var1);
}
