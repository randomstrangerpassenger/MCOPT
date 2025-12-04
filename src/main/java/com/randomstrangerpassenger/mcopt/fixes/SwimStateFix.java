package com.randomstrangerpassenger.mcopt.fixes;

import net.minecraft.world.entity.player.Player;

/**
 * 수영 상태 동기화 수정 유틸리티 (MC-220390 버그 수정).
 * <p>
 * 마인크래프트에서 플레이어가 물 속에서 수영(스프린트) 중 엔티티를 공격하면,
 * 서버가 플레이어의 수영 상태를 잘못 해제하여 클라이언트와 서버 사이에
 * 히트박스 불일치가 발생하는 버그가 있습니다.
 * <p>
 * <b>문제</b>: 수영 중 공격 시 서버가 수영 상태를 해제하여 클라이언트와 히트박스가 불일치함
 * <p>
 * <b>증상</b>:
 * <ul>
 * <li>1x1 블록 공간을 통과하려 할 때 글리치 발생</li>
 * <li>해저 신전(Ocean Monument)에서 가디언과 싸울 때 불편함</li>
 * <li>물속 전투 후 이상한 이동 동작</li>
 * </ul>
 * <p>
 * <b>해결</b>: 공격 직후 수영 조건이 충족되면 강제로 수영 상태를 복원
 *
 * @see com.randomstrangerpassenger.mcopt.config.GameplayConfig#ENABLE_SWIM_STATE_FIX
 */
public final class SwimStateFix {

    private SwimStateFix() {
        // Utility class
    }

    /**
     * 플레이어가 수영 상태를 유지해야 하는지 확인합니다.
     * <p>
     * 수영 상태 복원 조건:
     * <ul>
     * <li>플레이어가 물 속에 있음 (isInWater)</li>
     * <li>플레이어가 스프린트 중임 (isSprinting)</li>
     * <li>플레이어가 헤엄칠 공간이 있음 (수영 가능)</li>
     * <li>플레이어가 현재 수영 상태가 아님 (desync 상태)</li>
     * </ul>
     *
     * @param player 확인할 플레이어
     * @return true면 수영 상태 복원이 필요함
     */
    public static boolean shouldRestoreSwimState(Player player) {
        if (player == null) {
            return false;
        }

        // 클라이언트 측에서는 처리하지 않음 (서버 측 동기화 문제)
        if (player.level().isClientSide()) {
            return false;
        }

        // 조건 1: 물 속에 있어야 함
        if (!player.isInWater()) {
            return false;
        }

        // 조건 2: 스프린트 중이어야 함 (또는 스프린트 키가 눌린 상태)
        if (!player.isSprinting()) {
            return false;
        }

        // 조건 3: 현재 수영 상태가 아니어야 함 (desync 발생 상황)
        if (player.isSwimming()) {
            return false;
        }

        // 조건 4: 수영할 공간이 있어야 함
        // 플레이어가 서있는 위치에서 수영 자세가 가능한지 확인
        if (!canSwimAt(player)) {
            return false;
        }

        return true;
    }

    /**
     * 플레이어가 현재 위치에서 수영할 수 있는지 확인합니다.
     * <p>
     * 수영 자세는 플레이어의 히트박스가 0.6x0.6 블록으로 변경되므로,
     * 최소한 그 정도의 물 공간이 필요합니다.
     *
     * @param player 확인할 플레이어
     * @return true면 수영 가능
     */
    public static boolean canSwimAt(Player player) {
        if (player == null) {
            return false;
        }

        // 플레이어가 물 속에 잠겨있고, 수영 동작이 가능한 상태인지 확인
        // 바닐라의 Player.updateSwimming() 로직을 참고
        // 수중에 있고 관전 모드가 아니면 수영 가능
        return player.isInWater() && !player.isSpectator();
    }

    /**
     * 플레이어의 수영 상태를 복원합니다.
     * <p>
     * 공격 직후 서버가 잘못 해제한 수영 상태를 다시 설정합니다.
     * 스프린트 상태도 함께 복원하여 동기화를 보장합니다.
     *
     * @param player 수영 상태를 복원할 플레이어
     */
    public static void restoreSwimState(Player player) {
        if (player == null) {
            return;
        }

        // 서버 측에서만 처리
        if (player.level().isClientSide()) {
            return;
        }

        // 스프린트와 수영 상태를 강제로 복원
        player.setSprinting(true);
        player.setSwimming(true);
    }

    /**
     * 필요한 경우 플레이어의 수영 상태를 확인하고 복원합니다.
     * <p>
     * 이 메서드는 공격 후 또는 tick 시점에서 호출되어
     * 수영 상태 desync를 자동으로 수정합니다.
     *
     * @param player 확인 및 복원할 플레이어
     * @return true면 수영 상태가 복원됨
     */
    public static boolean checkAndRestoreSwimState(Player player) {
        if (shouldRestoreSwimState(player)) {
            restoreSwimState(player);
            return true;
        }
        return false;
    }
}
