package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.SpawnEggBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.DisplayEntityBillboardOption;
import fzmm.zailer.me.client.gui.options.TextDisplayAlignmentOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;


@SuppressWarnings("UnstableApiUsage")
public class ImagetextTextDisplayTab implements IImagetextTab {
    private static final String TEXT_OPACITY_ID = "textDisplayTextOpacity";
    private static final String BACKGROUND_COLOR_ID = "textDisplayBackgroundColor";
    private static final String TEXT_SHADOW_ID = "textDisplayTextShadow";
    private static final String TEXT_SEE_THROUGH_ID = "textDisplayTextSeeThrough";
    private static final String TEXT_ALIGNMENT_ID = "textDisplayTextAlignment";
    private static final String BILLBOARD_ID = "textDisplayBillboard";
    private static final String ROTATION_ID = "textDisplayRotation";
    private static final String TEXT_DISPLAY_TAG = "ImagetextTextDisplay";
    private SliderWidget textOpacity;
    private ConfigTextBox backgroundColor;
    private SmallCheckboxComponent textShadow;
    private SmallCheckboxComponent textSeeThrough;
    private EnumWidget textAlignment;
    private EnumWidget billboard;
    private SliderWidget rotation;

    public ImagetextTextDisplayTab() {
    }

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        NbtCompound textDisplayNbt = new NbtCompound();
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        textDisplayNbt.putString(DisplayEntity.TextDisplayEntity.TEXT_NBT_KEY, Text.Serialization.toJsonString(logic.getText(), registryManager));
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_LINE_WIDTH, logic.getLineWidth());

        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_TEXT_OPACITY, (int) this.textOpacity.discreteValue());
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_BACKGROUND, ((Color) this.backgroundColor.parsedValue()).argb());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SHADOW, this.textShadow.checked());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SEE_THROUGH, this.textSeeThrough.checked());
        textDisplayNbt.putString(TagsConstant.TEXT_DISPLAY_ALIGNMENT, ((TextDisplayAlignmentOption) this.textAlignment.parsedValue()).getType().asString());
        textDisplayNbt.putString(DisplayEntity.BILLBOARD_NBT_KEY, ((DisplayEntityBillboardOption) this.billboard.parsedValue()).getType().asString());

        NbtList rotationList = new NbtList();
        rotationList.add(NbtFloat.of((float) this.rotation.discreteValue()));
        rotationList.add(NbtFloat.of(0f));
        textDisplayNbt.put(TagsConstant.ENTITY_ROTATION_ID, rotationList);

        NbtList tagList = new NbtList();
        tagList.add(NbtString.of(TEXT_DISPLAY_TAG));
        textDisplayNbt.put(TagsConstant.ENTITY_TAG_TAGS_ID, tagList);

        ItemStack spawnEgg = SpawnEggBuilder.builder()
                .entityType(EntityType.TEXT_DISPLAY)
                .entityTag(textDisplayNbt)
                .get();

        FzmmUtils.giveItem(spawnEgg);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        assert MinecraftClient.getInstance().player != null;

        this.textOpacity = SliderRow.setup(rootComponent, TEXT_OPACITY_ID, 255, 0, 255, Integer.class, 0, 10, null);
        this.backgroundColor = ColorRow.setup(rootComponent, BACKGROUND_COLOR_ID, Color.ofArgb(DisplayEntity.TextDisplayEntity.INITIAL_BACKGROUND), true, 0, null);
        this.textShadow = rootComponent.childById(SmallCheckboxComponent.class, TEXT_SHADOW_ID + "-checkbox");
        BaseFzmmScreen.checkNull(this.textShadow, "small-checkbox", TEXT_SHADOW_ID + "-checkbox");
        this.textShadow.checked(false);
        this.textSeeThrough = rootComponent.childById(SmallCheckboxComponent.class, TEXT_SEE_THROUGH_ID + "-checkbox");
        BaseFzmmScreen.checkNull(this.textSeeThrough, "small-checkbox", TEXT_SEE_THROUGH_ID + "-checkbox");
        this.textSeeThrough.checked(false);
        this.textAlignment = EnumRow.setup(rootComponent, TEXT_ALIGNMENT_ID, TextDisplayAlignmentOption.LEFT, null);
        this.billboard = EnumRow.setup(rootComponent, BILLBOARD_ID, DisplayEntityBillboardOption.FIXED, null);
        this.rotation = SliderRow.setup(rootComponent, ROTATION_ID, MathHelper.wrapDegrees(MinecraftClient.getInstance().player.getYaw()), -180, 180, Float.class, 1, 30, null);
    }

    @Override
    public String getId() {
        return "textDisplay";
    }


    @Override
    public IMementoObject createMemento() {
        return new TextDisplayMementoTab(
                (int) this.textOpacity.discreteValue(),
                this.backgroundColor.getText(),
                this.textShadow.checked(),
                this.textSeeThrough.checked(),
                (TextDisplayAlignmentOption) this.textAlignment.parsedValue(),
                (DisplayEntityBillboardOption) this.billboard.parsedValue()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        TextDisplayMementoTab memento = (TextDisplayMementoTab) mementoTab;
        this.textOpacity.setFromDiscreteValue(memento.textOpacity);
        this.backgroundColor.text(memento.backgroundColor);
        this.textShadow.checked(memento.textShadow);
        this.textSeeThrough.checked(memento.textSeeThrough);
        this.textAlignment.setValue(memento.textAlignment);
        this.billboard.setValue(memento.billboard);
    }

    private record TextDisplayMementoTab(int textOpacity, String backgroundColor, boolean textShadow,
                                         boolean textSeeThrough, TextDisplayAlignmentOption textAlignment,
                                         DisplayEntityBillboardOption billboard) implements IMementoObject {
    }
}
