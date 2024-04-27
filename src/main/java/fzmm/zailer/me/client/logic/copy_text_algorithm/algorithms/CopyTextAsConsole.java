package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

public class CopyTextAsConsole extends CopyTextAsChatLegacy {
    @Override
    public String getId() {
        return "console";
    }

    @Override
    public String colorCharacter() {
        return "§";
    }
}
