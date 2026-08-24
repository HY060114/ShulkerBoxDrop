package enduraut.shulkerboxdrop;

import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 方块破坏监听器，插件核心逻辑所在。
 *
 * <p>统计每个玩家连续破坏潜影盒的数量，达到阈值后复制该潜影盒
 * （连同其内部物品）并在原地自然掉落。玩家退出或切换世界时，
 * 对应计数会由 {@link PlayerQuitListener} 与 {@link PlayerWorldChangeListener} 清理。</p>
 */
public class BlockBreakListener implements Listener {
    private final ShulkerBoxDrop plugin;
    /** 按玩家 UUID 记录的连续破坏潜影盒计数 */
    private final Map<UUID, Integer> shulkerBoxCount = new HashMap<>();

    /** @param plugin 插件主类实例，用于读取配置与禁用玩家状态 */
    public BlockBreakListener(ShulkerBoxDrop plugin) {
        this.plugin = plugin;
    }

    /**
     * 破坏方块事件入口。
     *
     * <p>仅在插件启用且被破坏方块为潜影盒时进入复制流程。</p>
     *
     * @param event 方块破坏事件
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!ShulkerBoxDrop.isEnabledPlugin()) return;
        if (event.getBlock().getType().name().endsWith("SHULKER_BOX")) {
            dropShulkerBoxes(event);
        }
    }

    /**
     * 累加玩家计数，并在达到阈值时复制潜影盒并掉落。
     *
     * @param event 方块破坏事件
     */
    private void dropShulkerBoxes(BlockBreakEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();

        // 检查玩家是否被禁用复制权限
        if (plugin.isDisabledPlayer(playerUUID)) return;

        int count = shulkerBoxCount.getOrDefault(playerUUID, 0);
        count++;

        if (count >= plugin.getThreshold()) {
            BlockState blockState = event.getBlock().getState();
            if (blockState instanceof ShulkerBox shulkerBox) {
                Inventory inventory = shulkerBox.getInventory();

                // 检查物品是否符合白名单逻辑
                if (!checkShulkerBoxContents(inventory)) {
                    shulkerBoxCount.put(playerUUID, 0);
                    return;
                }

                // 通过 BlockStateMeta 将潜影盒内容物复制到新的物品堆
                ItemStack itemStack = new ItemStack(event.getBlock().getType(), 1);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta instanceof BlockStateMeta blockStateMeta) {
                    BlockState blockStateCopy = blockStateMeta.getBlockState();
                    if (blockStateCopy instanceof ShulkerBox shulkerBoxCopy) {
                        shulkerBoxCopy.getInventory().setContents(inventory.getContents());
                        blockStateMeta.setBlockState(shulkerBoxCopy);
                        itemStack.setItemMeta(blockStateMeta);
                    }
                }

                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemStack);
            }
            count = 0;
        }
        shulkerBoxCount.put(playerUUID, count);
    }

    /**
     * 校验潜影盒内容是否符合复制条件。
     *
     * <p>白名单模式下，盒内所有物品都必须在白名单中；否则直接放行。</p>
     *
     * @param inventory 潜影盒的物品栏
     * @return 是否允许复制
     */
    private boolean checkShulkerBoxContents(Inventory inventory) {
        List<String> allowed = plugin.getAllowedItems();
        boolean whitelistMode = plugin.isWhitelistMode();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            String id = item.getType().getKey().toString(); // 例如 minecraft:oak_planks
            if (whitelistMode) {
                if (!allowed.contains(id)) return false;
            }
        }
        return true;
    }

    /**
     * 清除指定玩家的破坏计数。
     *
     * @param playerUUID 玩家 UUID
     */
    public void clearPlayerData(UUID playerUUID) {
        shulkerBoxCount.remove(playerUUID);
    }
}
