package bianloveyue.shulkerboxdrop;


import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

// ShulkerBoxDrop 类继承自 JavaPlugin 类，实现了 Listener 接口
public final class ShulkerBoxDrop extends JavaPlugin implements Listener {

    // 定义静态常量PLUGIN_NAME，表示插件的名字
    private static final String PLUGIN_NAME = "[ShulkerBoxDrop]";

    // 定义插件状态枚举
    private enum PluginState {
        ENABLED,
        DISABLED
    }

    // 定义插件状态
    private PluginState pluginState;

    // 定义最大ShulkerBox破坏次数限制
    private static final int SHULKER_BOX_BREAK_LIMIT = 10;

    // 定义一个HashMap来存储每个玩家破坏ShulkerBox的次数
    private final HashMap<UUID, Integer> shulkerBoxCount;

    // ShulkerBoxDrop 构造函数
    public ShulkerBoxDrop() {
        // 初始化插件状态为启用
        this.pluginState = PluginState.ENABLED;
        // 初始化ShulkerBox破坏次数为0
        this.shulkerBoxCount = new HashMap<>();
    }

    // 插件启动时调用的方法
    @Override
    public void onEnable() {
        // 注册事件监听器
        this.getServer().getPluginManager().registerEvents(this, this);
        // 输出插件启动信息到控制台
        Bukkit.getConsoleSender().sendMessage(PLUGIN_NAME + "§a插件已启用");

    }

    // 插件关闭时调用的方法
    @Override
    public void onDisable() {
        // 输出插件关闭信息到控制台
        Bukkit.getConsoleSender().sendMessage(PLUGIN_NAME + "§c 插件已禁用");

    }

    // 当方块被破坏时调用的方法
    private void dropShulkerBoxes(BlockBreakEvent event) {
        // 获取破坏方块的玩家UUID
        UUID playerUUID = event.getPlayer().getUniqueId();
        // 获取玩家破坏ShulkerBox的次数
        int count = this.shulkerBoxCount.getOrDefault(playerUUID, 0);
        // 增加破坏次数
        count++;
        // 检查是否达到破坏次数限制
        if (count == SHULKER_BOX_BREAK_LIMIT) {
            // 禁止方块破坏后掉落物品
            event.setDropItems(false);
            // 获取被破坏的方块状态
            BlockState blockState = event.getBlock().getState();
            // 判断是否为ShulkerBox
            if (blockState instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockState;
                Inventory inventory = shulkerBox.getInventory();
                // 创建一个新的ItemStack对象，表示ShulkerBox类型的方块
                ItemStack itemStack = new ItemStack(event.getBlock().getType(), 2);
                // 获取ItemStack的ItemMeta对象
                ItemMeta itemMeta = itemStack.getItemMeta();
                // 判断ItemMeta是否为BlockStateMeta
                if (itemMeta instanceof BlockStateMeta) {
                    // 将BlockStateMeta转换为ShulkerBox类型的BlockState
                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    BlockState blockStateCopy = blockStateMeta.getBlockState();
                    // 判断blockStateCopy是否为ShulkerBox
                    if (blockStateCopy instanceof ShulkerBox) {
                        ShulkerBox shulkerBoxCopy = (ShulkerBox) blockStateCopy;
                        // 将ShulkerBox的内容复制到新的ShulkerBox中
                        shulkerBoxCopy.getInventory().setContents(inventory.getContents());
                        // 将新的ShulkerBox设置为ItemStack的ItemMeta
                        blockStateMeta.setBlockState(shulkerBoxCopy);
                        itemStack.setItemMeta(blockStateMeta);
                    }
                }
                // 将ItemStack掉落到方块被破坏的位置
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemStack);
            }
            // 重置破坏次数为0
            count = 0;
        }
        // 更新HashMap中的破坏次数
        this.shulkerBoxCount.put(playerUUID, count);
    }

    // 当方块被破坏事件发生时调用的方法
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // 检查插件状态是否为启用状态且被破坏的方块为方块类型
        if (this.pluginState == PluginState.ENABLED && event.getBlock().getType().isBlock()) {
            this.dropShulkerBoxes(event);
        }
    }

    // 当玩家退出游戏时调用的事件处理器
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 获取退出游戏的玩家UUID
        UUID playerUUID = event.getPlayer().getUniqueId();
        // 从HashMap中移除该玩家的条目
        shulkerBoxCount.remove(playerUUID);
    }

    // 当玩家更改世界维度时调用的事件处理器
// 当玩家更改世界维度时调用的事件处理器
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // 获取更改世界维度的玩家UUID
        UUID playerUUID = event.getPlayer().getUniqueId();
        // 从HashMap中移除该玩家的条目
        shulkerBoxCount.remove(playerUUID);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查命令名称是否为shulkerboxdrop
        if (command.getName().equalsIgnoreCase("shulkerboxdrop")) {
            // 判断发送者是否为玩家
            if (sender instanceof Player) {
                    // 切换插件状态
                    this.pluginState = this.pluginState == ShulkerBoxDrop.PluginState.ENABLED ? ShulkerBoxDrop.PluginState.DISABLED : ShulkerBoxDrop.PluginState.ENABLED;
                    // 发送消息显示插件状态
                    sender.sendMessage("[ShulkerBoxDrop] is now " + this.pluginState + ".");
                    sender.sendMessage(PLUGIN_NAME+"§a is now " + this.pluginState + ".");
                }
                return true;
            } else {
                return false;
            }
        }
}

