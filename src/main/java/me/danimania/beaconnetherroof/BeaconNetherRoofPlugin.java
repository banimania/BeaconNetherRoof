package me.danimania.beaconnetherroof;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class BeaconNetherRoofPlugin extends JavaPlugin {

    public static BeaconNetherRoofPlugin instance;

    public static ArrayList<Location> loadedBeaconLocations;

    public static int minDistance;
    public static double damage;

    public BeaconNetherRoofPlugin() {
        instance = this;
        loadedBeaconLocations = new ArrayList<>();
    }

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        minDistance = getConfig().getInt("distance");
        damage = getConfig().getInt("damage");

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                findAllLoadedBeacons();
                dealDamageOnNetherRoof();
            }
        }, 0, 20);
    }

    /**
     * Checks for players on the nether roof
     * and deals damage to them if they are not
     * near a beacon.
     * */
    private static void dealDamageOnNetherRoof() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                if (p.getLocation().getBlockY() > 127) {
                    boolean isInBeaconRange = false;

                    for (Location beaconLocation : loadedBeaconLocations) {
                        if (p.getLocation().distance(beaconLocation) < minDistance) {
                            isInBeaconRange = true;
                        }
                    }

                    if (!isInBeaconRange) {
                        p.damage(damage);
                    }
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
        for (Chunk chunk : Bukkit.getWorld("world_nether").getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Beacon) {
                    Beacon beacon = (Beacon) blockState;
                    if (beacon.getTier() != 0) {
                        loadedBeaconLocations.add(blockState.getLocation());
                    }
                }
            }
        }
    }

}
