package com.flyaway.smartmobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

public class MobManager {

    private final Random random = new Random();
    private final ConfigManager configManager;

    public MobManager() {
        this.configManager = SmartMobs.getInstance().getConfigManager();
    }

    public void enhanceMob(LivingEntity entity) {
        if (entity == null || configManager == null) return;
        if (!configManager.isMobEnabled(entity.getType())) return;

        double rand = random.nextDouble();
        double hardenedChance = configManager.getHardenedChance();
        double eliteChance = configManager.getEliteChance();

        if (rand < eliteChance) {
            makeElite(entity);
        } else if (rand < eliteChance + hardenedChance) {
            makeHardened(entity);
        }
    }

    private double getDoubleFromConfig(Object value) {
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Double) return (Double) value;
        try { return Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return 1.0; }
    }

    private int getIntFromConfig(Object value) {
        if (value instanceof Double) return ((Double) value).intValue();
        if (value instanceof Integer) return (Integer) value;
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return 1; }
    }

    public void makeHardened(LivingEntity entity) {
        applyBaseModifications(entity, "hardened");
        applySpecialAbilities(entity, "hardened");
    }

    public void makeElite(LivingEntity entity) {
        applyBaseModifications(entity, "elite");
        applySpecialAbilities(entity, "elite");
    }

    private void applyBaseModifications(LivingEntity entity, String variant) {
        if (entity == null) return;

        double hpMultiplier = variant.equals("elite") ? configManager.getEliteHpMultiplier() : configManager.getHardenedHpMultiplier();
        double damageMultiplier = variant.equals("elite") ? configManager.getEliteDamageMultiplier() : configManager.getHardenedDamageMultiplier();
        double knockbackResistance = variant.equals("elite") ? configManager.getEliteKnockbackResistance() : configManager.getHardenedKnockbackResistance();

        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            double base = entity.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(base * hpMultiplier);
            double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            try { entity.setHealth(maxHealth); } catch (IllegalArgumentException ignored) {}
        }

        if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            double base = entity.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(base * damageMultiplier);
        }

        if (entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null) {
            entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
        }

        if (variant.equals("elite") && entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            double base = entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(base * configManager.getEliteSpeedMultiplier());
        }

        if (variant.equals("elite") && configManager.isEliteStrengthEnabled() && !(entity instanceof Creeper)) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE,
                    Math.max(0, configManager.getEliteStrengthLevel()), true, false, false));
        }

        setDisplayNameWithTimer(entity, variant);
    }

    private void setDisplayNameWithTimer(LivingEntity entity, String variant) {
        String displayName = configManager.getDisplayName(variant, entity.getType());
        if (displayName != null && !displayName.isEmpty()) {
            entity.setCustomName(displayName);
            entity.setCustomNameVisible(false);
        }
    }

    private void applySpecialAbilities(LivingEntity entity, String variant) {
        Map<String, Object> abilities = configManager.getSpecialAbilities(entity.getType(), variant);
        if (abilities == null || abilities.isEmpty()) return;

        abilities.forEach((key, value) -> applyAbility(entity, key, value, variant));
    }

    private void applyAbility(LivingEntity entity, String key, Object value, String variant) {
        switch (entity.getType()) {
            case SKELETON: applySkeletonAbility((Skeleton) entity, key, value, variant); break;
            case CREEPER: applyCreeperAbility((Creeper) entity, key, value, variant); break;
            case SPIDER: case CAVE_SPIDER: applySpiderAbility((Spider) entity, key, value, variant); break;
            case ENDERMAN: applyEndermanAbility((Enderman) entity, key, value, variant); break;
            case WITCH: applyWitchAbility((Witch) entity, key, value, variant); break;
            case PHANTOM: applyPhantomAbility((Phantom) entity, key, value, variant); break;
            case BLAZE: applyBlazeAbility((Blaze) entity, key, value, variant); break;
            case GHAST: applyGhastAbility((Ghast) entity, key, value, variant); break;
            default: break;
        }
    }

    // ===================== MOB-SPECIFIC ABILITY METHODS =====================
    private void applySkeletonAbility(Skeleton skeleton, String key, Object value, String variant) {
        switch (key) {
            case "arrow-speed-multiplier":
                double speedMultiplier = getDoubleFromConfig(value);
                skeleton.getPersistentDataContainer().set(
                    MobKeys.ARROW_SPEED_MULTIPLIER,
                    PersistentDataType.DOUBLE,
                    speedMultiplier
                );
                break;
            case "attack-speed":
                if (skeleton.getAttribute(Attribute.ATTACK_SPEED) != null) {
                    double attackSpeedMultiplier = getDoubleFromConfig(value);
                    double baseSpeed = skeleton.getAttribute(Attribute.ATTACK_SPEED).getBaseValue();
                    skeleton.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(baseSpeed * attackSpeedMultiplier);
                }
                break;
            case "triple-shot":
                if (Boolean.TRUE.equals(value)) {
                    skeleton.getPersistentDataContainer().set(
                        MobKeys.TRIPLE_SHOT,
                        PersistentDataType.BYTE,
                        (byte) 1
                    );
                }
                break;
        }
    }

    private void applyCreeperAbility(Creeper creeper, String key, Object value, String variant) {
        switch (key) {
            case "explosion-power":
                double explosionPower = getDoubleFromConfig(value);
                creeper.getPersistentDataContainer().set(
                    MobKeys.EXPLOSION_POWER,
                    PersistentDataType.DOUBLE,
                    explosionPower
                );
                break;
            case "fuse-time": int current = creeper.getMaxFuseTicks(); if (current > 0) creeper.setMaxFuseTicks(Math.max(1, (int)(current * getDoubleFromConfig(value)))); break;
            case "charged": if (Boolean.TRUE.equals(value)) creeper.setPowered(true); break;
        }
    }

    private void applySpiderAbility(Spider spider, String key, Object value, String variant) {
        switch (key) {
            case "jump-strength": if (spider.getAttribute(Attribute.JUMP_STRENGTH) != null) {
                double base = spider.getAttribute(Attribute.JUMP_STRENGTH).getBaseValue();
                spider.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(base * getDoubleFromConfig(value));
            } break;
            case "web-effect":
                if (Boolean.TRUE.equals(value)) {
                    spider.getPersistentDataContainer().set(
                        MobKeys.WEB_EFFECT,
                        PersistentDataType.BYTE,
                        (byte) 1
                    );
                }
                break;
            case "poison-bite":
                if (Boolean.TRUE.equals(value)) {
                    spider.getPersistentDataContainer().set(
                        MobKeys.POISON_BITE,
                        PersistentDataType.BYTE,
                        (byte) 1
                    );
                }
                break;
        }
    }

    private void applyEndermanAbility(Enderman enderman, String key, Object value, String variant) {
        switch (key) {
            case "teleport-range":
                double teleportRange = getDoubleFromConfig(value);
                enderman.getPersistentDataContainer().set(
                    MobKeys.TELEPORT_RANGE,
                    PersistentDataType.DOUBLE,
                    teleportRange
                );
                break;
            case "teleport-cooldown": if (enderman.getAttribute(Attribute.ATTACK_SPEED) != null) {
                    double baseSpeed = enderman.getAttribute(Attribute.ATTACK_SPEED).getBaseValue();
                    enderman.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(baseSpeed * getDoubleFromConfig(value));
                } break;
            case "water-resistance": if (Boolean.TRUE.equals(value)) {
                enderman.getPersistentDataContainer().set(
                    MobKeys.WATER_RESISTANT,
                    PersistentDataType.BYTE,
                    (byte) 1
                );
            } break;
        }
    }

    private void applyWitchAbility(Witch witch, String key, Object value, String variant) {
        switch (key) {
            case "potion-strength":
                double potionStrength = getDoubleFromConfig(value);
                witch.getPersistentDataContainer().set(
                    MobKeys.POTION_STRENGTH,
                    PersistentDataType.DOUBLE,
                    potionStrength
                );
                break;
            case "healing-chance":
                double healingChance = getDoubleFromConfig(value);
                witch.getPersistentDataContainer().set(
                    MobKeys.HEALING_CHANCE,
                    PersistentDataType.DOUBLE,
                    healingChance
                );
                break;
            case "resistance-potion": if (Boolean.TRUE.equals(value)) witch.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false)); break;
        }
    }

    private void applyPhantomAbility(Phantom phantom, String key, Object value, String variant) {
        switch (key) {
            case "swoop-speed": if (phantom.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                double base = phantom.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
                phantom.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(base * getDoubleFromConfig(value));
            } break;
            case "attack-cooldown": if (phantom.getAttribute(Attribute.ATTACK_SPEED) != null) {
                double baseAttackSpeed = phantom.getAttribute(Attribute.ATTACK_SPEED).getBaseValue();
                double attackMultiplier = getDoubleFromConfig(value);
                phantom.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(baseAttackSpeed * attackMultiplier);
            } break;
            case "size-multiplier": try { phantom.setSize(Math.max(1, Math.min(20, (int)Math.round(phantom.getSize() * getDoubleFromConfig(value))))); } catch (Exception ignored) {} break;
        }
    }

    private void applyBlazeAbility(Blaze blaze, String key, Object value, String variant) {
        switch (key) {
            case "fireball-speed":
                double fireballSpeed = getDoubleFromConfig(value);
                blaze.getPersistentDataContainer().set(
                    MobKeys.FIREBALL_SPEED,
                    PersistentDataType.DOUBLE,
                    fireballSpeed
                );
                break;
            case "fireball-count":
                int fireballCount = getIntFromConfig(value);
                blaze.getPersistentDataContainer().set(
                    MobKeys.FIREBALL_COUNT,
                    PersistentDataType.INTEGER,
                    fireballCount
                );
                break;
            case "fire-aura": if (Boolean.TRUE.equals(value)) startFireAura(blaze); break;
        }
    }

    private void applyGhastAbility(Ghast ghast, String key, Object value, String variant) {
        switch (key) {
            case "explosion-power":
                double explosionPower = getDoubleFromConfig(value);
                ghast.getPersistentDataContainer().set(
                    MobKeys.EXPLOSION_POWER,
                    PersistentDataType.DOUBLE,
                    explosionPower
                );
                break;
            case "fireball-speed":
                double fireballSpeed = getDoubleFromConfig(value);
                ghast.getPersistentDataContainer().set(
                    MobKeys.FIREBALL_SPEED,
                    PersistentDataType.DOUBLE,
                    fireballSpeed
                );
                break;
            case "triple-shot":
                if (Boolean.TRUE.equals(value)) {
                    ghast.getPersistentDataContainer().set(
                        MobKeys.TRIPLE_SHOT,
                        PersistentDataType.BYTE,
                        (byte) 1
                    );
                }
                break;
        }
    }

    private void startFireAura(Blaze blaze) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!blaze.isValid() || blaze.isDead()) { this.cancel(); return; }
                blaze.getNearbyEntities(3, 3, 3).forEach(entity -> {
                    if (entity instanceof LivingEntity && !(entity instanceof Blaze)) ((LivingEntity) entity).setFireTicks(60);
                });
                blaze.getWorld().spawnParticle(Particle.FLAME, blaze.getLocation(), 10, 1, 1, 1);
            }
        }.runTaskTimer(SmartMobs.getInstance(), 40L, 40L);
    }
}
