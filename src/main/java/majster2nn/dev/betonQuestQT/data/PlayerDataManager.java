package majster2nn.dev.betonQuestQT.data;

import majster2nn.dev.betonQuestQT.Tracker.PathFinding.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataManager {
    public static void savePlayerData(Player player){
        Map<String, List<String>> statusesMap = new HashMap<>(){{
            put("activeQuests", new ArrayList<>());
            put("lockedQuests", new ArrayList<>());
            put("finishedQuests", new ArrayList<>());
        }};

        BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}

            Statuses status = QuestPlaceholder.packageStatusesMap.get(player).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN);
            switch (status) {
                case ACTIVE -> statusesMap.computeIfAbsent("activeQuests", x -> new ArrayList<>()).add(id);
                case LOCKED -> statusesMap.computeIfAbsent("lockedQuests", x -> new ArrayList<>()).add(id);
                case FINISHED -> statusesMap.computeIfAbsent("finishedQuests", x -> new ArrayList<>()).add(id);
            }
        });

        for(String key : statusesMap.keySet()){
            DataBaseManager.addColumnValueToUserTable(key, String.join(",", statusesMap.getOrDefault(key, new ArrayList<>())), player);
        }

        DataBaseManager.addColumnValueToUserTable("username", player.getName(), player);

        if(PlayerQuestTracker.getPlayerActiveQuest(player) != null) {
            DataBaseManager.addColumnValueToUserTable("currentlyActiveQuest", PlayerQuestTracker.getPlayerActiveQuest(player).questPackage.getQuestPath(), player);
        }else{
            DataBaseManager.addColumnValueToUserTable("currentlyActiveQuest", " ", player);
        }
    }
}
