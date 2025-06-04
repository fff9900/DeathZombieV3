package CustomZombie;

import org.bukkit.entity.Player;

/**
 * CustomZombie 插件的API类，供其他插件调用生成僵尸或实体。
 */
public class CustomZombieAPI {

    /**
     * 生成自定义僵尸
     *
     * @param player   生成僵尸的玩家
     * @param zombieId 僵尸的ID（如id1, id2, ..., id25）
     */
    public static void spawnCustomZombie(Player player, String zombieId) {
        CustomZombie.getInstance().spawnCustomZombie(player, zombieId);
    }

    /**
     * 生成自定义实体
     *
     * @param player  生成实体的玩家
     * @param otherId 实体的ID（如idc1, idc2, ..., idc6）
     */
    public static void spawnOtherEntity(Player player, String otherId) {
        CustomZombie.getInstance().spawnOtherEntity(player, otherId);
    }
}
