package majster2nn.dev.betonQuestQT.menu_handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class InventoryGUI implements InventoryHandler{
    protected Inventory inventory;
    public String name;
    protected Map<Integer, InventoryButton> buttonMap = new HashMap<>();

    public InventoryGUI(String invName) {
        this.name = invName;
        this.inventory = this.createInventory(name);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void addButton(int slot, InventoryButton button) {
        this.buttonMap.put(slot, button);
    }
    public void remButton(int slot, InventoryButton button){
        this.buttonMap.remove(slot, button);
    }

    public void decorate(Player player) {
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = button.getIconCreator().apply(player);
            this.inventory.setItem(slot, icon);
        });
    }

    @Override
    public void onDrag(InventoryDragEvent e) {

    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        InventoryButton button = this.buttonMap.get(slot);
        if (button != null) {
            button.getEventConsumer().accept(event);
            decorate((Player) event.getWhoClicked());
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        decorate((Player) event.getPlayer());
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
    }

    protected abstract Inventory createInventory(String invName);

}
