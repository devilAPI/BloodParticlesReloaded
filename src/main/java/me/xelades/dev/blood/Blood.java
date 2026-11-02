package me.xelades.dev.blood;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Blood extends JavaPlugin implements Listener {

    /** Particles per point of damage at intensity 1.0. */
    private static final double BASE_PARTICLES_PER_DAMAGE = 5.0;

    private static final String PREFIX = ChatColor.RED + "[Blood] " + ChatColor.GRAY;

    static final String PERM_BYPASS = "blood.bypass";
    static final String PERM_RELOAD = "blood.commands.reload";
    static final String PERM_TOGGLE = "blood.commands.toggle";
    static final String PERM_TOGGLE_OTHERS = "blood.commands.toggle.others";

    /** Stored on the player (survives restarts) when they turned their blood off. */
    private NamespacedKey bloodDisabledKey;

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

        bloodDisabledKey = new NamespacedKey(this, "blood_disabled");
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

    // ------------------------------------------------------------------
    //  Per-player toggle
    // ------------------------------------------------------------------

    private boolean isBloodDisabled(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.has(bloodDisabledKey, PersistentDataType.BYTE);
    }

    /** Flips the player's setting and returns true if blood is now enabled. */
    private boolean toggleBlood(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (data.has(bloodDisabledKey, PersistentDataType.BYTE)) {
            data.remove(bloodDisabledKey);
            return true;
        }
        data.set(bloodDisabledKey, PersistentDataType.BYTE, (byte) 1);
        return false;
    }

    private static String state(boolean enabled) {
        return enabled ? ChatColor.GREEN + "enabled" + ChatColor.GRAY : ChatColor.RED + "disabled" + ChatColor.GRAY;
    }

    // ------------------------------------------------------------------
    //  Commands
    // ------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";

        if (sub.equals("reload") && args.length == 1) {
            if (!checkPermission(sender, PERM_RELOAD)) {
                return true;
            }
            loadSettings();
            sender.sendMessage(PREFIX + "Config reloaded.");
            return true;
        }

        if (sub.equals("toggle") && args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(PREFIX + "Usage from console: /" + label + " toggle <player>");
                return true;
            }
            if (!checkPermission(sender, PERM_TOGGLE)) {
                return true;
            }
            boolean enabled = toggleBlood((Player) sender);
            sender.sendMessage(PREFIX + "Your blood is now " + state(enabled) + ".");
            return true;
        }

        if (sub.equals("toggle") && args.length == 2) {
            if (!checkPermission(sender, PERM_TOGGLE_OTHERS)) {
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(PREFIX + "Player " + ChatColor.WHITE + args[1] + ChatColor.GRAY + " is not online.");
                return true;
            }
            boolean enabled = toggleBlood(target);
            sender.sendMessage(PREFIX + "Blood for " + ChatColor.WHITE + target.getName()
                    + ChatColor.GRAY + " is now " + state(enabled) + ".");
            if (target != sender) {
                target.sendMessage(PREFIX + "Your blood was " + state(enabled) + " by "
                        + ChatColor.WHITE + sender.getName() + ChatColor.GRAY + ".");
            }
            return true;
        }

        sendUsage(sender, label);
        return true;
    }

    private static boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to do that.");
        return false;
    }

    private static void sendUsage(CommandSender sender, String label) {
        List<String> lines = new ArrayList<>();
        if (sender.hasPermission(PERM_TOGGLE) && sender instanceof Player) {
            lines.add("/" + label + " toggle " + ChatColor.DARK_GRAY + "- turn your blood on/off");
        }
        if (sender.hasPermission(PERM_TOGGLE_OTHERS)) {
            lines.add("/" + label + " toggle <player> " + ChatColor.DARK_GRAY + "- turn a player's blood on/off");
        }
        if (sender.hasPermission(PERM_RELOAD)) {
            lines.add("/" + label + " reload " + ChatColor.DARK_GRAY + "- reload config.yml");
        }
        if (lines.isEmpty()) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to use this command.");
            return;
        }
        sender.sendMessage(PREFIX + "Commands:");
        for (String line : lines) {
            sender.sendMessage(ChatColor.GRAY + "  " + line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission(PERM_TOGGLE) || sender.hasPermission(PERM_TOGGLE_OTHERS)) {
                options.add("toggle");
            }
            if (sender.hasPermission(PERM_RELOAD)) {
                options.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")
                && sender.hasPermission(PERM_TOGGLE_OTHERS)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
        } else {
            return Collections.emptyList();
        }

        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(typed)) {
                matches.add(option);
            }
        }
        return matches;
    }

    // ------------------------------------------------------------------
    //  Blood
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return; // no blood from item frames, minecarts, etc.
        }
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.hasPermission(PERM_BYPASS) || isBloodDisabled(player)) {
                return;
            }
        } else if (playersOnly) {
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
