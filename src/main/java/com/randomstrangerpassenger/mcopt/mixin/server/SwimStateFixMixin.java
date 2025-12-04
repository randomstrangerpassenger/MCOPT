package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.fixes.SwimStateFix;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 수영 중 공격 시 발생하는 수영 상태 동기화 버그를 수정합니다 (MC-220390).
 * <p>
 * 마인크래프트에서 플레이어가 물 속에서 수영(스프린트) 중 엔티티를 공격하면,
 * 서버가 플레이어의 수영 상태를 잘못 해제하여 클라이언트와 서버 사이에
 * 히트박스 불일치가 발생합니다.
 * <p>
 * 이 Mixin은 공격 후 수영 조건이 충족되면 강제로 수영 상태를 복원합니다.
 *
 * @see SwimStateFix
 * @see GameplayConfig#ENABLE_SWIM_STATE_FIX
 */
@Mixin(Player.class)
public abstract class SwimStateFixMixin {

    /**
     * 플레이어가 엔티티를 공격한 직후 수영 상태를 확인하고 복원합니다.
     * <p>
     * 공격 로직이 완료된 후(TAIL)에 실행되어 서버가 잘못 해제한
     * 수영 상태를 다시 설정합니다.
     *
     * @param target 공격 대상 엔티티
     * @param ci     콜백 정보
     */
    @Inject(method = "attack", at = @At("TAIL"))
    private void mcopt$restoreSwimStateAfterAttack(Entity target, CallbackInfo ci) {
        if (!GameplayConfig.ENABLE_SWIM_STATE_FIX.get()) {
            return;
        }

        Player self = (Player) (Object) this;

        // 서버 측에서만 처리
        if (self.level().isClientSide()) {
            return;
        }

        // 수영 상태 확인 및 복원
        SwimStateFix.checkAndRestoreSwimState(self);
    }
}
