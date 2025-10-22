package com.flyaway.smartmobs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {

    private final MobManager mobManager;

    public MobSpawnListener() {
        this.mobManager = SmartMobs.getInstance().getMobManager();
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        // Игнорируем спавн из яиц, порталов и т.д.
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
            reason == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
            mobManager.enhanceMob((org.bukkit.entity.LivingEntity) event.getEntity());
        }
    }
}
