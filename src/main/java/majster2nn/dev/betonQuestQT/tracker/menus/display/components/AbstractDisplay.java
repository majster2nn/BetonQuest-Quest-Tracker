package majster2nn.dev.betonQuestQT.tracker.menus.display.components;

import lombok.Getter;
import lombok.Setter;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class AbstractDisplay {
    @Getter @Setter
    protected String bqItemReg;
    @Getter @Setter
    protected Map<String, String> langNames;

    public abstract ItemStack getDisplay(Profile profile);

    public abstract void actionOnClick();

    protected AbstractDisplay(String bqItemReg, Map<String, String> langNames) {
        this.bqItemReg = bqItemReg;
        this.langNames = langNames;
    }

}
