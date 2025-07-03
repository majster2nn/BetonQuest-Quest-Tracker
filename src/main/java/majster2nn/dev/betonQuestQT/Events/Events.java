package majster2nn.dev.betonQuestQT.Events;

import majster2nn.dev.betonQuestQT.Database.DataBaseManager;
import majster2nn.dev.betonQuestQT.Tracker.PathFinding.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
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

        BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                    .put(questPackage.getQuestPath(), statusesMap.getOrDefault(id, Statuses.HIDDEN));
        });

        String activeQuest = DataBaseManager.getValueOfCellInUserTable("currentlyActiveQuest", player);
        if(!activeQuest.isBlank() && !activeQuest.isEmpty()){
            PlayerQuestTracker.setPlayerActiveQuest(player, QuestPlaceholder.getQuestPlaceholderFromPackage(BetonQuest.getInstance().getPackages().get(activeQuest), player));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
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
        QuestPlaceholder.packageStatusesMap.remove(player);
        PlayerQuestTracker.setPlayerActiveQuest(player, null);

    }
}
