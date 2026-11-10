package org.sylvarion.sylvUtilities.chat;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.event.CarbonEventSubscription;
import net.draycia.carbon.api.event.events.CarbonChatEvent;

/** Kept separate so Carbon API classes are only loaded when Carbon is enabled. */
final class CarbonTextReplacementHook {
    private final CarbonEventSubscription<CarbonChatEvent> subscription;

    CarbonTextReplacementHook(final TextReplacements rules) {
        this.subscription = CarbonChatProvider.carbonChat().eventHandler().subscribe(
                CarbonChatEvent.class, 100, false, event -> event.message(rules.apply(event.message())));
    }

    void disable() {
        this.subscription.dispose();
    }
}
