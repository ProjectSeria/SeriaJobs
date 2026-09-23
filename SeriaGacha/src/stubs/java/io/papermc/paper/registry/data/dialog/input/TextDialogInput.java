package io.papermc.paper.registry.data.dialog.input;

public interface TextDialogInput extends DialogInput {
    interface MultilineOptions {
        static MultilineOptions create(int maxLines, int maxCharactersPerLine) {
            return null;
        }
    }
}
