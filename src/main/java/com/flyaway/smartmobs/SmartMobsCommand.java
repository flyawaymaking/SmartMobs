package com.flyaway.smartmobs;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class SmartMobsCommand implements CommandExecutor, TabCompleter {

    private final MobManager mobManager;
    private final ConfigManager configManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SmartMobsCommand(SmartMobs plugin, MobManager mobManager, ConfigManager configManager) {
        this.mobManager = mobManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("smartmobs.use")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "spawn-usage");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> {
                if (!sender.hasPermission("smartmobs.spawn")) {
                    sendMessage(sender, "spawn-no-permission");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sendMessage(sender, "only-players");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(sender, "spawn-usage");
                    return true;
                }

                String mobTypeName = args[1].toLowerCase();
                String variant = args[2].toLowerCase();

                if (!configManager.isMobEnabled(mobTypeName)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("mob", mobTypeName);
                    sendMessage(sender, "spawn-mob-disabled", placeholders);
                    return true;
                }

                EntityType type;
                try {
                    type = EntityType.valueOf(mobTypeName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("mob", mobTypeName);
                    sendMessage(sender, "spawn-unknown-mob", placeholders);
                    return true;
                }

                Location loc = player.getLocation();
                Entity spawned = player.getWorld().spawnEntity(loc, type);

                if (!(spawned instanceof LivingEntity living)) {
                    sendMessage(sender, "spawn-not-living");
                    spawned.remove();
                    return true;
                }

                switch (variant) {
                    case "hardened" -> mobManager.makeHardened(living);
                    case "elite" -> mobManager.makeElite(living);
                    default -> {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("variant", variant);
                        sendMessage(sender, "spawn-unknown-variant", placeholders);
                        spawned.remove();
                        return true;
                    }
                }

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("variant", variant);
                placeholders.put("mob", type.name().toLowerCase());
                sendMessage(sender, "spawn-success", placeholders);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("smartmobs.reload")) {
                    sendMessage(sender, "reload-no-permission");
                    return true;
                }
                configManager.reloadConfig();
                sendMessage(sender, "reload-success");
                return true;
            }
            default -> sendMessage(sender, "unknown-subcommand");
        }

        return true;
    }

    private void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, Collections.emptyMap());
    }

    private void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = configManager.getMessage(messageKey);
        if (message != null && !message.isEmpty()) {
            // Заменяем плейсхолдеры {key} на значения из Map
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                message = message.replace(placeholder, entry.getValue());
            }
            sender.sendMessage(miniMessage.deserialize(message));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("smartmobs.spawn")) subcommands.add("spawn");
            if (sender.hasPermission("smartmobs.reload")) subcommands.add("reload");
            return subcommands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("spawn") && sender.hasPermission("smartmobs.spawn")) {
            return configManager.getEnabledMobTypes();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("spawn") && sender.hasPermission("smartmobs.spawn")) {
            return Arrays.asList("hardened", "elite");
        }

        return List.of();
    }
}
