package com.flyaway.smartmobs;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

public class SmartMobs extends JavaPlugin {

    private static SmartMobs instance;
    private MobManager mobManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Синхронная инициализация (без гонок)
        this.configManager = new ConfigManager();
        this.configManager.loadConfig();

        this.mobManager = new MobManager();

        registerEvents();
        registerCommands();

        getLogger().info("SmartMobs v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Hardened chance: " + configManager.getHardenedChance());
        getLogger().info("Elite chance: " + configManager.getEliteChance());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        getLogger().info("SmartMobs disabled!");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new MobSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(), this);
    }

    private void registerCommands() {
        var command = getCommand("smartmobs");
        if (command != null) {
            SmartMobsCommand executor = new SmartMobsCommand(this, mobManager, configManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    public static SmartMobs getInstance() {
        return instance;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
