package com.flyaway.smartmobs;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class SmartMobsCommand implements CommandExecutor, TabCompleter {

    private final SmartMobs plugin;
    private final MobManager mobManager;
    private final ConfigManager configManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SmartMobsCommand(SmartMobs plugin, MobManager mobManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("smartmobs.use")) {
            sender.sendMessage(mm.deserialize("<red>❌ У вас нет прав на использование этой команды."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<yellow>Использование: /smartmobs spawn <mob> <hardened|elite>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> {
                if (!sender.hasPermission("smartmobs.spawn")) {
                    sender.sendMessage(mm.deserialize("<red>❌ У вас нет прав на спавн мобов."));
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>❌ Только игрок может спавнить мобов."));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>❌ Использование: /smartmobs spawn <mob> <hardened|elite>"));
                    return true;
                }

                String mobTypeName = args[1].toLowerCase();
                String variant = args[2].toLowerCase();

                if (!configManager.isMobEnabled(mobTypeName)) {
                    sender.sendMessage(mm.deserialize("<red>❌ Моб <white>" + mobTypeName + "</white> отключён в конфиге."));
                    return true;
                }

                EntityType type;
                try {
                    type = EntityType.valueOf(mobTypeName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(mm.deserialize("<red>❌ Неизвестный тип моба: <white>" + mobTypeName));
                    return true;
                }

                Location loc = player.getLocation();
                Entity spawned = player.getWorld().spawnEntity(loc, type);

                if (!(spawned instanceof LivingEntity living)) {
                    sender.sendMessage(mm.deserialize("<red>❌ Этот тип не является живым существом."));
                    spawned.remove();
                    return true;
                }

                switch (variant) {
                    case "hardened" -> mobManager.makeHardened(living);
                    case "elite" -> mobManager.makeElite(living);
                    default -> {
                        sender.sendMessage(mm.deserialize("<red>❌ Неизвестный вариант: <white>" + variant));
                        spawned.remove();
                        return true;
                    }
                }

                sender.sendMessage(mm.deserialize("<green>✔ Заспавнен <yellow>" + variant + "</yellow> <gray>" + type.name().toLowerCase() + "</gray>."));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("smartmobs.reload")) {
                    sender.sendMessage(mm.deserialize("<red>❌ У вас нет прав на перезагрузку плагина."));
                    return true;
                }
                // логика перезагрузки конфига
                configManager.reloadConfig();
                sender.sendMessage(mm.deserialize("<green>✔ Конфиг SmartMobs перезагружен."));
                return true;
            }
            default -> sender.sendMessage(mm.deserialize("<red>❌ Неизвестная подкоманда."));
        }

        return true;
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
            // Показываем только мобов, включённых в конфиге
            return configManager.getEnabledMobTypes();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("spawn") && sender.hasPermission("smartmobs.spawn")) {
            return Arrays.asList("hardened", "elite");
        }

        return List.of();
    }
}
