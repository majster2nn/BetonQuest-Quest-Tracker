package majster2nn.dev.betonQuestQT.hooks.betonquest.events;

import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FinishQuest implements OnlineEvent {
    private String id;
    private QuestPackage questPackage;

    public FinishQuest(String id, QuestPackage questPackage){
        this.id = id;
        this.questPackage = questPackage;
    }

    @Override
    public void execute(OnlineProfile onlineProfile) throws QuestException {
        Player player = onlineProfile.getPlayer();
        QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                .put(id, Statuses.FINISHED);
        QuestPlaceholder activeQuest = PlayerQuestTracker.getPlayerActiveQuest(player);
        if(activeQuest != null && activeQuest.questPackage == questPackage){
            PlayerQuestTracker.setPlayerActiveQuest(player, null);
        }
        PlayerDataManager.savePlayerData(player);
    }
}
