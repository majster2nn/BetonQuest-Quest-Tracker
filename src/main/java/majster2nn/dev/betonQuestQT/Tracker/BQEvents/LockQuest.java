package majster2nn.dev.betonQuestQT.Tracker.BQEvents;

import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LockQuest implements OnlineEvent {
    private QuestPackage questPackage;

    public LockQuest(QuestPackage questPackage){
        this.questPackage = questPackage;
    }

    @Override
    public void execute(OnlineProfile onlineProfile) throws QuestRuntimeException {
        Player player = onlineProfile.getPlayer();
        QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, k -> new HashMap<>())
                .put(questPackage, QuestPlaceholder.Statuses.LOCKED);
    }
}
