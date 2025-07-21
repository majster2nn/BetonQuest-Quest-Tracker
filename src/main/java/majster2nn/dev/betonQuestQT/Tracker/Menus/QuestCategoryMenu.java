package majster2nn.dev.betonQuestQT.Tracker.Menus;

import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import org.bukkit.inventory.Inventory;

public class QuestCategoryMenu extends MultiPageInventoryGUI {
    public QuestCategoryMenu(String invName) {
        super(invName);
    }

    @Override
    protected Inventory createInventory(String invName) {
        return null;
    }
}
//TODO create a menu skeleton that can be dynamicaly changed from files, aka it will take inputs for inv layout and get corresponding buttons