package io.papermc.paper.registry.data.dialog.type;

import io.papermc.paper.registry.data.dialog.ActionButton;
import java.util.List;

public interface DialogType {
    static DialogType multiAction(List<ActionButton> buttons, ActionButton backButton, int columns) {
        return null;
    }
}
