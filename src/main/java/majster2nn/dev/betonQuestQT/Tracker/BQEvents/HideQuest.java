package majster2nn.dev.betonQuestQT.Tracker.BQEvents;

import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class HideQuest implements OnlineEvent {
    private final String id;

    public HideQuest(String id){
        this.id = id;
    }

    @Override
    public void execute(OnlineProfile onlineProfile) throws QuestException {
        Player player = onlineProfile.getPlayer();
        QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, x -> new HashMap<>())
                .put(id, Statuses.HIDDEN);
    }
}
