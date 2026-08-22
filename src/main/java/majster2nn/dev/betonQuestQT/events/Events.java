package majster2nn.dev.betonQuestQT.events;

import majster2nn.dev.betonQuestQT.menu_handlers.InventoryGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class Events implements Listener {
    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent e){
        if(e.getItem().getItemMeta().getPersistentDataContainer().has(InventoryGUI.buttonKey)){
            e.getSource().remove(e.getItem());
            e.getDestination().remove(e.getItem());
            e.setCancelled(true);
        }
    }
}
