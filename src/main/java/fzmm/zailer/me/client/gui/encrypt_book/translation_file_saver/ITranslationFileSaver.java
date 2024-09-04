package fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver;

import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public interface ITranslationFileSaver {

    Text getMessage();

    CompletableFuture<Boolean> save(TranslationEncryptProfile profile);
}
