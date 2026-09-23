package io.papermc.paper.registry.data.dialog;

import io.papermc.paper.registry.data.dialog.type.DialogType;

public interface DialogRegistryEntry {
    interface Builder {
        Builder empty();
        Builder base(DialogBase base);
        Builder type(DialogType type);
        DialogRegistryEntry build();
    }
}
