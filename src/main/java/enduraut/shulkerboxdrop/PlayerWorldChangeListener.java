package enduraut.shulkerboxdrop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

/**
 * 玩家切换世界监听器
 * - 玩家切换世界时清理其计数数据
 */
public record PlayerWorldChangeListener(BlockBreakListener blockBreakListener) implements Listener {
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        blockBreakListener.clearPlayerData(playerUUID);
    }
}

