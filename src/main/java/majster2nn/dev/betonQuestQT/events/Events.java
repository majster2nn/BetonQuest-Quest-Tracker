package majster2nn.dev.betonQuestQT.events;

import majster2nn.dev.betonQuestQT.menu_handlers.InventoryGUI;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(e.getPlayer());
        PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
        if(playerData.getLanguage().isPresent() && playerData.getLanguage().get().contains("Optional")){
            playerData.setLanguage(BetonQuest.getInstance().getDefaultLanguage());
            BetonQuest.getInstance().getPlayerDataStorage().put(profile, playerData);
        }
    }

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
