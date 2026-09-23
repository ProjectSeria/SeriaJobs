package id.seria.gacha.api.sdk.models;

public interface Animation {
   String getName();

   void setLoopMode(LoopMode var1);

   void setOverride(boolean var1);

   boolean isFinished();

   double getTime();

   double getLength();

   boolean isSimpleProperty();
}
