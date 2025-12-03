package com.randomstrangerpassenger.mcopt.fixes;

import net.minecraft.world.entity.player.Player;

/**
 * 피격 시 카메라 틸트 방향을 수정하는 유틸리티.
 * <p>
 * 서버에서 보낸 데미지 방향(Yaw)이 유효하지 않을 때 올바른 방향을 계산합니다.
 * <p>
 * <b>문제</b>: 서버 데미지 패킷의 Yaw가 0이거나 NaN이면 카메라 틸트가 잘못된 방향으로 발생
 * <p>
 * <b>해결</b>: 유효하지 않은 Yaw를 감지하고 랜덤 또는 계산된 방향으로 대체
 *
 * @see com.randomstrangerpassenger.mcopt.config.GameplayConfig
 */
public class DamageTiltFix {

    private DamageTiltFix() {
        // Utility class
    }

    /**
     * Yaw 값이 유효한지 확인합니다.
     * <p>
     * 유효하지 않은 Yaw:
     * <ul>
     * <li>NaN (Not a Number)</li>
     * <li>0.0 (방향 정보 없음)</li>
     * </ul>
     *
     * @param yaw 검사할 Yaw 값
     * @return 유효하면 true, 유효하지 않으면 false
     */
    public static boolean isValidYaw(float yaw) {
        return !Float.isNaN(yaw) && yaw != 0.0f;
    }

    /**
     * 데미지 소스 위치에서 플레이어로의 방향(Yaw)을 계산합니다.
     * <p>
     * 주로 폭발 데미지와 같이 방향 정보가 없는 경우에 사용됩니다.
     *
     * @param player  피격된 플레이어
     * @param sourceX 데미지 소스 X 좌표
     * @param sourceZ 데미지 소스 Z 좌표
     * @return 계산된 Yaw (-180 ~ 180 도)
     */
    public static float calculateYawFromSource(Player player, double sourceX, double sourceZ) {
        double deltaX = sourceX - player.getX();
        double deltaZ = sourceZ - player.getZ();

        // atan2로 라디안 각도 계산
        double angleRad = Math.atan2(deltaZ, deltaX);

        // Minecraft Yaw 형식으로 변환 (도 단위)
        float yaw = (float) Math.toDegrees(angleRad) - 90.0f;

        // -180 ~ 180 범위로 정규화
        while (yaw < -180.0f) {
            yaw += 360.0f;
        }
        while (yaw > 180.0f) {
            yaw -= 360.0f;
        }

        return yaw;
    }

    /**
     * 랜덤 Yaw 값을 생성합니다 (fallback용).
     * <p>
     * 데미지 소스 위치를 알 수 없을 때 사용됩니다.
     *
     * @return -180 ~ 180 범위의 랜덤 Yaw
     */
    public static float getRandomYaw() {
        return (float) (Math.random() * 360.0 - 180.0);
    }
}
