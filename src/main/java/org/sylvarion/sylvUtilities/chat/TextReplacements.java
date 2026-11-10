package org.sylvarion.sylvUtilities.chat;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;

/** Immutable rules shared safely by asynchronous chat handlers. */
public final class TextReplacements {
    private final Map<String, String> mappings;
    private final Pattern pattern;

    public TextReplacements(final Map<String, String> mappings) {
        if (mappings.containsKey("")) throw new IllegalArgumentException("Replacement keys must not be empty");
        this.mappings = Map.copyOf(mappings);
        this.pattern = mappings.isEmpty() ? null : Pattern.compile(mappings.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()))
                .map(Pattern::quote).collect(Collectors.joining("|")));
    }

    public Component apply(final Component message) {
        if (this.pattern == null) return message;
        return message.replaceText(builder -> builder.match(this.pattern)
                .replacement((match, original) -> Component.text(this.mappings.get(match.group()))));
    }

    public int size() {
        return this.mappings.size();
    }
}
