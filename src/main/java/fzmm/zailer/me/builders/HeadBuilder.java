package fzmm.zailer.me.builders;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fzmm.zailer.me.client.logic.FzmmHistory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class HeadBuilder {

    private String skinValue;
    @Nullable
    private String headName;
    @Nullable
    private String signature;
    private UUID uuid;
    private boolean addToHeadHistory;

    private HeadBuilder() {
        this.skinValue = "";
        this.headName = null;
        this.addToHeadHistory = true;
        this.uuid = UUID.randomUUID();
    }

    public static HeadBuilder builder() {
        return new HeadBuilder();
    }

    public ItemStack get() {
        ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();

        stack.apply(DataComponentTypes.PROFILE, null, component -> {

            PropertyMap propertiesMap = new PropertyMap();
            propertiesMap.put("textures", new Property("textures", this.skinValue));

            return new ProfileComponent(
                    Optional.ofNullable(this.headName),
                    Optional.of(this.uuid),
                    propertiesMap
            );
        });

        if (this.addToHeadHistory)
            FzmmHistory.addGeneratedHeads(stack);
        return stack;
    }

    public HeadBuilder skinValue(String skinValue) {
        this.skinValue = skinValue;
        return this;
    }

    public HeadBuilder headName(@Nullable String headName) {
        this.headName = headName;
        return this;
    }

    public HeadBuilder signature(@Nullable String signature) {
        this.signature = signature;
        return this;
    }

    public HeadBuilder id(UUID id) {
        this.uuid = id;
        return this;
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public static ItemStack of(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();

        head.apply(DataComponentTypes.PROFILE, null, component ->
                new ProfileComponent(Optional.of(username), Optional.empty(), new PropertyMap()));

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }

    public static ItemStack of(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();

        head.apply(DataComponentTypes.PROFILE, null, component -> new ProfileComponent(profile));

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }
}
