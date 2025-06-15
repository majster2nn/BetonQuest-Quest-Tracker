package majster2nn.dev.betonQuestQT.Tracker.BQEvents;

import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory;
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter;
import org.betonquest.betonquest.instruction.Instruction;

public class ActiveQuestFactory implements PlayerEventFactory {

    private final BetonQuestLoggerFactory loggerFactory;

    public ActiveQuestFactory(BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerEvent parsePlayer(Instruction instruction) throws QuestException {
        final BetonQuestLogger log = loggerFactory.create(LockQuest.class);
        return new OnlineEventAdapter(new ActiveQuest(
                instruction.getPackage().getQuestPath()),
                log, instruction.getPackage()
        );
    }
}
