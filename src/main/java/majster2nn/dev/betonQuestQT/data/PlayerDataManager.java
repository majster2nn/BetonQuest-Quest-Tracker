package majster2nn.dev.betonQuestQT.data;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.data.asyncSaver.Record;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataManager {
    public static void savePlayerData(Player player) {
        Map<String, List<String>> statusesMap = new HashMap<>() {{
            put("activeQuests", new ArrayList<>());
            put("lockedQuests", new ArrayList<>());
            put("finishedQuests", new ArrayList<>());
        }};

        BetonQuest.getInstance().getQuestPackageManager().getPackages().forEach((id, questPackage) -> {
            if (!questPackage.getTemplates().contains("trackedQuest")) {
                return;
            }

            Statuses status = QuestPlaceholder.packageStatusesMap.get(player).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN);
            switch (status) {
                case ACTIVE -> statusesMap.computeIfAbsent("activeQuests", x -> new ArrayList<>()).add(id);
                case LOCKED -> statusesMap.computeIfAbsent("lockedQuests", x -> new ArrayList<>()).add(id);
                case FINISHED -> statusesMap.computeIfAbsent("finishedQuests", x -> new ArrayList<>()).add(id);
            }
        });

        BetonQuestQT plugin = BetonQuestQT.getInstance();

        for (String key : statusesMap.keySet()) {
            plugin.savePlayerDataThread.addRecordToQueue(new Record(
                            player.getUniqueId().toString(),
                            String.join(",", statusesMap.getOrDefault(key, new ArrayList<>())),
                            key
                    )
            );
        }

        plugin.savePlayerDataThread.addRecordToQueue(new Record(
                        player.getUniqueId().toString(),
                        player.getName(),
                        "username"
                )
        );

        if (PlayerQuestTracker.getPlayerActiveQuest(player) != null) {
            plugin.savePlayerDataThread.addRecordToQueue(new Record(
                    player.getUniqueId().toString(),
                    PlayerQuestTracker.getPlayerActiveQuest(player).questPackage.getQuestPath(),
                    "currentlyActiveQuest"
            ));
        } else {
            plugin.savePlayerDataThread.addRecordToQueue(new Record(
                    player.getUniqueId().toString(),
                    " ",
                    "currentlyActiveQuest"
            ));
        }
    }
}
