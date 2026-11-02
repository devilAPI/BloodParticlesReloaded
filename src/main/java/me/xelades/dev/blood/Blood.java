package me.xelades.dev.blood;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Blood extends JavaPlugin implements Listener {

    /** Particles per point of damage at intensity 1.0. */
    private static final double BASE_PARTICLES_PER_DAMAGE = 5.0;

    private Particle blockParticle;
    private BlockData bloodData;

    private boolean playersOnly;
    private double intensity;
    private int maxParticles;
    private double spread;

    @Override
    public void onEnable() {
        blockParticle = resolveBlockParticle();
        if (blockParticle == null) {
            getLogger().severe("Could not find a block particle type on this server version. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * The block particle was called BLOCK_CRACK before 1.20.5 and BLOCK afterwards.
     * Looking it up by name lets the same code run on old and new servers.
     */
    private static Particle resolveBlockParticle() {
        for (String name : new String[]{"BLOCK", "BLOCK_CRACK"}) {
            try {
                Object value = Particle.class.getField(name).get(null);
                if (value instanceof Particle) {
                    return (Particle) value;
                }
            } catch (ReflectiveOperationException ignored) {
                // try the next name
            }
        }
        return null;
    }

    private void loadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();

        playersOnly = config.getBoolean("players-only", false);
        intensity = Math.max(0.0, config.getDouble("intensity", 1.0));
        maxParticles = Math.max(0, config.getInt("max-particles", 50));
        spread = Math.max(0.0, config.getDouble("spread", 0.4));

        Material material = Material.matchMaterial(config.getString("block", "REDSTONE_BLOCK"));
        if (material == null || !material.isBlock()) {
            getLogger().warning("Invalid 'block' in config.yml, falling back to REDSTONE_BLOCK.");
            material = Material.REDSTONE_BLOCK;
        }
        bloodData = material.createBlockData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            loadSettings();
            sender.sendMessage(ChatColor.RED + "[Blood] " + ChatColor.GRAY + "Config reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "[Blood] " + ChatColor.GRAY + "Usage: /" + label + " reload");
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return; // no blood from item frames, minecarts, etc.
        }
        if (playersOnly && !(entity instanceof Player)) {
            return;
        }

        int particleCount = (int) Math.min(
                event.getFinalDamage() * BASE_PARTICLES_PER_DAMAGE * intensity, maxParticles);
        if (particleCount <= 0) {
            return;
        }

        Location bloodLocation = entity.getLocation().add(0, entity.getHeight() / 1.5, 0);
        entity.getWorld().spawnParticle(blockParticle, bloodLocation, particleCount,
                spread, spread, spread, bloodData);
    }
}
