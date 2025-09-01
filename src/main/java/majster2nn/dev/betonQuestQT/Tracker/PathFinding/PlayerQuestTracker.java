package majster2nn.dev.betonQuestQT.tracker.PathFinding;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.IntStream;

public class PlayerQuestTracker {
    private static Map<Player, QuestPlaceholder> playerActiveQuest = new HashMap<>();
    private static Map<Player, BukkitRunnable> runningTasks = new HashMap<>();
    private static final long cooldown = 20;

    public static QuestPlaceholder getPlayerActiveQuest(Player player) {
        return playerActiveQuest.getOrDefault(player, null);
    }

    public static void setPlayerActiveQuest(Player player, QuestPlaceholder activeQuest) {
        if(activeQuest == null){
            playerActiveQuest.remove(player);
            runningTasks.remove(player);
        }else {
            playerActiveQuest.put(player, activeQuest);
        }
    }

    public static void activateQuestTracking(Player player){
        if(playerActiveQuest.get(player).currentlyActiveQuestPart == null) return;
        //TODO add a possibility to add checkpoints that the tracker needs to run through so that the tracker points to a point that is set and tries to find a path from the player to there and from there to another point/last point and track from player to the nearest point and deactivate the ones that are further from the final location
        String preFindLoc = playerActiveQuest.get(player).currentlyActiveQuestPart.getLocation();

        if(preFindLoc != null && !preFindLoc.isEmpty() && !preFindLoc.isBlank()){
            List<String> preFormatLocation = List.of(preFindLoc.split(","));

            if(preFormatLocation.size() < 3)return;

            List<Integer> coords = IntStream.range(0, 3)
                    .mapToObj(i -> Integer.parseInt(preFormatLocation.get(i)))
                    .toList();

            World world = Bukkit.getWorld(preFormatLocation.size() > 3 ? preFormatLocation.get(3) : player.getWorld().getName());

            Location location = new Location(world, coords.get(0), coords.get(1), coords.get(2));

            if (runningTasks.containsKey(player)) {
                runningTasks.get(player).cancel();
                runningTasks.remove(player);
            }


            BukkitRunnable playerTrackTask = new BukkitRunnable() {
                final QuestPackage currentlyActivePackage = playerActiveQuest.get(player).questPackage;
                @Override
                public void run() {
                    if(getPlayerActiveQuest(player) != null){
                        if(getPlayerActiveQuest(player).status != Statuses.ACTIVE){
                            setPlayerActiveQuest(player, null);
                        }else {
                            getPlayerActiveQuest(player).update(player);
                        }
                    }

                    if(playerActiveQuest.get(player) == null){
                        this.cancel();
                        runningTasks.remove(player);
                        return;
                    }
                    if(!Bukkit.getOnlinePlayers().contains(player)) {
                        this.cancel();
                        runningTasks.remove(player);
                        return;
                    }
                    if(currentlyActivePackage != playerActiveQuest.getOrDefault(player, null).questPackage) {
                        this.cancel();
                        runningTasks.remove(player); // clean up
                        return;
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

                    PathFinder pathFinder = new PathFinder(playerLocation, location, 4000, true, 5);
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
