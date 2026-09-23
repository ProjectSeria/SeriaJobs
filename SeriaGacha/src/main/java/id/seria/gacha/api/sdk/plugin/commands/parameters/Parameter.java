package id.seria.gacha.api.sdk.plugin.commands.parameters;

public interface Parameter {
   Class<?> getType();

   String getName();

   Object getDefaultValue();

   boolean isOptional();
}
