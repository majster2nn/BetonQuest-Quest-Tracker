package majster2nn.dev.betonQuestQT.InventoryHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.HashMap;

public abstract class MultiPageInventoryGUI extends InventoryGUI{

    public int currentPage = 1;
    protected final HashMap<Integer, HashMap<Integer, InventoryButton>> pageMap = new HashMap<>();
    public HashMap<Integer, String> pageNameMap = new HashMap<>();

    public MultiPageInventoryGUI(String invName){
        this.name = invName;
        this.inventory = this.createInventory(name);
    }

    public void addButton(int slot, int page, InventoryButton button) {
        if(page==-1) page=currentPage;

        HashMap<Integer, InventoryButton> currentButtonMap = pageMap.getOrDefault(page, new HashMap<>());
        currentButtonMap.put(slot, button);

        this.pageMap.put(page, currentButtonMap);
    }

    @Override
    public void decorate(Player player) {
        this.inventory.clear();
        // Update button map for the current page
        this.buttonMap = pageMap.getOrDefault(currentPage, new HashMap<>());

        super.decorate(player);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        decorate((Player) event.getPlayer());
    }
}
