package com.randomstrangerpassenger.mcopt.fixes;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * ItemStack Data Component sanitizer utility.
 * <p>
 * Removes empty CustomData components to fix item merging bugs.
 * <p>
 * <b>Problem</b>: Identical items fail to merge if one has an empty CustomData
 * component and the other has none.
 * <p>
 * <b>Solution</b>: Automatically remove empty CustomData components to restore
 * vanilla state.
 * <p>
 * <b>Config</b>: Specific items can be blacklisted.
 */
public class ItemDataSanitizer {

    /**
     * Sanitizes the ItemStack by removing empty CustomData components.
     *
     * @param stack The ItemStack to sanitize
     * @return true if the stack was modified
     */
    public static boolean sanitizeEmptyTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // Check blacklist
        if (isBlacklisted(stack)) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            return true;
        }

        return false;
    }

    /**
     * Checks if the item is blacklisted.
     *
     * @param stack The ItemStack to check
     * @return true if blacklisted
     */
    private static boolean isBlacklisted(ItemStack stack) {
        try {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String itemIdStr = itemId.toString();

            return GameplayConfig.ITEM_NBT_SANITIZER_BLACKLIST.get().contains(itemIdStr);
        } catch (Exception e) {
            // Safe fallback
            return false;
        }
    }
}
