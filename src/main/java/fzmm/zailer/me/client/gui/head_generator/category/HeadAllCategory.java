package fzmm.zailer.me.client.gui.head_generator.category;

import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class HeadAllCategory implements IHeadCategory {
    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.all";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return true;
    }

    @Override
    public Text getText() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(0xFFFFFF));
    }

}
