package io.papermc.paper.registry.data.dialog.body;

import net.kyori.adventure.text.Component;

public interface DialogBody {
    static PlainMessageDialogBody plainMessage(Component text) {
        return null;
    }
}
