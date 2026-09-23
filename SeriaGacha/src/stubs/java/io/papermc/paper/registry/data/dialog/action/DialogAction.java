package io.papermc.paper.registry.data.dialog.action;

import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import java.util.function.BiConsumer;

public interface DialogAction {
    static DialogAction customClick(BiConsumer<DialogResponseView, Audience> callback, ClickCallback.Options options) {
        return null;
    }
}
