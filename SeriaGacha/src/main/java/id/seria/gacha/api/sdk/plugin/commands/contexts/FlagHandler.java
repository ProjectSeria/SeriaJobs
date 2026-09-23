package id.seria.gacha.api.sdk.plugin.commands.contexts;

public interface FlagHandler {
   boolean hasFlag(String var1);

   String getFlagValue(String var1, String var2);

   Integer getFlagValue(String var1, Integer var2);

   Long getFlagValue(String var1, Long var2);

   Float getFlagValue(String var1, Float var2);

   Double getFlagValue(String var1, Double var2);

   Integer getIntFlagValue(String var1, Number var2);

   Long getLongFlagValue(String var1, Number var2);

   Float getFloatFlagValue(String var1, Number var2);

   Double getDoubleFlagValue(String var1, Number var2);

   Double getFlagValue(String var1, Number var2);
}
