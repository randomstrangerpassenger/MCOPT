package com.randomstrangerpassenger.mcopt.client.bucket;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.Axolotl.Variant;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomName;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DataComponents;
import net.minecraft.world.item.component.FluidContents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adds lightweight bucket content previews in item tooltips.
 */
public class BucketPreviewHandler {

    private static final Map<Item, EntityType<?>> MOB_BUCKET_TYPES = new HashMap<>();
    private static final Map<Axolotl.Variant, Component> AXOLOTL_VARIANTS = new EnumMap<>(Axolotl.Variant.class);

    static {
        MOB_BUCKET_TYPES.put(Items.AXOLOTL_BUCKET, EntityType.AXOLOTL);
        MOB_BUCKET_TYPES.put(Items.COD_BUCKET, EntityType.COD);
        MOB_BUCKET_TYPES.put(Items.PUFFERFISH_BUCKET, EntityType.PUFFERFISH);
        MOB_BUCKET_TYPES.put(Items.SALMON_BUCKET, EntityType.SALMON);
        MOB_BUCKET_TYPES.put(Items.TROPICAL_FISH_BUCKET, EntityType.TROPICAL_FISH);
        MOB_BUCKET_TYPES.put(Items.TADPOLE_BUCKET, EntityType.TADPOLE);

        AXOLOTL_VARIANTS.put(Variant.LUCY, Component.translatable("tooltip.mcopt.bucket.axolotl.lucy"));
        AXOLOTL_VARIANTS.put(Variant.WILD, Component.translatable("tooltip.mcopt.bucket.axolotl.wild"));
        AXOLOTL_VARIANTS.put(Variant.GOLD, Component.translatable("tooltip.mcopt.bucket.axolotl.gold"));
        AXOLOTL_VARIANTS.put(Variant.CYAN, Component.translatable("tooltip.mcopt.bucket.axolotl.cyan"));
        AXOLOTL_VARIANTS.put(Variant.BLUE, Component.translatable("tooltip.mcopt.bucket.axolotl.blue"));
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!MCOPTConfig.ENABLE_BUCKET_PREVIEW.get()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (item instanceof BucketItem bucketItem) {
            addFluidDetails(event, bucketItem);
        }

        if (item instanceof MobBucketItem) {
            addMobDetails(event, stack, item);
        }
    }

    private void addFluidDetails(ItemTooltipEvent event, BucketItem bucketItem) {
        Optional<FluidContents> contents = event.getItemStack().getComponents().get(DataComponents.FLUID_CONTENTS);
        Fluid fluid = contents.map(FluidContents::fluid).orElse(bucketItem.getFluid());
        if (fluid == null) {
            return;
        }

        Component fluidName = fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.AQUA);
        event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.fluid", fluidName));
    }

    private void addMobDetails(ItemTooltipEvent event, ItemStack stack, Item item) {
        EntityType<?> type = MOB_BUCKET_TYPES.get(item);
        if (type != null) {
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.entity",
                    type.getDescription().copy().withStyle(ChatFormatting.GOLD)));
        }

        appendCustomName(stack, event);
        appendAxolotlVariant(stack, event);
        appendTropicalFishVariant(stack, event);
    }

    private void appendCustomName(ItemStack stack, ItemTooltipEvent event) {
        CustomName customName = stack.getComponents().get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            Component name = customName.value().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.custom_name", name));
        }
    }

    private void appendAxolotlVariant(ItemStack stack, ItemTooltipEvent event) {
        CustomData entityData = stack.getComponents().get(DataComponents.ENTITY_DATA);
        if (entityData == null) {
            return;
        }

        Variant variant = Axolotl.Variant.byId(entityData.copyTag().getInt("Variant"));
        Component variantName = AXOLOTL_VARIANTS.get(variant);
        if (variantName != null) {
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.axolotl", variantName.withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }

    private void appendTropicalFishVariant(ItemStack stack, ItemTooltipEvent event) {
        CustomData entityData = stack.getComponents().get(DataComponents.ENTITY_DATA);
        if (entityData == null || !stack.is(Items.TROPICAL_FISH_BUCKET)) {
            return;
        }

        int variant = entityData.copyTag().getInt(TropicalFish.BUCKET_VARIANT_TAG);
        int baseColorIndex = variant & 0xFF;
        int patternColorIndex = (variant >> 8) & 0xFF;
        int patternId = (variant >> 16) & 0xFF;

        Component baseColor = describeDyeColor(baseColorIndex);
        Component patternColor = describeDyeColor(patternColorIndex);
        Component pattern = describeFishPattern(patternId);

        if (baseColor != null || patternColor != null || pattern != null) {
            Component detail = Component.translatable("tooltip.mcopt.bucket.tropical_fish",
                    safeComponent(baseColor), safeComponent(patternColor), safeComponent(pattern))
                    .withStyle(ChatFormatting.DARK_AQUA);
            event.getToolTip().add(detail);
        }
    }

    private Component describeDyeColor(int index) {
        if (index < 0 || index >= 16) {
            return null;
        }
        return Component.translatable("color.minecraft." + net.minecraft.world.item.DyeColor.byId(index).getName());
    }

    private Component describeFishPattern(int patternId) {
        TropicalFish.Pattern[] patterns = TropicalFish.Pattern.values();
        if (patternId < 0 || patternId >= patterns.length) {
            return null;
        }
        return Component.translatable("entity.minecraft.tropical_fish.type." + patterns[patternId].getName());
    }

    private Component safeComponent(Component component) {
        return component == null ? Component.literal("?") : component;
    }
}
