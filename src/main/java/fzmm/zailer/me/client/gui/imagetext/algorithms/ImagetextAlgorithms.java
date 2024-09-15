package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public enum ImagetextAlgorithms implements ITabsEnum {
    CHARACTERS(ImagetextCharactersAlgorithm::new),
    BRAILLE(ImagetextBrailleAlgorithm::new);

    private final Supplier<IImagetextAlgorithm> algorithm;
    private final String id;

    ImagetextAlgorithms(Supplier<IImagetextAlgorithm> algorithm) {
        this.algorithm = algorithm;
        this.id = this.createTab().getId();
    }

    public String getId() {
        return this.id;
    }

    @Override
    public IScreenTab createTab() {
        return this.algorithm.get();
    }

    public Text getText(String baseTranslation) {
        return Text.translatable(BaseFzmmScreen.getTabTranslationKey(baseTranslation) + this.getId());
    }
}
