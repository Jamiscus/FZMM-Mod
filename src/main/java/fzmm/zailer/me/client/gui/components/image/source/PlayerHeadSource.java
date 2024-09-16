package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import fzmm.zailer.me.client.gui.utils.select_item.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerHeadSource implements IInteractiveImageLoader {
    @Nullable
    private BufferedImage image;
    private Consumer<BufferedImage> consumer;
    private BaseFzmmScreen previousScreen;

    public PlayerHeadSource() {
        this.image = null;
    }

    @Override
    public void execute(Consumer<BufferedImage> consumer) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;
        this.consumer = consumer;
        MinecraftClient client = MinecraftClient.getInstance();

        this.previousScreen = client.currentScreen instanceof BaseFzmmScreen baseScreen ? baseScreen : null;
        RequestedItem requestedItem = new RequestedItem(
                itemStack -> itemStack.getItem() == Items.PLAYER_HEAD,
                this::setImage,
                List.of(Items.PLAYER_HEAD.getDefaultStack()),
                Items.PLAYER_HEAD.getName(),
                false
        );
        FzmmUtils.setScreen(new SelectItemScreen(this.previousScreen, requestedItem));
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean hasTextField() {
        return false;
    }

    private void setImage(ItemStack head) {
        Optional<BufferedImage> skinOptional = Optional.empty();

        try {
            if (head != null) {
                skinOptional = HeadUtils.getSkin(head);
            }
        } catch (IOException ignored) {
        }

        this.setImage(skinOptional.orElse(null));
    }

    public void setImage(BufferedImage image) {
        if (this.image != null) {
            this.image.flush();
        }
        FzmmUtils.setScreen(this.previousScreen);
        this.previousScreen = null;

        this.image = image;
        this.consumer.accept(this.image);
    }

}
