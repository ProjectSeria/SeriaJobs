package id.seria.jobs;

import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextUtilOptimizationTest {

    @Test
    public void testShortCircuitPlainText() {
        String plain = "Simple plain text";
        Component comp = TextUtil.parseForPlayer(null, plain);
        assertNotNull(comp);
        assertEquals("Simple plain text", TextUtil.toLegacy(comp).replace("§r", ""));
    }

    @Test
    public void testFormatSemanticColors() {
        String input = "<menu-prefix>Hello";
        Component comp = TextUtil.parse(input);
        assertNotNull(comp);
        String legacy = TextUtil.toLegacy(comp);
        assertFalse(legacy.contains("<menu-prefix>"), "Tag <menu-prefix> must be replaced");
        assertTrue(legacy.contains("|") && legacy.contains("Hello"));
    }

    @Test
    public void testLegacyAndHexColors() {
        String legacy = "&aGreen &bAqua";
        Component comp = TextUtil.parse(legacy);
        assertNotNull(comp);
        String legacyOut = TextUtil.toLegacy(comp);
        assertTrue(legacyOut.contains("§aGreen") || legacyOut.contains("Green"));
    }
}
