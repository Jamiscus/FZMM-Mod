package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.toast.status.IStatus;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.text.Text;

public class BookNbtOverflowToast extends AbstractStatusToast {

    private final IStatus status;

    public BookNbtOverflowToast(int pageLength) {
        this.status = new IStatus() {
            @Override
            public Text getStatusTranslation() {
                return Text.translatable("fzmm.toast.bookTooltip.overflow.title", pageLength, WrittenBookContentComponent.MAX_SERIALIZED_PAGE_LENGTH);
            }

            @Override
            public Text getDetailsTranslation(Object... args) {
                return Text.translatable("fzmm.toast.bookTooltip.overflow.details");
            }

            @Override
            public Icon getIcon() {
                return FzmmIcons.ERROR;
            }

            @Override
            public int getOutlineColor() {
                return 0xA7FF0000;
            }

            @Override
            public int getBackgroundColor() {
                return 0x77000000;
            }
        };
    }

    @Override
    public IStatus getStatus() {
        return this.status;
    }

    @Override
    public Text getDetails() {
        return this.status.getDetailsTranslation();
    }
}
