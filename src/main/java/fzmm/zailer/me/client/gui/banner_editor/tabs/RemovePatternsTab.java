package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;

public class RemovePatternsTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_LAYOUT = "remove-patterns-layout";
    @Override
    public String getId() {
        return "removePatterns";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_LAYOUT;
    }

    @Override
    public boolean shouldAddBaseColor() {
        return false;
    }

    @Override
    protected void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent,
                                          BannerPatternsComponent.Layer componentLayer,
                                          BannerBuilder currentBanner, DyeColor selectedColor) {
        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();
            parent.addUndo(currentBanner);

            currentBanner.removeLayer(componentLayer);

            parent.updatePreview(currentBanner);
            return true;
        });
    }
}
