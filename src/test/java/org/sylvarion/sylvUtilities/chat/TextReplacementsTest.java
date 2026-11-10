package org.sylvarion.sylvUtilities.chat;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextReplacementsTest {
    private String text(final Component component) {
        return (component instanceof TextComponent t ? t.content() : "")
                + component.children().stream().map(this::text).reduce("", String::concat);
    }

    @Test
    void replacesLiteralBracketsRepeatedlyAndPreservesCase() {
        final var rules = new TextReplacements(Map.of("[u]", "Ⓤ", "$", "dollar"));
        assertEquals("Ⓤ Ⓤ [U] dollar", text(rules.apply(Component.text("[u] [u] [U] $"))));
    }

    @Test
    void longestMatchWinsWithoutCascadingOrParsingTags() {
        final var rules = new TextReplacements(Map.of("a", "b", "ab", "<red>", "b", "c"));
        assertEquals("<red> b c", text(rules.apply(Component.text("ab a b"))));
    }

    @Test
    void preservesSurroundingStyleAndSupportsEmptyReplacement() {
        final var rules = new TextReplacements(Map.of("[u]", "Ⓤ", "remove", ""));
        final var result = rules.apply(Component.text("hello [u] remove", NamedTextColor.GOLD));
        assertEquals("hello Ⓤ ", text(result));
        assertEquals(NamedTextColor.GOLD, result.color());
        assertThrows(IllegalArgumentException.class, () -> new TextReplacements(Map.of("", "bad")));
    }
}
