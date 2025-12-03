package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Improves potion effect level display for levels > 10.
 * <p>
 * <b>Problem</b>: Vanilla only provides Roman numeral translations up to level X (10).
 * Higher levels (11+) don't have translations and appear blank.
 * <p>
 * <b>Solution</b>: Dynamically generate Arabic numeral strings for missing
 * "potion.potency.X" translation keys.
 * <p>
 * <b>Example</b>:
 * <ul>
 * <li>"potion.potency.0" → "I" (vanilla)</li>
 * <li>"potion.potency.9" → "X" (vanilla)</li>
 * <li>"potion.potency.10" → "11" (MCOPT)</li>
 * <li>"potion.potency.127" → "128" (MCOPT)</li>
 * </ul>
 * <p>
 * <b>Config</b>: {@link SafetyConfig#ENABLE_POTION_LIMIT_FIX}
 */
@Mixin(Language.class)
public abstract class LanguageMixin {

    /**
     * Intercept translation lookups for potion potency levels.
     * When amplifier > 9 (level > 10) and no translation exists,
     * return Arabic numeral instead of the translation key.
     */
    @Inject(
            method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mcopt$improvePotionLevelDisplay(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        if (!SafetyConfig.ENABLE_POTION_LIMIT_FIX.get()) {
            return;
        }

        // Check if this is a potion potency translation key
        if (key != null && key.startsWith("potion.potency.")) {
            try {
                // Extract amplifier value from key (e.g., "potion.potency.10" → 10)
                String amplifierStr = key.substring("potion.potency.".length());
                int amplifier = Integer.parseInt(amplifierStr);

                // For amplifier > 9 (level > 10), return Arabic numeral
                // Amplifier 0 = Level I, Amplifier 9 = Level X, Amplifier 10 = Level 11
                if (amplifier > 9) {
                    cir.setReturnValue(String.valueOf(amplifier + 1));
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // Not a valid potency key, let vanilla handle it
            }
        }
    }
}
