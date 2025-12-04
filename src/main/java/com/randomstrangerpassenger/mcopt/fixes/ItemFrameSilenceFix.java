package com.randomstrangerpassenger.mcopt.fixes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * 아이템 액자(ItemFrame) 및 그림(Painting) 로딩 시 불필요한 소리 방지 유틸리티.
 * <p>
 * 청크 로딩이나 월드 생성 시 엔티티가 로드될 때 playPlacementSound가 호출되면
 * 불필요한 소리가 재생되어 거슬릴 수 있습니다.
 * <p>
 * <b>문제</b>: 청크 로딩 시 아이템 액자/그림이 로드될 때마다 배치 소리가 재생됨
 * <p>
 * <b>해결</b>: 엔티티가 처음 로드되는 상황(tickCount == 0 또는 firstTick)에서는 소리를 음소거
 *
 * @see com.randomstrangerpassenger.mcopt.config.GameplayConfig#ENABLE_ITEM_FRAME_SILENCE
 */
public class ItemFrameSilenceFix {

    private ItemFrameSilenceFix() {
        // Utility class
    }

    /**
     * 배치 소리를 재생해야 하는지 확인합니다.
     * <p>
     * 소리를 음소거해야 하는 조건:
     * <ul>
     * <li>클라이언트 측에서 실행 중일 때 (서버에서는 소리가 재생되지 않음)</li>
     * <li>엔티티의 tickCount가 0일 때 (처음 로드된 상태)</li>
     * <li>엔티티가 firstTick 상태일 때 (아직 첫 틱을 경과하지 않음)</li>
     * </ul>
     * <p>
     * 플레이어가 직접 아이템 액자를 설치할 때는 이미 tickCount가 증가했거나
     * firstTick이 false이므로 정상적으로 소리가 재생됩니다.
     *
     * @param entity 소리 재생 여부를 확인할 엔티티
     * @return true면 소리를 재생해야 함, false면 소리를 음소거해야 함
     */
    public static boolean shouldPlaySound(Entity entity) {
        if (entity == null) {
            return true;
        }

        Level level = entity.level();

        // 서버 측에서는 소리가 재생되지 않으므로 체크 불필요
        if (!level.isClientSide()) {
            return true;
        }

        // tickCount가 0이면 엔티티가 방금 로드된 상태
        // 이 경우 청크 로딩/월드 생성으로 인한 로드이므로 소리를 음소거
        if (entity.tickCount == 0) {
            return false;
        }

        // firstTick이 true면 아직 첫 틱을 완료하지 않은 상태
        // 엔티티가 처음 월드에 추가되었을 때를 의미
        if (entity.firstTick) {
            return false;
        }

        // 그 외의 경우는 플레이어가 직접 설치한 것으로 판단하여 소리 재생
        return true;
    }

    /**
     * 엔티티가 처음 로드된 상태인지 확인합니다.
     * <p>
     * 이 메서드는 디버깅 및 로깅 목적으로 사용됩니다.
     *
     * @param entity 확인할 엔티티
     * @return true면 처음 로드된 상태
     */
    public static boolean isFirstLoad(Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity.tickCount == 0 || entity.firstTick;
    }
}
