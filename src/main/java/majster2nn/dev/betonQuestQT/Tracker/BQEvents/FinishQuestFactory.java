package majster2nn.dev.betonQuestQT.tracker.BQEvents;

import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory;
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter;

public class FinishQuestFactory implements PlayerEventFactory {

    private final BetonQuestLoggerFactory loggerFactory;

    public FinishQuestFactory(BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerEvent parsePlayer(Instruction instruction) throws QuestException {
        final BetonQuestLogger log = loggerFactory.create(FinishQuest.class);

        return new OnlineEventAdapter(new FinishQuest(
                instruction.getPackage().getQuestPath(), instruction.getPackage()),
                log, instruction.getPackage()
        );
    }
}
