package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddPatternsTab implements IBannerEditorTab {
    private static final String PATTERNS_LAYOUT = "add-patterns-layout";
    private FlowLayout patternsLayout;

    @Override
    public String getId() {
        return "addPatterns";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.patternsLayout = rootComponent.childById(FlowLayout.class, PATTERNS_LAYOUT);
        BaseFzmmScreen.checkNull(patternsLayout, "flow-layout", PATTERNS_LAYOUT);
    }

    @Override
    public void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color) {
        this.patternsLayout.clearChildren();
        List<Component> bannerList = new ArrayList<>();

        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
        Optional<Registry<BannerPattern>> bannerRegistry = registryManager.getOptional(RegistryKeys.BANNER_PATTERN);
        if (bannerRegistry.isEmpty()) {
            FzmmClient.LOGGER.error("[AddPatternsTab] No banner registry found");
            return;
        }

        RegistryKey<BannerPattern> basePattern = BannerPatterns.BASE;

        for (var registry : bannerRegistry.stream().toList()) {
            for (var pattern : registry.streamEntries().toList()) {
                if (basePattern == pattern.registryKey()) {
                    continue;
                }

                ItemStack banner = currentBanner.copy()
                        .addLayer(color, pattern)
                        .get();

                Component itemComponent = Components.item(banner)
                        .sizing(Sizing.fixed(32), Sizing.fixed(32));

                itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    UISounds.playButtonSound();
                    parent.addUndo(currentBanner);

                    currentBanner.addLayer(color, pattern);

                    parent.updatePreview(currentBanner);
                    return true;
                });
                itemComponent.cursorStyle(CursorStyle.HAND);

                bannerList.add(itemComponent);
            }
        }
        this.patternsLayout.children(bannerList);
    }
}
