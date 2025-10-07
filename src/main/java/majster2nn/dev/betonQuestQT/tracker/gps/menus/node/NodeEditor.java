package majster2nn.dev.betonQuestQT.tracker.gps.menus.node;

import majster2nn.dev.betonQuestQT.menu_handlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.menu_handlers.UniversalButtonStorage;
import majster2nn.dev.betonQuestQT.tracker.gps.utils.LocationNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class NodeEditor extends MultiPageInventoryGUI {
    private LocationNode node;

    public NodeEditor(String invName, LocationNode node) {
        super(invName);
        this.node = node;
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 6*9, invName);
    }

    @Override
    public void decorate(Player player){
        if(buttonMap.get(currentPage + 1) != null){
            addButton(40, UniversalButtonStorage.nextPageButton(this));
        }
        super.decorate(player);
    }


}
