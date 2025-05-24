package majster2nn.dev.betonQuestQT.Tracker.BQEvents;

import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FinishQuest implements OnlineEvent {
    private QuestPackage questPackage;

    public FinishQuest(QuestPackage questPackage){
        this.questPackage = questPackage;
    }

    @Override
    public void execute(OnlineProfile onlineProfile) throws QuestException {
        Player player = onlineProfile.getPlayer();
        QuestPlaceholder.packageStatusesMap.computeIfAbsent(player, k -> new HashMap<>())
                .put(questPackage, QuestPlaceholder.Statuses.FINISHED);
    }
}
