package fzmm.zailer.me.compat.placeholder_api;

import eu.pb4.placeholders.api.parsers.TagParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import net.minecraft.text.Text;

public class PlaceholderApiCompat {

    public static Text parse(String inputText) {
        if (!CompatMods.PLACEHOLDER_API_PRESENT)
            return Text.literal(inputText);


        try {
            return TagParser.DEFAULT.parseNode(inputText).toText();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[PlaceholderApiCompat] Failed to parse text", e);
            CompatMods.PLACEHOLDER_API_PRESENT = false;
            return Text.literal(inputText);
        }
    }
}
