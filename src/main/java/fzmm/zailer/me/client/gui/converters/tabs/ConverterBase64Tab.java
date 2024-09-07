package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConverterBase64Tab implements IScreenTab {
    private static final String MESSAGE_ID = "message";
    private static final String COPY_DECODED_ID = "copyDecoded";
    private static final String COPY_ENCODED_ID = "copyEncoded";

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        TextFieldWidget messageField = TextBoxRow.setup(rootComponent, MESSAGE_ID, "", 5000);

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_DECODED_ID), true, button -> {
            try {
                byte[] decodedValue = Base64.getDecoder().decode(messageField.getText());
                String decodedMessage = new String(decodedValue, StandardCharsets.UTF_8);
                SnackBarManager.copyToClipboard(decodedMessage);
            } catch (Exception ignored) {
            }
        });

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ENCODED_ID), true, button -> {
            try {
                byte[] messageByte = messageField.getText().getBytes(StandardCharsets.UTF_8);
                String encodedMessage = Base64.getEncoder().encodeToString(messageByte);
                SnackBarManager.copyToClipboard(encodedMessage);
            } catch (Exception ignored) {
            }
        });
    }
}
