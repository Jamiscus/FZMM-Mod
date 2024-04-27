package fzmm.zailer.me.client.logic.copy_text_algorithm;

import fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms.*;

public class CopyText {
    private static final AbstractCopyTextAlgorithm[] algorithms;

    static {
        algorithms = new AbstractCopyTextAlgorithm[] {
                new CopyTextAsJson(),
                new CopyTextAsChatDefault(),
                new CopyTextAsChatLegacy(),
                new CopyTextAsConsole(),
                new CopyTextAsMOTD(),
                new CopyTextAsXml(),
                new CopyTextAsBBCode(),
                new CopyTextAsString()
        };
    }

    public static AbstractCopyTextAlgorithm[] getAlgorithms() {
        return algorithms;
    }
}
