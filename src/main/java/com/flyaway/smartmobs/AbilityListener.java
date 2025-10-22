package com.flyaway.smartmobs;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Random;

public class AbilityListener implements Listener {
    private final Random random = new Random();

    public AbilityListener() {}

    // ========== SKELETON ==========
    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton)) return;

        PersistentDataContainer pdc = skeleton.getPersistentDataContainer();

        // Получаем множитель скорости
        Double speedMultiplier = pdc.get(MobKeys.ARROW_SPEED_MULTIPLIER, PersistentDataType.DOUBLE);
        if (speedMultiplier != null && event.getProjectile() != null) {
            Vector velocity = applySpeedAndTrajectoryCorrection(skeleton, event.getProjectile().getVelocity(), speedMultiplier);
            event.getProjectile().setVelocity(velocity);
        }

        // TRIPLE SHOT
        if (pdc.has(MobKeys.TRIPLE_SHOT, PersistentDataType.BYTE)) {
            final Double finalMultiplier = speedMultiplier;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skeleton.isValid() || skeleton.isDead()) return;

                    Vector baseVelocity = event.getProjectile().getVelocity();
                    for (int i = 0; i < 2; i++) {
                        Arrow extraArrow = skeleton.launchProjectile(Arrow.class);

                        // Создаём небольшой горизонтальный разброс
                        Vector spread = baseVelocity.clone().rotateAroundY((i == 0 ? 0.08 : -0.08));

                        // Применяем коррекцию траектории к дополнительным стрелам
                        if (finalMultiplier != null) {
                            spread = applySpeedAndTrajectoryCorrection(skeleton, spread, finalMultiplier);
                        }

                        extraArrow.setVelocity(spread);
                    }
                }
            }.runTaskLater(SmartMobs.getInstance(), 1L);
        }
    }

    // Общий метод для коррекции скорости и траектории
    private Vector applySpeedAndTrajectoryCorrection(Skeleton skeleton, Vector velocity, double speedMultiplier) {
        Vector result = velocity.clone().normalize().multiply(velocity.length() * speedMultiplier);

        LivingEntity target = skeleton.getTarget();
        if (target != null) {
            Vector diff = target.getEyeLocation().toVector().subtract(skeleton.getEyeLocation().toVector());
            double distance = diff.length();

            // Добавляем корректировку Y по формуле с учётом гравитации стрелы
            double gravity = 0.05;
            double time = distance / (velocity.length() * speedMultiplier); // время полёта
            result.setY(diff.getY() / time + 0.5 * gravity * time);
        }

        return result;
    }

    // ========== CREEPER ==========
    @EventHandler
    public void onCreeperExplosion(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;

        PersistentDataContainer pdc = creeper.getPersistentDataContainer();

        Double explosionPower = pdc.get(MobKeys.EXPLOSION_POWER, PersistentDataType.DOUBLE);
        if (explosionPower != null) {
            event.setRadius((float) (event.getRadius() * explosionPower));
        }
    }

    // ========== SPIDER ==========
    @EventHandler
    public void onSpiderAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Spider spider)) return;

        PersistentDataContainer pdc = spider.getPersistentDataContainer();

        if (pdc.has(MobKeys.POISON_BITE, PersistentDataType.BYTE) && event.getEntity() instanceof LivingEntity target) {
            if (random.nextDouble() < 0.3) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            }
        }

        if (pdc.has(MobKeys.WEB_EFFECT, PersistentDataType.BYTE)) {
            if (random.nextDouble() < 0.2) {
                Location loc = event.getEntity().getLocation();
                if (loc.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.COBWEB);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (loc.getBlock().getType() == Material.COBWEB) loc.getBlock().setType(Material.AIR);
                        }
                    }.runTaskLater(SmartMobs.getInstance(), 100L);
                }
            }
        }
    }

    // ========== ENDERMAN ==========
    @EventHandler
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof Enderman enderman)) return;

        PersistentDataContainer pdc = enderman.getPersistentDataContainer();

        Double teleportRange = pdc.get(MobKeys.TELEPORT_RANGE, PersistentDataType.DOUBLE);

        if (teleportRange != null) {
            Vector dir = event.getTo().toVector().subtract(event.getFrom().toVector());
            event.setTo(event.getFrom().clone().add(dir.multiply(teleportRange)));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Enderman enderman)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING ||
            event.getCause() == EntityDamageEvent.DamageCause.CONTACT) {

            // Проверяем, имеет ли эндермен иммунитет к воде
            if (!enderman.getPersistentDataContainer().has(MobKeys.WATER_RESISTANT, PersistentDataType.BYTE)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    // ========== WITCH ==========
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Witch witch)) return;

        PersistentDataContainer pdc = witch.getPersistentDataContainer();

        Double potionStrength = pdc.get(MobKeys.POTION_STRENGTH, PersistentDataType.DOUBLE);
        if (potionStrength != null) {
            for (PotionEffect effect : event.getPotion().getEffects()) {
                PotionEffect newEffect = new PotionEffect(
                    effect.getType(),
                    Math.max(1, (int) (effect.getDuration() * potionStrength)),
                    Math.max(0, (int) (effect.getAmplifier() * potionStrength)),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
                );
                for (LivingEntity le : event.getAffectedEntities()) le.addPotionEffect(newEffect);
            }
        }

        Double healingChance = pdc.get(MobKeys.HEALING_CHANCE, PersistentDataType.DOUBLE);
        if (healingChance != null && random.nextDouble() < healingChance) {
            witch.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1));
            witch.getWorld().spawnParticle(Particle.HEART, witch.getLocation().add(0, 2, 0), 3);
        }
    }

    // ========== BLAZE ==========
    @EventHandler
    public void onBlazeShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof SmallFireball proj)) return;
        ProjectileSource shooter = proj.getShooter();
        if (!(shooter instanceof Blaze blaze)) return;

        // Пропускаем фаерболы, созданные нашим кодом
        PersistentDataContainer projPdc = proj.getPersistentDataContainer();
        if (projPdc.has(MobKeys.EXTRA_PROJECTILE, PersistentDataType.BYTE)) return;

        PersistentDataContainer blazePdc = blaze.getPersistentDataContainer();

        Double fireballSpeed = blazePdc.get(MobKeys.FIREBALL_SPEED, PersistentDataType.DOUBLE);
        if (fireballSpeed != null) {
            proj.setVelocity(proj.getVelocity().multiply(fireballSpeed));
        }

        Integer fireballCount = blazePdc.get(MobKeys.FIREBALL_COUNT, PersistentDataType.INTEGER);
        if (fireballCount == null || fireballCount <= 1) return;

        Vector baseDir = proj.getVelocity().clone().normalize();
        Location spawnLoc = proj.getLocation().add(baseDir.multiply(0.5)); // чуть впереди от ифрита
        final double baseSpeed = proj.getVelocity().length();

        for (int i = 1; i < fireballCount; i++) {
            final int delay = i * 3;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!blaze.isValid() || blaze.isDead()) return;

                    Vector spread = baseDir.clone().add(randomSpreadVector(0.15)).normalize();

                    // Спавним вручную, чтобы метку успеть поставить ДО того, как сработает событие
                    SmallFireball extra = (SmallFireball) blaze.getWorld().spawnEntity(spawnLoc, EntityType.SMALL_FIREBALL);
                    extra.getPersistentDataContainer().set(MobKeys.EXTRA_PROJECTILE, PersistentDataType.BYTE, (byte) 1);
                    extra.setShooter(blaze);

                    Vector velocity = spread.multiply(baseSpeed);
                    if (fireballSpeed != null) {
                        velocity = velocity.multiply(fireballSpeed);
                    }
                    extra.setDirection(velocity);
                    extra.setVelocity(velocity);
                }
            }.runTaskLater(SmartMobs.getInstance(), delay);
        }
    }

    // ========== GHAST ==========
    @EventHandler
    public void onGhastShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof LargeFireball proj)) return;
        ProjectileSource shooter = proj.getShooter();
        if (!(shooter instanceof Ghast ghast)) return;

        // Пропускаем дополнительные снаряды, чтобы избежать рекурсии
        if (proj.getPersistentDataContainer().has(MobKeys.EXTRA_PROJECTILE, PersistentDataType.BYTE)) return;

        PersistentDataContainer pdc = ghast.getPersistentDataContainer();
        Double fireballSpeed = pdc.get(MobKeys.FIREBALL_SPEED, PersistentDataType.DOUBLE);
        Double explosionPower = pdc.get(MobKeys.EXPLOSION_POWER, PersistentDataType.DOUBLE);

        if (fireballSpeed != null) proj.setVelocity(proj.getVelocity().multiply(fireballSpeed));
        if (explosionPower != null) proj.setYield((float)(proj.getYield() * explosionPower));

        if (!pdc.has(MobKeys.TRIPLE_SHOT, PersistentDataType.BYTE)) return;

        Vector baseDir = proj.getVelocity().clone().normalize();
        Vector perpendicular = new Vector(-baseDir.getZ(), 0, baseDir.getX()).normalize();
        double baseSpeed = proj.getVelocity().length();
        double spreadAmount = 0.3;
        Location spawnLoc = proj.getLocation().add(baseDir);

        // Левый и правый снаряды через spawnEntity
        LargeFireball left = (LargeFireball) ghast.getWorld().spawnEntity(spawnLoc.clone().add(perpendicular.clone().multiply(spreadAmount)), EntityType.FIREBALL);
        LargeFireball right = (LargeFireball) ghast.getWorld().spawnEntity(spawnLoc.clone().subtract(perpendicular.clone().multiply(spreadAmount)), EntityType.FIREBALL);

        // Ставим метку сразу
        left.getPersistentDataContainer().set(MobKeys.EXTRA_PROJECTILE, PersistentDataType.BYTE, (byte) 1);
        right.getPersistentDataContainer().set(MobKeys.EXTRA_PROJECTILE, PersistentDataType.BYTE, (byte) 1);

        // Назначаем стрелка
        left.setShooter(ghast);
        right.setShooter(ghast);

        // Устанавливаем направления и модификаторы
        Vector leftVel = baseDir.clone().add(perpendicular.clone().multiply(spreadAmount)).normalize().multiply(baseSpeed);
        Vector rightVel = baseDir.clone().subtract(perpendicular.clone().multiply(spreadAmount)).normalize().multiply(baseSpeed);
        if (fireballSpeed != null) {
            leftVel.multiply(fireballSpeed);
            rightVel.multiply(fireballSpeed);
        }
        left.setVelocity(leftVel);
        right.setVelocity(rightVel);

        if (explosionPower != null) {
            left.setYield((float)(left.getYield() * explosionPower));
            right.setYield((float)(right.getYield() * explosionPower));
        }
    }

    private Vector randomSpreadVector(double maxOffset) {
        double ox = (random.nextDouble() * 2 - 1) * maxOffset;
        double oy = (random.nextDouble() * 2 - 1) * maxOffset;
        double oz = (random.nextDouble() * 2 - 1) * maxOffset;
        return new Vector(ox, oy, oz);
    }
}
