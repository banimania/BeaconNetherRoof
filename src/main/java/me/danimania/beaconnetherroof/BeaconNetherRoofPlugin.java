package me.danimania.beaconnetherroof;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class BeaconNetherRoofPlugin extends JavaPlugin implements Listener {

    public static BeaconNetherRoofPlugin instance;

    public static ArrayList<Location> loadedBeaconLocations;

    public static int minDistance;
    public static double damage;
    public static int delay;

    public BeaconNetherRoofPlugin() {
        instance = this;
        loadedBeaconLocations = new ArrayList<>();
    }

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        instance.getServer().getPluginManager().registerEvents(this, this);
        minDistance = getConfig().getInt("distance");
        damage = getConfig().getInt("damage");
        delay = getConfig().getInt("delay");

        //Scheduler
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                findAllLoadedBeacons();
                dealDamageOnNetherRoof();
            }
        }, 0, 20 * delay);

    }

    /**
     * Checks for players on the nether roof
     * and deals damage to them if they are not
     * near a beacon.
     * */
    private static void dealDamageOnNetherRoof() {
        for (Player p : instance.getServer().getWorld("world_nether").getPlayers()) {
            if (p.getLocation().getBlockY() > 127) {
                boolean isInBeaconRange = false;

                for (Location beaconLocation : loadedBeaconLocations) {
                    if (p.getLocation().distance(beaconLocation) < minDistance) {
                        if (beaconLocation.getBlock().getState() instanceof Beacon) {
                            Beacon beacon = (Beacon) beaconLocation.getBlock().getState();
                            if (beacon.getTier() != 0) {
                                isInBeaconRange = true;
                                break;
                            }
                        }
                    }
                }

                if (!isInBeaconRange) {
                    p.damage(damage);
                }
            }
        }
    }

    /**
     * Locates all loaded beacons and stores
     * their coordinates
     * */
    private static void findAllLoadedBeacons() {
        loadedBeaconLocations.clear();
        for (Chunk chunk : instance.getServer().getWorld("world_nether").getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Beacon) {
                    loadedBeaconLocations.add(blockState.getLocation());
                }
            }
        }
    }


    /**
     * Add beacons placed to the list, this way
     * we can increase the delay between beacon
     * checks.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlacedEvent(BlockPlaceEvent event) {
        Location blockPos = event.getBlockPlaced().getLocation();
        if (event.getBlockPlaced().getState() instanceof Beacon) {
            if (loadedBeaconLocations.contains(blockPos)) {
                return;
            }
            loadedBeaconLocations.add(event.getBlockPlaced().getLocation());
        }
    }

    /**
     * Check all beacon locations when a player changes
     * dimension
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeDimensionEvent(PlayerChangedWorldEvent event) {
        if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NETHER) {
            findAllLoadedBeacons();
        }
    }
}
