package com.randomstrangerpassenger.mcopt.fishing;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;

/**
 * 안정적인 낚싯대 사용을 위해 플레이어와 낚싯찌의 상태를 주기적으로 점검합니다.
 * <p>
 * 차원 이동, 핫바 교체, 서버 지연 등으로 낚싯찌 엔티티가 분리되면 이후 모든 입력이
 * 무시되는 문제가 발생할 수 있습니다. 이 핸들러는 그런 고아 상태의 낚싯찌를 감지해
 * 안전하게 정리함으로써 재투척이 즉시 가능하도록 만듭니다.
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class FishingRodFixHandler {

    private FishingRodFixHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (!MCOPTConfig.ENABLE_FISHING_ROD_FIX.get()) {
            return;
        }

        FishingHook hook = player.fishing;
        if (hook == null) {
            return; // 정상 상태
        }

        if (shouldForceStop(player, hook)) {
            discardOrphanedHook(player, hook);
        }
    }

    private static boolean shouldForceStop(ServerPlayer player, FishingHook hook) {
        if (!hook.isAlive() || hook.isRemoved()) {
            return true;
        }

        if (hook.level() != player.level()) {
            return true; // 차원이 다르면 즉시 정리
        }

        if (hook.getOwner() != player) {
            return true; // 다른 엔티티가 소유한 상태로 뒤바뀜
        }

        if (!isHoldingFishingRod(player)) {
            return true; // 낚싯대를 치웠다면 줄을 걷어야 함
        }

        // 낚싯찌가 불러오지 않은 청크에 남아 있으면 조작이 불가능해지므로 안전하게 제거
        if (!hook.level().hasChunkAt(hook.blockPosition())) {
            return true;
        }

        // 바닐라와 동일한 최대 거리 기준을 한 번 더 확인해 끊어진 줄을 처리
        return hook.distanceToSqr(player) > 1024.0D;
    }

    private static boolean isHoldingFishingRod(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(Items.FISHING_ROD) || off.is(Items.FISHING_ROD);
    }

    private static void discardOrphanedHook(ServerPlayer player, FishingHook hook) {
        hook.discard();
        player.fishing = null;
        player.stopUsingItem();
    }
}
