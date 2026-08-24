package enduraut.shulkerboxdrop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * 玩家退出监听器
 * - 玩家退出时清理其计数数据
 */
public record PlayerQuitListener(BlockBreakListener blockBreakListener) implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        blockBreakListener.clearPlayerData(playerUUID);
    }
}
