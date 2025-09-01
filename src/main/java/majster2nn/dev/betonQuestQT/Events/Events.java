package majster2nn.dev.betonQuestQT.Events;

import majster2nn.dev.betonQuestQT.data.DataBaseManager;
import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import majster2nn.dev.betonQuestQT.tracker.PathFinding.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class Events implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(e.getPlayer());
        PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
        if(playerData.getLanguage().isEmpty() ||
           playerData.getLanguage().get().equals("default")){
            playerData.setLanguage(BetonQuest.getInstance().getDefaultLanguage());
            BetonQuest.getInstance().getPlayerDataStorage().put(profile, playerData);
        }
        
        Player player = e.getPlayer();
        Map<String, Statuses> statusesMap = new HashMap<>();
        for(String key : DataBaseManager.getValueOfCellInUserTable("activeQuests", player).split(",")){
            statusesMap.put(key, Statuses.ACTIVE);
        }
        for(String key : DataBaseManager.getValueOfCellInUserTable("lockedQuests", player).split(",")){
            statusesMap.put(key, Statuses.LOCKED);
        }
        for(String key : DataBaseManager.getValueOfCellInUserTable("finishedQuests", player).split(",")){
            statusesMap.put(key, Statuses.FINISHED);
        }

        BetonQuest.getInstance().getQuestPackageManager().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                    .put(questPackage.getQuestPath(), statusesMap.getOrDefault(id, Statuses.HIDDEN));
        });

        String activeQuest = DataBaseManager.getValueOfCellInUserTable("currentlyActiveQuest", player);
        if(!activeQuest.isBlank() && !activeQuest.isEmpty()){
            PlayerQuestTracker.setPlayerActiveQuest(player, QuestPlaceholder.getQuestPlaceholderFromPackage(BetonQuest.getInstance().getQuestPackageManager().getPackages().get(activeQuest), player));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
        PlayerDataManager.savePlayerData(player);

        QuestPlaceholder.packageStatusesMap.remove(player);
        PlayerQuestTracker.setPlayerActiveQuest(player, null);

    }
}
