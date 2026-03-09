package com.flyaway.smartmobs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;

public class ConfigManager {

    private FileConfiguration config;
    private final Map<EntityType, Boolean> enabledMobs = new HashMap<>();
    private final Map<String, Map<EntityType, String>> displayNames = new HashMap<>();
    private final Map<EntityType, Map<String, Object>> specialAbilities = new HashMap<>();

    private double hardenedChance = 0.15;
    private double eliteChance = 0.05;
    private boolean radiusComplication = false;
    private double worldRadius = 10000;
    List<RadiusLevel> radiusLevels = new ArrayList<>();

    private double hardenedHpMultiplier = 1.25;
    private double hardenedDamageMultiplier = 1.25;
    private double hardenedKnockbackResistance = 0.5;
    private boolean hardenedNameVisible = true;

    private double eliteHpMultiplier = 1.5;
    private double eliteDamageMultiplier = 1.5;
    private double eliteSpeedMultiplier = 1.4;
    private double eliteKnockbackResistance = 0.8;
    private boolean eliteNameVisible = true;
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
        loadConfig();
    }

    private void loadChances() {
        if (config == null) return;
        hardenedChance = config.getDouble("chances.hardened", hardenedChance);
        eliteChance = config.getDouble("chances.elite", eliteChance);
        worldRadius = config.getDouble("chances.world-radius", 10000.0);
        radiusComplication = config.getBoolean("chances.radius-complication", false);
        if (!radiusComplication) return;

        List<RadiusLevel> result = new ArrayList<>();

        List<?> rawList = config.getList("chances.radius-levels");
        if (rawList == null) {
            SmartMobs.getInstance().getLogger().warning("[SmartMobs] radius-levels не найден в конфиге!");
            return;
        }

        for (Object item : rawList) {
            if (item instanceof Map<?, ?> map) {
                double from = parseDouble(map.get("from"), 0.0);
                double to = parseDouble(map.get("to"), 1.0);
                double hardened = parseDouble(map.get("hardened"), getHardenedChance());
                double elite = parseDouble(map.get("elite"), getEliteChance());

                result.add(new RadiusLevel(from, to, hardened, elite));
            } else {
                SmartMobs.getInstance().getLogger().warning("[SmartMobs] Элемент radius-levels имеет неверный формат: " + item);
            }
        }

        radiusLevels = result;
        SmartMobs.getInstance().getLogger().info("[SmartMobs] Загружено уровней сложности: " + result.size());
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
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void loadHardenedSettings() {
        if (config == null) return;
        hardenedHpMultiplier = config.getDouble("hardened.hp-multiplier", hardenedHpMultiplier);
        hardenedDamageMultiplier = config.getDouble("hardened.damage-multiplier", hardenedDamageMultiplier);
        hardenedKnockbackResistance = config.getDouble("hardened.knockback-resistance", hardenedKnockbackResistance);
        hardenedNameVisible = config.getBoolean("hardened.name-visible", hardenedNameVisible);
    }

    private void loadEliteSettings() {
        if (config == null) return;
        eliteHpMultiplier = config.getDouble("elite.hp-multiplier", eliteHpMultiplier);
        eliteDamageMultiplier = config.getDouble("elite.damage-multiplier", eliteDamageMultiplier);
        eliteSpeedMultiplier = config.getDouble("elite.speed-multiplier", eliteSpeedMultiplier);
        eliteKnockbackResistance = config.getDouble("elite.knockback-resistance", eliteKnockbackResistance);
        eliteNameVisible = config.getBoolean("elite.name-visible", eliteNameVisible);
        eliteStrengthEnabled = config.getBoolean("elite.strength.enabled", eliteStrengthEnabled);
        eliteStrengthLevel = config.getInt("elite.strength.level", eliteStrengthLevel);
    }

    private void loadDisplayNames() {
        displayNames.clear();
        if (config == null) return;

        Map<EntityType, String> hardenedNames = new HashMap<>();
        if (config.getConfigurationSection("hardened.display-names") != null) {
            for (String mobKey : config.getConfigurationSection("hardened.display-names").getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                    String name = config.getString("hardened.display-names." + mobKey);
                    hardenedNames.put(type, name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        displayNames.put("hardened", hardenedNames);

        Map<EntityType, String> eliteNames = new HashMap<>();
        if (config.getConfigurationSection("elite.display-names") != null) {
            for (String mobKey : config.getConfigurationSection("elite.display-names").getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(mobKey.toUpperCase());
                    String name = config.getString("elite.display-names." + mobKey);
                    eliteNames.put(type, name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        displayNames.put("elite", eliteNames);
    }

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
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public double getHardenedChance() {
        return hardenedChance;
    }

    public double getEliteChance() {
        return eliteChance;
    }

    public boolean isMobEnabled(EntityType type) {
        return enabledMobs.getOrDefault(type, false);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "message-not-found: " + key);
    }

    public boolean isMobEnabled(String mobName) {
        if (mobName == null) return false;

        try {
            EntityType type = EntityType.valueOf(mobName.toUpperCase());
            return enabledMobs.getOrDefault(type, false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public double getHardenedHpMultiplier() {
        return hardenedHpMultiplier;
    }

    public double getHardenedDamageMultiplier() {
        return hardenedDamageMultiplier;
    }

    public double getHardenedKnockbackResistance() {
        return hardenedKnockbackResistance;
    }

    public double getEliteHpMultiplier() {
        return eliteHpMultiplier;
    }

    public double getEliteDamageMultiplier() {
        return eliteDamageMultiplier;
    }

    public double getEliteSpeedMultiplier() {
        return eliteSpeedMultiplier;
    }

    public double getEliteKnockbackResistance() {
        return eliteKnockbackResistance;
    }

    public boolean isEliteStrengthEnabled() {
        return eliteStrengthEnabled;
    }

    public int getEliteStrengthLevel() {
        return eliteStrengthLevel;
    }

    public List<String> getEnabledMobTypes() {
        List<String> result = new ArrayList<>();

        for (Map.Entry<EntityType, Boolean> entry : enabledMobs.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                result.add(entry.getKey().name().toLowerCase());
            }
        }
        return result;
    }

    public boolean isNameVisible(String type) {
        if (Objects.equals(type, "hardened")) return hardenedNameVisible;
        else if (Objects.equals(type, "elite")) return eliteNameVisible;
        return false;
    }

    public String getDisplayName(String type, EntityType mobType) {
        if (!isNameVisible(type)) return null;
        Map<EntityType, String> names = displayNames.get(type);
        return names != null ? names.get(mobType) : null;
    }

    public boolean isRadiusComplicationEnabled() {
        return radiusComplication;
    }

    public double getWorldRadius() {
        return worldRadius;
    }

    public static class RadiusLevel {
        public final double from;
        public final double to;
        public final double hardened;
        public final double elite;

        public RadiusLevel(double from, double to, double hardened, double elite) {
            this.from = from;
            this.to = to;
            this.hardened = hardened;
            this.elite = elite;
        }
    }

    public List<RadiusLevel> getRadiusLevels() {
        return radiusLevels;
    }

    private double parseDouble(Object obj, double def) {
        if (obj instanceof Number num) return num.doubleValue();
        if (obj instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

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
