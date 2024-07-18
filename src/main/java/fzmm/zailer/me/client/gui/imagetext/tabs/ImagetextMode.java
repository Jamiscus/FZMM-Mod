package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public enum ImagetextMode implements ITabsEnum {
    LORE(ImagetextLoreTab::new),
    BOOK_PAGE(ImagetextBookPageTab::new),
    BOOK_TOOLTIP(ImagetextBookTooltipTab::new),
    TEXT_DISPLAY(ImagetextTextDisplayTab::new),
    SIGN(ImagetextSignTab::new),
    HOLOGRAM(ImagetextHologramTab::new),
    COPY(ImagetextCopyTab::new);

    private final Supplier<IImagetextTab> tabSupplier;
    private final String id;

    ImagetextMode(Supplier<IImagetextTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public IImagetextTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Text getText(String baseTranslation) {
        return Text.translatable(BaseFzmmScreen.getTabTranslationKey(baseTranslation) + this.getId());
    }
}
