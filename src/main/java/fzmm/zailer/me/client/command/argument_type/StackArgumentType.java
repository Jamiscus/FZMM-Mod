package fzmm.zailer.me.client.command.argument_type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;

import java.util.concurrent.CompletableFuture;

public class StackArgumentType extends ItemStackArgumentType {

    public StackArgumentType(CommandRegistryAccess commandRegistryAccess) {
        super(commandRegistryAccess);
    }

    public static StackArgumentType itemStack(CommandRegistryAccess commandRegistryAccess) {
        return new StackArgumentType(commandRegistryAccess);
    }

    @Override
    public ItemStackArgument parse(StringReader stringReader) throws CommandSyntaxException {
        if (!ComponentArgumentType.maxDepthCheck(stringReader)) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.createWithContext(stringReader);
        }
        return super.parse(stringReader);
    }

    @Override
    public <S> ItemStackArgument parse(StringReader stringReader, S source) throws CommandSyntaxException {
        if (!ComponentArgumentType.maxDepthCheck(stringReader)) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.createWithContext(stringReader);
        }
        return super.parse(stringReader, source);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!ComponentArgumentType.maxDepthCheck(new StringReader(context.getInput()))) {
            return builder.buildFuture();
        }
        return super.listSuggestions(context, builder);
    }
}
