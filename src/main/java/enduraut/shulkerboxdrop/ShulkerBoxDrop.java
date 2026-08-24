package enduraut.shulkerboxdrop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class ShulkerBoxDrop extends JavaPlugin {
    private static boolean enabled = true; // 插件启用状态
    private final Set<UUID> disabledPlayers = new HashSet<>();
    private boolean whitelistMode;
    private List<String> allowedItems;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        whitelistMode = getConfig().getBoolean("whitelist-mode", false);
        allowedItems = getConfig().getStringList("allowed-items");

        for (String uuidStr : getConfig().getStringList("disabled-players")) {
            try {
                disabledPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        BlockBreakListener blockBreakListener = new BlockBreakListener(this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(blockBreakListener), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(blockBreakListener), this);

        CommandHandler handler = new CommandHandler(this);
        Objects.requireNonNull(getCommand("shulkerboxdrop")).setExecutor(handler);
        Objects.requireNonNull(getCommand("shulkerboxdrop")).setTabCompleter(handler);

        Bukkit.getConsoleSender().sendMessage("[ShulkerBoxDrop] §a插件已启用");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[ShulkerBoxDrop] §c插件已禁用");
    }

    public static boolean isEnabledPlugin() {
        return enabled;
    }

    public static void setEnabledPlugin(boolean state) {
        enabled = state;
    }

    public int getThreshold() {
        return getConfig().getInt("shulkerbox-break-threshold", 10);
    }

    public void setThreshold(int value) {
        getConfig().set("shulkerbox-break-threshold", value);
        saveConfig();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        whitelistMode = getConfig().getBoolean("whitelist-mode", false);
        allowedItems = getConfig().getStringList("allowed-items");

        disabledPlayers.clear();
        for (String uuidStr : getConfig().getStringList("disabled-players")) {
            try {
                disabledPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // 玩家禁用复制权限
    public void addDisabledPlayer(UUID uuid) {
        disabledPlayers.add(uuid);
        saveDisabledPlayers();
    }

    public void removeDisabledPlayer(UUID uuid) {
        disabledPlayers.remove(uuid);
        saveDisabledPlayers();
    }

    public boolean isDisabledPlayer(UUID uuid) {
        return disabledPlayers.contains(uuid);
    }

    public int getDisabledPlayerCount() {
        return disabledPlayers.size();
    }

    private void saveDisabledPlayers() {
        List<String> uuidStrings = new ArrayList<>();
        for (UUID uuid : disabledPlayers) {
            uuidStrings.add(uuid.toString());
        }
        getConfig().set("disabled-players", uuidStrings);
        saveConfig();
    }

    // 白名单模式
    public boolean isWhitelistMode() {
        return whitelistMode;
    }

    public void setWhitelistMode(boolean mode) {
        whitelistMode = mode;
        getConfig().set("whitelist-mode", mode);
        saveConfig();
    }

    // 允许物品列表
    public List<String> getAllowedItems() {
        return allowedItems;
    }

    public void addAllowedItem(String itemId) {
        allowedItems.add(itemId);
        getConfig().set("allowed-items", allowedItems);
        saveConfig();
    }

    public boolean removeAllowedItem(String itemId) {
        boolean removed = allowedItems.remove(itemId);
        if (removed) {
            getConfig().set("allowed-items", allowedItems);
            saveConfig();
        }
        return removed;
    }
}
