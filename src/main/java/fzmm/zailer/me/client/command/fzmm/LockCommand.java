package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class LockCommand implements ISubCommand {
    @Override
    public String alias() {
        return "lock";
    }

    @Override
    public String syntax() {
        return "lock <key>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

            String key = ctx.getArgument("key", String.class);
            this.lockContainer(key);
            return 1;

        })).build();
    }

    private void lockContainer(String key) {
        MinecraftClient client = MinecraftClient.getInstance();

        ItemStack containerStack = ItemUtils.from(Hand.MAIN_HAND);
        ItemStack lockStack = ItemUtils.from(Hand.OFF_HAND);

        containerStack.apply(DataComponentTypes.LOCK, ContainerLock.EMPTY, component -> new ContainerLock(key));

        lockStack.apply(DataComponentTypes.CUSTOM_NAME, Text.empty(), component -> Text.literal(key));

        ItemUtils.give(containerStack);
        assert client.interactionManager != null;
        // PlayerInventory.OFF_HAND_SLOT is 40, but OFF_HAND_SLOT is 45 (PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE)
        client.interactionManager.clickCreativeStack(lockStack, PlayerInventory.MAIN_SIZE + PlayerInventory.getHotbarSize());
    }
}
