package me.xelades.dev.blood;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Blood extends JavaPlugin implements Listener {

    private static final double PARTICLE_MULTIPLIER = 5.0;
    private static final int PARTICLE_MAX = 50;
    private static final double PARTICLE_SPREAD = 0.4;

    private BlockData bloodData;

    @Override
    public void onEnable() {
        bloodData = Material.REDSTONE_BLOCK.createBlockData();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return; // no blood from item frames, minecarts, etc.
        }

        int particleCount = (int) Math.min(event.getFinalDamage() * PARTICLE_MULTIPLIER, PARTICLE_MAX);
        if (particleCount <= 0) {
            return;
        }

        Location bloodLocation = entity.getLocation().add(0, entity.getHeight() / 1.5, 0);
        entity.getWorld().spawnParticle(Particle.BLOCK, bloodLocation, particleCount,
                PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, bloodData);
    }
}
