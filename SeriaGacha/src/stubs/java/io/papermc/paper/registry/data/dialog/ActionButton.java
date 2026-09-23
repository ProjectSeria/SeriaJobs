package io.papermc.paper.registry.data.dialog;

import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;

public interface ActionButton {
    static Builder builder(Component label) {
        return null;
    }

    interface Builder {
        Builder action(DialogAction action);
        ActionButton build();
    }
}
