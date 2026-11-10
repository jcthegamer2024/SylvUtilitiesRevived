package org.sylvarion.sylvUtilities.discord;

import github.scarsz.discordsrv.dependencies.kyori.adventure.key.Key;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.format.NamedTextColor;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscordNexoEmojiBridgeTest {
    private final Component glyph = Component.text("\uE001").font(Key.key("nexo:emoji"));

    @Test
    void translatesAliasesAndStaticAndAnimatedCustomEmojis() {
        final var result = DiscordNexoEmojiBridge.translate(
                Component.text(":lmfao: <:lmfao:123> <a:lmfao:456> :unknown: :LMFAO:"), Map.of("lmfao", glyph));
        assertEquals("\uE001 \uE001 \uE001 :unknown: :LMFAO:",
                PlainTextComponentSerializer.plainText().serialize(result));
    }

    @Test
    void preservesSurroundingFormattingAndGlyphFont() {
        final var result = DiscordNexoEmojiBridge.translate(
                Component.text("Discord ", NamedTextColor.BLUE).append(Component.text(":lmfao:")),
                Map.of("lmfao", glyph));
        assertEquals(NamedTextColor.BLUE, result.color());
        assertTrue(containsGlyph(result));
    }

    private boolean containsGlyph(final Component component) {
        return glyph.equals(component) || component.children().stream().anyMatch(this::containsGlyph);
    }

    @Test
    void leavesUnmappedMessagesAndMiniMessageTextUntouched() {
        final var original = Component.text("<red>Hello <glyph:admin> :other:");
        final var result = DiscordNexoEmojiBridge.translate(original, Map.of("lmfao", glyph));
        assertEquals("<red>Hello <glyph:admin> :other:",
                PlainTextComponentSerializer.plainText().serialize(result));
    }
}
