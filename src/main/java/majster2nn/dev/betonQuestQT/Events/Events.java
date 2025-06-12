package majster2nn.dev.betonQuestQT.Events;

import majster2nn.dev.betonQuestQT.Database.DataBaseManager;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Map<String, QuestPlaceholder.Statuses> statusesMap = new HashMap<>();
        for(String key : DataBaseManager.getValueOfCellInUserTable("activeQuests", player).split(",")){
            statusesMap.put(key, QuestPlaceholder.Statuses.ACTIVE);
        }
        for(String key : DataBaseManager.getValueOfCellInUserTable("lockedQuests", player).split(",")){
            statusesMap.put(key, QuestPlaceholder.Statuses.LOCKED);
        }
        for(String key : DataBaseManager.getValueOfCellInUserTable("finishedQuests", player).split(",")){
            statusesMap.put(key, QuestPlaceholder.Statuses.FINISHED);
        }

        BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, k -> new HashMap<>())
                    .put(questPackage, statusesMap.getOrDefault(id, QuestPlaceholder.Statuses.HIDDEN));
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
        Map<String, List<String>> statusesMap = new HashMap<>();

        BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}

            QuestPlaceholder.Statuses status = QuestPlaceholder.packageStatusesMap.get(player).getOrDefault(questPackage, QuestPlaceholder.Statuses.LOCKED);
            switch (status) {
                case ACTIVE -> statusesMap.computeIfAbsent("activeQuests", _ -> new ArrayList<>()).add(id);
                case LOCKED -> statusesMap.computeIfAbsent("lockedQuests", _ -> new ArrayList<>()).add(id);
                case FINISHED -> statusesMap.computeIfAbsent("finishedQuests", _ -> new ArrayList<>()).add(id);
            }
        });

        for(String key : statusesMap.keySet()){
            DataBaseManager.addColumnValueToUserTable(key, "", player);
            DataBaseManager.addColumnValueToUserTable(key, String.join(",", statusesMap.getOrDefault(key, new ArrayList<>())), player);
        }
    }
}
