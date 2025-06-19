package majster2nn.dev.betonQuestQT.Tracker.PathFinding;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.IntStream;

public class PlayerQuestTracker {
    private static Map<Player, QuestPackage> playerActiveQuest = new HashMap<>();
    private static Map<Player, BukkitRunnable> runningTasks = new HashMap<>();
    private static final long cooldown = 20;

    public static Map<Player, QuestPackage> getPlayerActiveQuest() {
        return playerActiveQuest;
    }

    public static void setPlayerActiveQuest(Player player, QuestPackage activeQuest) {
        playerActiveQuest.put(player, activeQuest);
    }

    public static void activateQuestTracking(Player player){
        ConfigurationSection parametersSection = playerActiveQuest.get(player).getConfig().getConfigurationSection("questParameters");
        if(parametersSection.contains("location")){
            List<String> preFormatLocation = List.of(parametersSection.getString("location").split(","));

            List<Integer> coords = IntStream.range(0, 3)
                    .mapToObj(i -> Integer.parseInt(preFormatLocation.get(i)))
                    .toList();

            World world = Bukkit.getWorld(preFormatLocation.size() > 3 ? preFormatLocation.get(3) : "world");

            Location location = new Location(world, coords.get(0), coords.get(1), coords.get(2));

            if (runningTasks.containsKey(player)) {
                runningTasks.get(player).cancel();
                runningTasks.remove(player);
            }


            BukkitRunnable playerTrackTask = new BukkitRunnable() {
                QuestPackage currentlyActivePackage = playerActiveQuest.get(player);
                @Override
                public void run() {
                    if(!Bukkit.getOnlinePlayers().contains(player)) this.cancel();
                    if(currentlyActivePackage != playerActiveQuest.get(player)) {
                        this.cancel();
                        runningTasks.remove(player); // clean up
                    }

                    RayTraceResult traceResult = player.getWorld().rayTraceBlocks(
                            player.getLocation().add(0, 1, 0),
                            new Vector(0, -1, 0),
                            3.0
                    );

                    Location playerLocation = traceResult != null
                            ? traceResult.getHitPosition().toLocation(player.getWorld())
                            : player.getLocation();

                    if(playerLocation.getBlock().isSolid()){
                        playerLocation = playerLocation.add(0, 1, 0);
                    }

                    PathFinder pathFinder = new PathFinder(playerLocation, location, 1500, true, 5);
                    List<Location> locationsToSpawnAt = new ArrayList<>(Arrays.asList(pathFinder.findPath()));
                    List<Location> processedLocations = new ArrayList<>();

                    for (int i = 0; i < locationsToSpawnAt.size() - 1; i += 1) {
                        Location a = locationsToSpawnAt.get(i).clone();
                        Location b = locationsToSpawnAt.get(i + 1).clone();

                        processedLocations.add(a);

                        // Example logic: midpoint
                        Location between = a.clone().add(b).multiply(0.5);
                        processedLocations.add(between);
                        processedLocations.add(b);
                    }

                    if (locationsToSpawnAt.size() % 2 != 0) {
                        processedLocations.add(locationsToSpawnAt.getLast().clone());
                    }

                    spawnTrackForPlayer(player, processedLocations);

                }
            };

            runningTasks.put(player, playerTrackTask);
            playerTrackTask.runTaskTimer(BetonQuestQT.getInstance(), 0L, cooldown);
        }
    }

    public static void spawnTrackForPlayer(Player player, List<Location> locationsToSpawnAt){
        BukkitRunnable spawnTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Particle.DustOptions dust = new Particle.DustOptions(Color.GREEN, 1.0F); // color, size

                for(Location loc : locationsToSpawnAt){
                    player.spawnParticle(Particle.DUST, loc.clone().add(new Vector(0.5, 0.3, 0.5)), 1, dust);
                }

                ticks += 5;
                if (ticks >= cooldown) { // 5 seconds at 20 ticks per second
                    this.cancel(); // Stop the task
                }
            }
        };
        spawnTask.runTaskTimer(BetonQuestQT.getInstance(), 0, 5);
    }
}
