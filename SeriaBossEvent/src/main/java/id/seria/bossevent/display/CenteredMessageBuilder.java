package id.seria.bossevent.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.map.MinecraftFont;

public class CenteredMessageBuilder {

    private static final int CENTER_PX = 154;
    private static final int SPACE_PX = 4;

    private CenteredMessageBuilder() {}

    /**
     * Centers an Adventure Component for Minecraft default chat font.
     * Ported from Skript getWidth and sendCenteredNative logic.
     */
    public static Component center(Component component) {
        if (component == null) return Component.empty();

        try {
            int width = calculateWidth(component, false);
            if (width <= 0) return component;

            int target = CENTER_PX - (width / 2);
            if (target <= 0) return component;

            int spaceCount = target / SPACE_PX;
            if (spaceCount <= 0) return component;

            return Component.text(" ".repeat(spaceCount)).append(component);
        } catch (Throwable t) {
            // Fallback: return original component on any calculation error
            return component;
        }
    }

    private static int calculateWidth(Component component, boolean parentBold) {
        int total = 0;
        TextDecoration.State boldState = component.decoration(TextDecoration.BOLD);
        boolean isBold = (boldState == TextDecoration.State.TRUE) || (boldState == TextDecoration.State.NOT_SET && parentBold);

        if (component instanceof TextComponent textComp) {
            String content = textComp.content();
            if (content != null && !content.isEmpty()) {
                total += calculateStringWidth(content, isBold);
            }
        }

        for (Component child : component.children()) {
            total += calculateWidth(child, isBold);
        }

        return total;
    }

    private static int calculateStringWidth(String text, boolean bold) {
        if (text == null || text.isEmpty()) return 0;

        // Filter text to only characters between 0x20 and 0x7E
        StringBuilder filtered = new StringBuilder();
        int boldCorrection = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= ' ' && c <= '~') {
                filtered.append(c);
                if (bold) {
                    boldCorrection++;
                }
            }
        }

        if (filtered.length() == 0) return 0;

        try {
            int baseWidth = MinecraftFont.Font.getWidth(filtered.toString());
            return baseWidth + boldCorrection;
        } catch (Exception e) {
            return 0;
        }
    }
}
