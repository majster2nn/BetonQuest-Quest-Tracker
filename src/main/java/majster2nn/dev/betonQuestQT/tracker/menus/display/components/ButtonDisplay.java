package majster2nn.dev.betonQuestQT.tracker.menus.display.components;

import lombok.Getter;
import lombok.Setter;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.identifier.ItemIdentifier;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ButtonDisplay extends AbstractDisplay {
    @Getter
    @Setter
    protected Map<String, String> langLores;

    protected ButtonDisplay(String bqItemReg, Map<String, String> langNames) {
        super(bqItemReg, langNames);
    }

    @Override
    public ItemStack getDisplay(Profile profile) {
        try {
            BetonQuestApi bqApi = BetonQuestQT.getInstance().getBetonQuestApi();

            QuestPackage questPackage = bqApi.packages().getPackage("");

            if(questPackage == null)
                throw new QuestException("Config package not found!");

            ItemIdentifier identifier = bqApi.instructions()
                    .createForArgument(questPackage, bqItemReg)
                    .identifier(ItemIdentifier.class)
                    .get().getValue(profile);

            ItemStack display = bqApi.items().manager().getItem(profile, identifier).generate(1);

            return display;
        } catch (QuestException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actionOnClick() {

    }
}
