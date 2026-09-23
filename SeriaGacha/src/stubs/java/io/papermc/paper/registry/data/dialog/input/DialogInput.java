package io.papermc.paper.registry.data.dialog.input;

import net.kyori.adventure.text.Component;

public interface DialogInput {
    static TextDialogInput text(String id, int lines, Component label, boolean required, String initialValue, int maxLength, TextDialogInput.MultilineOptions options) {
        return null;
    }
}
