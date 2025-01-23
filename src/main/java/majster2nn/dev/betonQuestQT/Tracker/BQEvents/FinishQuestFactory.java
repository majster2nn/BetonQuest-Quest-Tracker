package majster2nn.dev.betonQuestQT.Tracker.BQEvents;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter;
import org.betonquest.betonquest.exceptions.InstructionParseException;

public class FinishQuestFactory implements EventFactory {

    private final BetonQuestLoggerFactory loggerFactory;

    public FinishQuestFactory(BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public Event parseEvent(Instruction instruction) throws InstructionParseException {
        final BetonQuestLogger log = loggerFactory.create(LockQuest.class);

        return new OnlineEventAdapter(new FinishQuest(
                instruction.getPackage()),
                log, instruction.getPackage()
        );
    }
}
