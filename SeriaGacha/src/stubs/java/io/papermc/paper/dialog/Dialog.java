package io.papermc.paper.dialog;

import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import net.kyori.adventure.dialog.DialogLike;
import java.util.function.Consumer;

public interface Dialog extends DialogLike {
    static Dialog create(Consumer<DialogRegistryEntry.Builder> consumer) {
        return null;
    }
}
