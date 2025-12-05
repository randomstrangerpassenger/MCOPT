package com.randomstrangerpassenger.mcopt.fixes;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.Objects;

/**
 * ItemStack Data Component sanitizer utility.
 * <p>
 * Removes empty CustomData components to fix item merging bugs.
 * Also sanitizes book pages and NBT data size to prevent server lag.
 * <p>
 * <b>Problem</b>: Identical items fail to merge if one has an empty CustomData
 * component and the other has none. Also, books with excessive pages can
 * cause server crashes (book bomb attacks).
 * <p>
 * <b>Solution</b>: Automatically remove empty CustomData components to restore
 * vanilla state. Limit book pages and NBT size.
 * <p>
 * <b>Config</b>: Specific items can be blacklisted.
 */
@SuppressWarnings("null")
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

        DataComponentType<CustomData> customDataType = Objects.requireNonNull(
                DataComponents.CUSTOM_DATA, "CUSTOM_DATA component type cannot be null");
        CustomData customData = stack.get(customDataType);
        if (customData != null && customData.isEmpty()) {
            stack.remove(customDataType);
            return true;
        }

        return false;
    }

    /**
     * Sanitizes book content by limiting pages.
     * Prevents "book bomb" attacks that can crash servers.
     *
     * @param stack The ItemStack to sanitize (should be a book)
     * @return true if the stack was modified
     */
    public static boolean sanitizeBookContent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!SafetyConfig.ENABLE_NBT_SANITIZER.get()) {
            return false;
        }

        int maxPages = SafetyConfig.MAX_BOOK_PAGES.get();
        boolean modified = false;

        // Check written book
        if (stack.is(Items.WRITTEN_BOOK)) {
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null && content.pages().size() > maxPages) {
                if (SafetyConfig.STRIP_EXCESSIVE_BOOK_DATA.get()) {
                    // Strip excessive pages by removing the component
                    stack.remove(DataComponents.WRITTEN_BOOK_CONTENT);
                    MCOPT.LOGGER.warn("Stripped excessive book data: {} pages (max: {})",
                            content.pages().size(), maxPages);
                    modified = true;
                } else {
                    MCOPT.LOGGER.warn("Book has excessive pages: {} (max: {})",
                            content.pages().size(), maxPages);
                }
            }
        }

        // Check writable book (book and quill)
        if (stack.is(Items.WRITABLE_BOOK)) {
            WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content != null && content.pages().size() > maxPages) {
                if (SafetyConfig.STRIP_EXCESSIVE_BOOK_DATA.get()) {
                    stack.remove(DataComponents.WRITABLE_BOOK_CONTENT);
                    MCOPT.LOGGER.warn("Stripped excessive writable book data: {} pages (max: {})",
                            content.pages().size(), maxPages);
                    modified = true;
                } else {
                    MCOPT.LOGGER.warn("Writable book has excessive pages: {} (max: {})",
                            content.pages().size(), maxPages);
                }
            }
        }

        return modified;
    }

    /**
     * Performs full sanitization on an ItemStack.
     * Combines all sanitization methods.
     *
     * @param stack The ItemStack to sanitize
     * @return true if the stack was modified
     */
    public static boolean sanitize(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        boolean modified = false;

        // Sanitize empty custom data
        if (sanitizeEmptyTag(stack)) {
            modified = true;
        }

        // Sanitize book content if enabled
        if (SafetyConfig.ENABLE_NBT_SANITIZER.get()) {
            if (sanitizeBookContent(stack)) {
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Checks if the item is blacklisted from sanitization.
     *
     * @param stack The ItemStack to check
     * @return true if blacklisted
     */
    private static boolean isBlacklisted(ItemStack stack) {
        try {
            Item item = Objects.requireNonNull(stack.getItem(), "Item cannot be null");
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            String itemIdStr = itemId.toString();

            return GameplayConfig.ITEM_NBT_SANITIZER_BLACKLIST.get().contains(itemIdStr);
        } catch (Exception e) {
            // Safe fallback
            return false;
        }
    }
}
