package fzmm.zailer.me.client.gui.item_editor.common.block_list;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBlockListBuilder implements ISortBuilder<AbstractBlockListBuilder.BlockOrTagData> {
    protected ItemStack stack;
    protected final NbtList blockStringList = new NbtList();

    public abstract String getNbtKey();

    public ItemStack get() {
        NbtCompound nbt = this.stack.getOrCreateNbt();

        if (this.blockStringList.isEmpty()) {
            nbt.remove(TagsConstant.HIDE_FLAGS);
        } else {
            nbt.put(this.getNbtKey(), this.blockStringList.copy());
        }

        if (nbt.isEmpty())
            nbt = null;

        this.stack.setNbt(nbt);

        return this.stack;
    }

    public AbstractBlockListBuilder of(ItemStack stack) {
        this.stack = stack.copy();

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList canPlaceOnList = nbt.getList(this.getNbtKey(), NbtElement.STRING_TYPE);
        this.blockStringList.clear();
        this.blockStringList.addAll(canPlaceOnList);

        return this;
    }

    public List<BlockOrTagData> getBlockList() {
        assert MinecraftClient.getInstance().world != null;
        Registry<Block> blockRegistry = MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.BLOCK);
        List<BlockOrTagData> result = new ArrayList<>();

        for (var nbtStr : this.blockStringList)
            result.add(new BlockOrTagData(blockRegistry, nbtStr.asString()));

        return result;
    }

    public AbstractBlockListBuilder add(Block block) {
        String value = Registries.BLOCK.getId(block).toString();
        this.blockStringList.add(NbtString.of(value));
        return this;
    }

    public AbstractBlockListBuilder add(TagKey<Block> tag) {
        return this.add("#" + tag.id().toString());
    }

    public AbstractBlockListBuilder add(String value) {
        this.blockStringList.add(NbtString.of(value));
        return this;
    }

    public AbstractBlockListBuilder addAll(List<String> blockList) {
        this.blockStringList.addAll(blockList.stream().map(NbtString::of).toList());
        return this;
    }

    public AbstractBlockListBuilder remove(String value) {
        this.blockStringList.remove(NbtString.of(value));
        return this;
    }

    public AbstractBlockListBuilder clear() {
        this.blockStringList.clear();
        return this;
    }

    public boolean contains(String value) {
        return this.blockStringList.contains(NbtString.of(value));
    }

    @Override
    public List<BlockOrTagData> values() {
        return this.getBlockList();
    }

    @Override
    public ISortBuilder<BlockOrTagData> values(List<BlockOrTagData> values) {
        this.blockStringList.clear();
        values.forEach(blockOrTagData -> this.blockStringList.add(NbtString.of(blockOrTagData.getBlockStr())));
        return this;
    }

    @Override
    public BlockOrTagData getValue(int i) {
        return this.getBlockList().get(i);
    }

    public static class BlockOrTagData {
        @Nullable
        private Block block = null;
        @Nullable
        private List<Block> tags = null;
        private final String blockStr;

        public BlockOrTagData(Registry<Block> blockRegistry, String blockStr) {
            this.blockStr = blockStr;
            this.processBlock(blockRegistry);
        }

        public BlockOrTagData(Registry<Block> blockRegistry, Block block) {
            this.blockStr = Registries.BLOCK.getId(block).toString();
            this.processBlock(blockRegistry);
        }

        public BlockOrTagData(Registry<Block> blockRegistry, TagKey<Block> tag) {
            this.blockStr = "#" + tag.id().toString();
            this.processBlock(blockRegistry);
        }

        public BlockOrTagData(@NotNull List<Block> blockList, String blockStr) {
            this.blockStr = blockStr;

            if (blockStr.startsWith("#"))
                this.tags = blockList;
            else
                this.block = blockList.get(0);
        }

        private void processBlock(Registry<Block> blockRegistry) {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> blockOrTag;
            try {
                blockOrTag = BlockArgumentParser.blockOrTag(blockRegistry.getReadOnlyWrapper(), this.blockStr, true);
            } catch (CommandSyntaxException ignored) {
                return;
            }

            if (blockOrTag.left().isPresent()) {
                this.block = blockOrTag.left().get().blockState().getBlock();
            } else if (blockOrTag.right().isPresent()) {
                RegistryEntryList<Block> tag = blockOrTag.right().get().tag();

                this.tags = new ArrayList<>();

                tag.stream().forEach(entry ->
                        entry.getKey().ifPresent(blockRegistryKey -> {
                            Block block = blockRegistry.get(blockRegistryKey);
                            assert block != null;
                            this.tags.add(block);
                        })
                );
            }
        }

        public List<Block> getBlocks() {
            assert this.tags != null || this.block != null;
            return this.tags != null ? this.tags : List.of(this.block);
        }

        public String getBlockStr() {
            return this.blockStr;
        }
    }
}
