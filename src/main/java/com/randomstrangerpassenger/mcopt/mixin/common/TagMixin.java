package com.randomstrangerpassenger.mcopt.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.Reference.class)
public abstract class TagMixin<T> {

    @Shadow
    public abstract boolean is(TagKey<T> tag);

    // Injecting into 'is' might cause recursion if not careful,
    // because TagLookupCache.isIn() might call holder.is() -> fallback.

    // TagLookupCache needs to call the *original* logic.
    // If we Redirect or Inject/Cancel, we need access to original.

    // BUT: Holder.Reference.is(TagKey) logic is:
    // return this.tags.contains(tag);

    // Tag caching is most useful for avoiding set lookups (O(1) but still hash
    // overhead?)
    // Actually, Holder.is() is extremely fast (HashSet contains).
    // The overhead comes from *finding* the holder or *iterating* tags.

    // TagLookupCache might be more useful for caching *complex* tag logic or if
    // usage patterns are specific.
    // Given Holder.Reference implementation is fast, this mixin might be premature
    // optimization
    // unless targeting specific heavy tag checks.

    // Let's hold off on injecting this for now to avoid stack overflow or
    // instability.
    // Just having the Cache class ready for specific optimizations in other places
    // is enough.
}
