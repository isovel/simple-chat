package ch.isota.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChat extends JavaPlugin implements Listener {
    private DataManager config;
    private DataManager nicknameData;
    private String configuredPluginTag;
    private String configuredChatFormat;

    @Override
    public void onEnable() {
        try {
            loadConfig();
            Bukkit.getPluginManager().registerEvents(this, this);
            getLogger().info("Loading complete!");
        } catch (Exception e) {
            getLogger().severe("Error while loading. Printing stack trace!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("screload")) {
            reloadConfig();
            sender.sendMessage(configuredPluginTag + ChatColor.GREEN + "Configuration reloaded!");
            return true;
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage(configuredPluginTag + ChatColor.RED + "This command must be executed as a player!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("nick")) {
            if (args.length == 0) {
                p.sendMessage(configuredPluginTag + ChatColor.RED + "Missing argument: <nickname>");
                return true;
            }

            if (args[0].equalsIgnoreCase("off")) {
                p.sendMessage(configuredPluginTag + ChatColor.GREEN + "Display name reset!");
                this.nicknameData.getConfig().set(p.getUniqueId().toString(), null);
                this.nicknameData.saveConfig();
                return true;
            }

            String nick = "";
            for (String arg : args) {
                nick += arg + " ";
            }
            nick = nick.substring(0, nick.length() - 1);

            p.sendMessage(configuredPluginTag + ChatColor.GREEN + "Display name changed to: " + ChatColor.GOLD + "\"" + nick + "\"");

            nick = parseChatFormatting(nick);

            this.nicknameData.getConfig().set(p.getUniqueId().toString(), nick);
            this.nicknameData.saveConfig();
        }
        return true;
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        e.setFormat(configuredChatFormat);
        e.setMessage(parseChatFormatting(e.getMessage()));
        if (this.nicknameData.getConfig().getString(e.getPlayer().getUniqueId().toString()) != null) {
            e.getPlayer().setDisplayName(this.nicknameData.getConfig().getString(e.getPlayer().getUniqueId().toString()) + ChatColor.RESET);
        } else {
            e.getPlayer().setDisplayName(e.getPlayer().getName());
        }
    }

    public void loadConfig() {
        this.config = new DataManager(this, "config.yml");
        this.nicknameData = new DataManager(this, "nicknames.yml");
        this.configuredPluginTag = parseChatFormatting(this.config.getConfig().getString("pluginTag"));
        this.configuredChatFormat = parseConfiguredChatFormat(this.config.getConfig().getString("chatFormat"));
    }

    public void reloadConfig() {
        loadConfig();
    }

    public String parseChatFormatting(String message) {
        char[] formatCodes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o'};
        for (char code : formatCodes) {
            message = message.replaceAll("&" + code, "§" + code);
        }
        return message;
    }

    public String parseConfiguredChatFormat(String configuredFormat) {
        if (!(configuredFormat.contains("{USERNAME}") || configuredFormat.contains("{DISPLAYNAME}")) || !configuredFormat.contains("{MESSAGE}") || (configuredFormat.contains("{USERNAME}") && configuredFormat.contains("{DISPLAYNAME}"))) {
            return "<%1$s> %2$s";
        }
        configuredFormat = configuredFormat.replace("{USERNAME}", "%1$s");
        configuredFormat = configuredFormat.replace("{DISPLAYNAME}", "%1$s");
        configuredFormat = configuredFormat.replace("{MESSAGE}", "%2$s");
        configuredFormat = parseChatFormatting(configuredFormat);
        return configuredFormat;
    }
}
