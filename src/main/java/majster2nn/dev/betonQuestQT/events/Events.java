package majster2nn.dev.betonQuestQT.events;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
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
        if(playerData.getLanguage().isPresent() && playerData.getLanguage().get().contains("Optional")){
            playerData.setLanguage(BetonQuest.getInstance().getDefaultLanguage());
            BetonQuest.getInstance().getPlayerDataStorage().put(profile, playerData);
        }
        
        Player player = e.getPlayer();
        Map<String, Statuses> statusesMap = new HashMap<>();
        BetonQuestQT plugin = BetonQuestQT.getInstance();
        for(String key : ((String)plugin.dataBaseHandler.getFromDb("userData", "activeQuests", "UUID", player.getUniqueId().toString())).split(",")){
            statusesMap.put(key, Statuses.ACTIVE);
        }
        for(String key : ((String)plugin.dataBaseHandler.getFromDb("userData", "lockedQuests", "UUID", player.getUniqueId().toString())).split(",")){
            statusesMap.put(key, Statuses.LOCKED);
        }
        for(String key : ((String)plugin.dataBaseHandler.getFromDb("userData", "finishedQuests", "UUID", player.getUniqueId().toString())).split(",")){
            statusesMap.put(key, Statuses.FINISHED);
        }

        BetonQuest.getInstance().getQuestPackageManager().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                    .put(questPackage.getQuestPath(), statusesMap.getOrDefault(id, Statuses.HIDDEN));
        });

        String activeQuest = ((String)plugin.dataBaseHandler.getFromDb("userData", "currentlyActiveQuest", "UUID", player.getUniqueId().toString()));
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
