package id.seria.gacha.managers.crates.engine;

public abstract class EngineData implements id.seria.gacha.api.crate.engine.EngineData {
   public EngineData() {
   }

   public abstract EngineType getType();

   public abstract EngineData clone();
}
