package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.gui.components.image.ImageStatus;

public interface IImageLoaderFromText extends IImageGetter {

    ImageStatus loadImage(String value);

    boolean predicate(String value);
}
