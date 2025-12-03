package com.randomstrangerpassenger.mcopt.client.bucket;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.Axolotl.Variant;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds lightweight bucket content previews in item tooltips.
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID, value = Dist.CLIENT)
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
    public static void onTooltip(ItemTooltipEvent event) {
        if (!GameplayConfig.ENABLE_BUCKET_PREVIEW.get()) {
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

    private static void addFluidDetails(ItemTooltipEvent event, BucketItem bucketItem) {
        // In 1.21, bucket fluids are determined by the bucket type itself
        // DataComponents system changed - fluids are not stored as components on
        // buckets
        // Skip fluid details for now as API changed significantly
    }

    private static void addMobDetails(ItemTooltipEvent event, ItemStack stack, Item item) {
        EntityType<?> type = MOB_BUCKET_TYPES.get(item);
        if (type != null) {
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.entity",
                    type.getDescription().copy().withStyle(ChatFormatting.GOLD)));
        }

        appendCustomName(stack, event);
        appendAxolotlVariant(stack, event);
        appendTropicalFishVariant(stack, event);
    }

    private static void appendCustomName(ItemStack stack, ItemTooltipEvent event) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            Component name = customName.copy().withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.custom_name", name));
        }
    }

    private static void appendAxolotlVariant(ItemStack stack, ItemTooltipEvent event) {
        net.minecraft.world.item.component.CustomData entityData = (net.minecraft.world.item.component.CustomData) stack
                .getOrDefault(DataComponents.ENTITY_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY);
        if (entityData.isEmpty()) {
            return;
        }

        int variantId = entityData.copyTag().getInt("Variant").orElse(0);
        Variant variant = Axolotl.Variant.byId(variantId);
        Component variantName = AXOLOTL_VARIANTS.get(variant);
        if (variantName != null) {
            Component styledVariant = variantName.copy().withStyle(ChatFormatting.LIGHT_PURPLE);
            event.getToolTip().add(Component.translatable("tooltip.mcopt.bucket.axolotl", styledVariant));
        }
    }

    private static void appendTropicalFishVariant(ItemStack stack, ItemTooltipEvent event) {
        net.minecraft.world.item.component.CustomData entityData = (net.minecraft.world.item.component.CustomData) stack
                .getOrDefault(DataComponents.ENTITY_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY);
        if (entityData.isEmpty() || !stack.is(Items.TROPICAL_FISH_BUCKET)) {
            return;
        }

        int variant = entityData.copyTag().getInt("BucketVariantTag").orElse(0);
        int baseColorIndex = variant & 0xFF;
        int patternColorIndex = (variant >> 8) & 0xFF;
        int patternId = (variant >> 16) & 0xFF;

        Component baseColor = describeDyeColor(baseColorIndex);
        Component patternColor = describeDyeColor(patternColorIndex);
        Component pattern = describeFishPattern(patternId);

        if (baseColor != null || patternColor != null || pattern != null) {
            Component detail = Component.translatable("tooltip.mcopt.bucket.tropical_fish",
                    safeComponent(baseColor), safeComponent(patternColor), safeComponent(pattern))
                    .copy().withStyle(ChatFormatting.DARK_AQUA);
            event.getToolTip().add(detail);
        }
    }

    private static Component describeDyeColor(int index) {
        if (index < 0 || index >= 16) {
            return null;
        }
        return Component.translatable("color.minecraft." + net.minecraft.world.item.DyeColor.byId(index).getName());
    }

    private static Component describeFishPattern(int patternId) {
        // Pattern names in order matching TropicalFish.Pattern enum ordinals
        String[] patternNames = { "kob", "sunstreak", "snooper", "dasher", "brinely", "spotty", "flopper", "stripey",
                "glitter", "blockfish", "betty", "clayfish" };
        if (patternId < 0 || patternId >= patternNames.length) {
            return null;
        }
        return Component.translatable("entity.minecraft.tropical_fish.type." + patternNames[patternId]);
    }

    private static Component safeComponent(Component component) {
        return component == null ? Component.literal("?") : component;
    }
}
