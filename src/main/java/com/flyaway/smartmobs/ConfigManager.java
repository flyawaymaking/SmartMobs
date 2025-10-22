package com.flyaway.smartmobs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private FileConfiguration config;
    private Map<EntityType, Boolean> enabledMobs = new HashMap<>();
    private Map<String, Map<EntityType, String>> displayNames = new HashMap<>();
    private Map<EntityType, Map<String, Object>> specialAbilities = new HashMap<>();

    // Вероятности
    private double hardenedChance = 0.15;
    private double eliteChance = 0.05;

    // Множители для hardened
    private double hardenedHpMultiplier = 1.25;
    private double hardenedDamageMultiplier = 1.25;
    private double hardenedKnockbackResistance = 0.5;
    private boolean hardenedNameVisible = true;
    private int hardenedShowDuration = 3;

    // Множители для elite
    private double eliteHpMultiplier = 1.5;
    private double eliteDamageMultiplier = 1.5;
    private double eliteSpeedMultiplier = 1.4;
    private double eliteKnockbackResistance = 0.8;
    private boolean eliteNameVisible = true;
    private int eliteShowDuration = 3;
    private boolean eliteStrengthEnabled = true;
    private int eliteStrengthLevel = 0;

    public void loadConfig() {
        SmartMobs plugin = SmartMobs.getInstance();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        loadChances();
        loadEnabledMobs();
        loadHardenedSettings();
        loadEliteSettings();
        loadDisplayNames();
        loadSpecialAbilities();
    }

    public void reloadConfig() {
        SmartMobs plugin = SmartMobs.getInstance();
        plugin.reloadConfig();
        loadConfig();
    }

    private void loadChances() {
        if (config == null) return;
        hardenedChance = config.getDouble("chances.hardened", hardenedChance);
        eliteChance = config.getDouble("chances.elite", eliteChance);
    }

    private void loadEnabledMobs() {
        enabledMobs.clear();
        if (config == null) return;
        if (config.getConfigurationSection("enabled-mobs") == null) return;

        for (String mobKey : config.getConfigurationSection("enabled-mobs").getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                boolean enabled = config.getBoolean("enabled-mobs." + mobKey, true);
                enabledMobs.put(type, enabled);
            } catch (IllegalArgumentException e) {
                SmartMobs.getInstance().getLogger().warning("Unknown entity type in config: " + mobKey);
            }
        }
    }

    private void loadHardenedSettings() {
        if (config == null) return;
        hardenedHpMultiplier = config.getDouble("hardened.hp-multiplier", hardenedHpMultiplier);
        hardenedDamageMultiplier = config.getDouble("hardened.damage-multiplier", hardenedDamageMultiplier);
        hardenedKnockbackResistance = config.getDouble("hardened.knockback-resistance", hardenedKnockbackResistance);
        hardenedNameVisible = config.getBoolean("hardened.name-visible", hardenedNameVisible);
        hardenedShowDuration = config.getInt("hardened.show-duration", hardenedShowDuration);
    }

    private void loadEliteSettings() {
        if (config == null) return;
        eliteHpMultiplier = config.getDouble("elite.hp-multiplier", eliteHpMultiplier);
        eliteDamageMultiplier = config.getDouble("elite.damage-multiplier", eliteDamageMultiplier);
        eliteSpeedMultiplier = config.getDouble("elite.speed-multiplier", eliteSpeedMultiplier);
        eliteKnockbackResistance = config.getDouble("elite.knockback-resistance", eliteKnockbackResistance);
        eliteNameVisible = config.getBoolean("elite.name-visible", eliteNameVisible);
        eliteShowDuration = config.getInt("elite.show-duration", eliteShowDuration);
        eliteStrengthEnabled = config.getBoolean("elite.strength.enabled", eliteStrengthEnabled);
        eliteStrengthLevel = config.getInt("elite.strength.level", eliteStrengthLevel);
    }

    private void loadDisplayNames() {
        displayNames.clear();
        if (config == null) return;

        // Hardened names
        Map<EntityType, String> hardenedNames = new HashMap<>();
        if (config.getConfigurationSection("hardened.display-names") != null) {
            for (String mobKey : config.getConfigurationSection("hardened.display-names").getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                    String name = config.getString("hardened.display-names." + mobKey);
                    hardenedNames.put(type, name);
                } catch (IllegalArgumentException e) {
                    // Ignore unknown types
                }
            }
        }
        displayNames.put("hardened", hardenedNames);

        // Elite names
        Map<EntityType, String> eliteNames = new HashMap<>();
        if (config.getConfigurationSection("elite.display-names") != null) {
            for (String mobKey : config.getConfigurationSection("elite.display-names").getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                    String name = config.getString("elite.display-names." + mobKey);
                    eliteNames.put(type, name);
                } catch (IllegalArgumentException e) {
                    // Ignore unknown types
                }
            }
        }
        displayNames.put("elite", eliteNames);
    }

    @SuppressWarnings("unchecked")
    private void loadSpecialAbilities() {
        specialAbilities.clear();
        if (config == null) return;
        if (config.getConfigurationSection("special-abilities") == null) return;

        for (String mobKey : config.getConfigurationSection("special-abilities").getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                Map<String, Object> abilities = new HashMap<>();

                if (config.contains("special-abilities." + mobKey + ".hardened")) {
                    abilities.put("hardened", config.getConfigurationSection("special-abilities." + mobKey + ".hardened").getValues(true));
                }
                if (config.contains("special-abilities." + mobKey + ".elite")) {
                    abilities.put("elite", config.getConfigurationSection("special-abilities." + mobKey + ".elite").getValues(true));
                }

                specialAbilities.put(type, abilities);
            } catch (IllegalArgumentException e) {
                // Ignore unknown types
            }
        }
    }

    // Getters
    public double getHardenedChance() { return hardenedChance; }
    public double getEliteChance() { return eliteChance; }
    public boolean isMobEnabled(EntityType type) { return enabledMobs.getOrDefault(type, false); }
    public boolean isMobEnabled(String mobName) {
        if (config == null) return false;
        return config.getBoolean("enabled-mobs." + mobName.toLowerCase(), false);
    }
    public double getHardenedHpMultiplier() { return hardenedHpMultiplier; }
    public double getHardenedDamageMultiplier() { return hardenedDamageMultiplier; }
    public double getHardenedKnockbackResistance() { return hardenedKnockbackResistance; }
    public boolean isHardenedNameVisible() { return hardenedNameVisible; }
    public int getHardenedShowDuration() { return hardenedShowDuration; }
    public double getEliteHpMultiplier() { return eliteHpMultiplier; }
    public double getEliteDamageMultiplier() { return eliteDamageMultiplier; }
    public double getEliteSpeedMultiplier() { return eliteSpeedMultiplier; }
    public double getEliteKnockbackResistance() { return eliteKnockbackResistance; }
    public boolean isEliteNameVisible() { return eliteNameVisible; }
    public int getEliteShowDuration() { return eliteShowDuration; }
    public boolean isEliteStrengthEnabled() { return eliteStrengthEnabled; }
    public int getEliteStrengthLevel() { return eliteStrengthLevel; }

    public List<String> getEnabledMobTypes() {
        List<String> result = new ArrayList<>();
        if (config == null || !config.isConfigurationSection("enabled-mobs")) return result;

        for (String key : config.getConfigurationSection("enabled-mobs").getKeys(false)) {
            if (config.getBoolean("enabled-mobs." + key)) {
                result.add(key.toLowerCase());
            }
        }
        return result;
    }

    public String getDisplayName(String type, EntityType mobType) {
        Map<EntityType, String> names = displayNames.get(type);
        return names != null ? names.get(mobType) : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSpecialAbilities(EntityType mobType, String variant) {
        Map<String, Object> abilities = specialAbilities.get(mobType);
        if (abilities != null && abilities.containsKey(variant)) {
            Object raw = abilities.get(variant);
            if (raw instanceof Map) {
                return (Map<String, Object>) raw;
            }
        }
        return new HashMap<>();
    }
}
