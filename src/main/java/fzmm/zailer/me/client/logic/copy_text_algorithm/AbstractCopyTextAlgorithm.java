package fzmm.zailer.me.client.logic.copy_text_algorithm;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public abstract class AbstractCopyTextAlgorithm {

    public abstract String getId();

    public void copy(Text text) {
        String value = this.getString(text);
        FzmmUtils.copyToClipboard(value);
    }

    public String getString(Text text) {
        Style baseStyle = text.getStyle();

        StringBuilder stringBuilder = new StringBuilder();
        List<Text> siblings = text.getSiblings();
        this.getStringRecursive(stringBuilder, baseStyle, !siblings.isEmpty() ? siblings : List.of(text));

        return stringBuilder.toString();
    }

    protected abstract void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings);
}
