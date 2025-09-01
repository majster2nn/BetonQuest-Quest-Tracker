package majster2nn.dev.betonQuestQT.tracker.BQEvents;

import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ActiveQuest implements OnlineEvent {
    private String id;

    public ActiveQuest(String id){
        this.id = id;
    }


    @Override
    public void execute(OnlineProfile onlineProfile) throws QuestException {
        Player player = onlineProfile.getPlayer();
        QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                .put(id, Statuses.ACTIVE);

        PlayerDataManager.savePlayerData(player);
    }
}
