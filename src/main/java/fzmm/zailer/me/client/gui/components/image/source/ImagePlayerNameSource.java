package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ImagePlayerNameSource implements IImageLoaderFromText, IImageSuggestion {
    private static final String REGEX = "^[a-zA-Z0-9_]{2,16}$";
    private BufferedImage image;

    public ImagePlayerNameSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;

        try {
            boolean isInvalidName = !this.predicateRegex(value);
            GetSkinDecorator getSkinDecorator;
            if (isInvalidName && this.predicateOnlinePlayer(value)) {
                getSkinDecorator = new GetSkinFromCache();
            } else if (isInvalidName) {
                return ImageStatus.INVALID_USERNAME;
            } else {
                getSkinDecorator = new GetSkinFromCache(new GetSkinFromMojang());
            }

            Optional<BufferedImage> optionalImage = ImageUtils.getPlayerSkin(value, getSkinDecorator);
            optionalImage.ifPresent(image -> this.image = image);
            return optionalImage.isEmpty() ? ImageStatus.INVALID_USERNAME : ImageStatus.IMAGE_LOADED;
        } catch (Exception e) {
            FzmmClient.LOGGER.error("Unexpected error loading an image", e);
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean predicate(String value) {
        return this.predicateRegex(value) || this.predicateOnlinePlayer(value);
    }

    private boolean predicateRegex(String value) {
        return value.matches(REGEX);
    }

    private boolean predicateOnlinePlayer(String value) {
        // supports users that are not allowed by the regex, as long as that player is online
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }

        return networkHandler.getPlayerListEntry(value) != null;
    }

    @Override
    public boolean hasTextField() {
        return true;
    }

    @Override
    public SuggestionProvider<?> getSuggestionProvider() {
        return FzmmUtils.SUGGESTION_PLAYER;
    }
}
