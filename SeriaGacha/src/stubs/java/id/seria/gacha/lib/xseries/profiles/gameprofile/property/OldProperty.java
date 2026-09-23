package id.seria.gacha.lib.xseries.profiles.gameprofile.property;

import com.mojang.authlib.properties.Property;
import javax.annotation.Nullable;
import org.jspecify.annotations.NonNull;

/** ponytail: compile-only stub for legacy (pre-record) Property adapter — replace with pre-compiled bytecode for runtime */
public final class OldProperty extends MojangProperty {
    public OldProperty(Property object) { super(object); }
    @Override public @NonNull String name() { return object.name(); }
    @Override public @NonNull String value() { return object.value(); }
    @Override @Nullable public String signature() { return object.signature(); }
}
