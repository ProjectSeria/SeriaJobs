package io.papermc.paper.registry.data.dialog;

import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.text.Component;
import java.util.List;

public interface DialogBase {
    static Builder builder(Component title) {
        return null;
    }

    interface Builder {
        Builder body(List<PlainMessageDialogBody> body);
        Builder inputs(List<? extends DialogInput> inputs);
        DialogBase build();
    }
}
