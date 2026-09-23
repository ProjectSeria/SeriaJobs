package id.seria.gacha.lib.xseries.profiles.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/** ponytail: compile-only stub for legacy (pre-record) GameProfile adapter — replace with pre-compiled bytecode for runtime */
public final class OldGameProfile extends MojangGameProfile {
    public OldGameProfile(GameProfile object) { super(object); }
    @Override public UUID id() { return object.getId(); }
    @Override public String name() { return object.getName(); }
    @Override public PropertyMap properties() { return object.getProperties(); }
    @Override public MojangGameProfile copy(@Nullable Consumer<PropertyModifier> modifier) {
        GameProfile clone = new GameProfile(id(), name());
        clone.getProperties().putAll(properties());
        if (modifier != null) {
            PropertyModifier mod = new PropertyModifier(clone.getProperties());
            modifier.accept(mod);
        }
        return new OldGameProfile(clone);
    }
}
