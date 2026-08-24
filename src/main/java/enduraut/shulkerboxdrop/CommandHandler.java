package enduraut.shulkerboxdrop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final ShulkerBoxDrop plugin;

    private static final List<String> CATEGORIES = List.of(
            "plugin", "player", "item", "whitelist", "threshold", "status", "reload"
    );

    public CommandHandler(ShulkerBoxDrop plugin) {
        this.plugin = plugin;
    }

    // ── Command dispatch ──────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "plugin"    -> handlePlugin(sender, args);
            case "player"    -> handlePlayer(sender, args);
            case "item"      -> handleItem(sender, args);
            case "whitelist" -> handleWhitelist(sender, args);
            case "threshold" -> handleThreshold(sender, args);
            case "status"    -> showStatus(sender);
            case "reload"    -> reloadConfig(sender);
            default          -> sender.sendMessage("§c未知指令，使用 /sbd 查看帮助");
        }
        return true;
    }

    // ── Category handlers ─────────────────────────────────────────

    private void handlePlugin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§e用法: /sbd plugin on|off|reload");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "on"     -> { ShulkerBoxDrop.setEnabledPlugin(true);  sender.sendMessage("§a插件已开启"); }
            case "off"    -> { ShulkerBoxDrop.setEnabledPlugin(false); sender.sendMessage("§c插件已关闭"); }
            case "reload" -> reloadConfig(sender);
            default       -> sender.sendMessage("§e用法: /sbd plugin on|off|reload");
        }
    }

    private void handlePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shulkerboxdrop.admin")) {
            sender.sendMessage("§c你没有权限！");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§e用法: /sbd player disable|enable <玩家名>");
            return;
        }
        UUID uuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
        switch (args[1].toLowerCase()) {
            case "disable" -> {
                plugin.addDisabledPlayer(uuid);
                sender.sendMessage("§c已取消玩家 " + args[2] + " 的复制权限");
            }
            case "enable" -> {
                plugin.removeDisabledPlayer(uuid);
                sender.sendMessage("§a已恢复玩家 " + args[2] + " 的复制权限");
            }
            default -> sender.sendMessage("§e用法: /sbd player disable|enable <玩家名>");
        }
    }

    private void handleItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shulkerboxdrop.admin")) {
            sender.sendMessage("§c你没有权限！");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§e用法: /sbd item add|remove <物品id>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "add" -> {
                plugin.addAllowedItem(args[2]);
                sender.sendMessage("§a已将物品 " + args[2] + " 添加到白名单");
            }
            case "remove" -> {
                if (plugin.removeAllowedItem(args[2])) {
                    sender.sendMessage("§a已将物品 " + args[2] + " 从白名单移除");
                } else {
                    sender.sendMessage("§c物品 " + args[2] + " 不在白名单中");
                }
            }
            default -> sender.sendMessage("§e用法: /sbd item add|remove <物品id>");
        }
    }

    private void handleWhitelist(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shulkerboxdrop.admin")) {
            sender.sendMessage("§c你没有权限！");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§e用法: /sbd whitelist on|off");
            return;
        }
        boolean mode = args[1].equalsIgnoreCase("on");
        plugin.setWhitelistMode(mode);
        sender.sendMessage("§e白名单模式已设置为: " + (mode ? "开启" : "关闭"));
    }

    private void handleThreshold(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shulkerboxdrop.admin")) {
            sender.sendMessage("§c你没有权限！");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§e当前阈值: " + plugin.getThreshold());
            sender.sendMessage("§e修改: /sbd threshold <数字>");
            return;
        }
        try {
            int value = Integer.parseInt(args[1]);
            plugin.setThreshold(value);
            sender.sendMessage("§a阈值已设置为 " + value);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c请输入有效数字！");
        }
    }

    // ── Shared handlers ───────────────────────────────────────────

    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6══ 插件状态 ══");
        sender.sendMessage("§e运行状态: " + (ShulkerBoxDrop.isEnabledPlugin() ? "§a开启" : "§c关闭"));
        sender.sendMessage("§e白名单模式: " + (plugin.isWhitelistMode() ? "§a开启" : "§c关闭"));
        sender.sendMessage("§e挖掘阈值: " + plugin.getThreshold());
        sender.sendMessage("§e白名单物品 (" + plugin.getAllowedItems().size() + "): " + plugin.getAllowedItems());
        sender.sendMessage("§e禁用玩家数: " + plugin.getDisabledPlayerCount());
    }

    private void reloadConfig(CommandSender sender) {
        plugin.reloadPluginConfig();
        sender.sendMessage("§a配置已重载");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6══ ShulkerBoxDrop 帮助 ══");
        sender.sendMessage("§e/sbd plugin on|off|reload   §7— 插件开关 & 重载配置");
        sender.sendMessage("§e/sbd player disable|enable <玩家> §7— 禁用/恢复玩家复制权限");
        sender.sendMessage("§e/sbd item add|remove <物品id>    §7— 添加/移除白名单物品");
        sender.sendMessage("§e/sbd whitelist on|off            §7— 开关白名单模式");
        sender.sendMessage("§e/sbd threshold [<数字>]          §7— 查看/设置挖掘阈值");
        sender.sendMessage("§e/sbd status                      §7— 查看完整状态");
        sender.sendMessage("§e/sbd reload                      §7— 重载配置文件");
    }

    // ── Tab completion ─────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return filterPrefix(CATEGORIES, args[0]);
        }

        switch (args[0].toLowerCase()) {
            case "plugin" -> {
                if (args.length == 2) completions = filterPrefix(List.of("on", "off", "reload"), args[1]);
            }
            case "player" -> {
                if (args.length == 2) {
                    completions = filterPrefix(List.of("disable", "enable"), args[1]);
                } else if (args.length == 3) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String name = player.getName();
                        if (name.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                }
            }
            case "item" -> {
                if (args.length == 2) {
                    completions = filterPrefix(List.of("add", "remove"), args[1]);
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("add")) {
                        for (Material material : Material.values()) {
                            String key = material.getKey().toString();
                            if (key.startsWith(args[2].toLowerCase())) {
                                completions.add(key);
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        for (String item : plugin.getAllowedItems()) {
                            if (item.startsWith(args[2].toLowerCase())) {
                                completions.add(item);
                            }
                        }
                    }
                }
            }
            case "whitelist" -> {
                if (args.length == 2) completions = filterPrefix(List.of("on", "off"), args[1]);
            }
            case "threshold" -> {
                if (args.length == 2) completions.add("<数字>");
            }
        }

        return completions;
    }

    private static List<String> filterPrefix(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(lower)) result.add(opt);
        }
        return result;
    }
}
