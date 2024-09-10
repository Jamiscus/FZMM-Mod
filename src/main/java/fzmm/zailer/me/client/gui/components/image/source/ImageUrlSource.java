package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.ImageStatus;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

public class ImageUrlSource implements IImageLoaderFromText {
    private BufferedImage image;

    public ImageUrlSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;
        try {
            if (value.isEmpty()) {
                return ImageStatus.NO_IMAGE_LOADED;
            }

            Optional<BufferedImage> optionalImage = ImageUtils.getImageFromUrl(value);
            this.image = optionalImage.orElse(null);

            return optionalImage.isEmpty() ? ImageStatus.URL_HAS_NO_IMAGE : ImageStatus.IMAGE_LOADED;
        } catch (MalformedURLException | IllegalArgumentException ignored) {
            return ImageStatus.MALFORMED_URL;
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[ImageUrlSource] Unexpected error loading an image", e);
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean predicate(String value) {
        return true;
    }

    @Override
    public boolean hasTextField() {
        return true;
    }
}
