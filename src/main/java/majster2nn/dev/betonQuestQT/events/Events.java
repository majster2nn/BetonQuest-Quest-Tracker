package majster2nn.dev.betonQuestQT.events;

import majster2nn.dev.betonQuestQT.menu_handlers.InventoryGUI;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();

        PlayerQuestTracker.setPlayerActiveQuest(player, null);
    }

    @EventHandler
    public void onItemMoveBetweenContainers(InventoryMoveItemEvent e){
        if(e.getItem().getItemMeta().getPersistentDataContainer().has(InventoryGUI.buttonKey)){
            e.getSource().remove(e.getItem());
            e.getDestination().remove(e.getItem());
            e.setCancelled(true);
        }
    }
}
