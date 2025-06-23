package majster2nn.dev.betonQuestQT;

import majster2nn.dev.betonQuestQT.Tracker.PathFinding.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUpdater extends BukkitRunnable {
    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()){
            if(PlayerQuestTracker.getPlayerActiveQuest(player) != null){
                if(!PlayerQuestTracker.getPlayerActiveQuest(player).status.equals(Statuses.ACTIVE)){
                    PlayerQuestTracker.setPlayerActiveQuest(player, null);
                }else {
                    PlayerQuestTracker.getPlayerActiveQuest(player).update(player);
                    PlayerQuestTracker.activateQuestTracking(player);
                }
            }
        }
    }
}
