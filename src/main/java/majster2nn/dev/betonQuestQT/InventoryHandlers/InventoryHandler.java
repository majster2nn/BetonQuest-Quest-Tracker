package majster2nn.dev.betonQuestQT.InventoryHandlers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface InventoryHandler {
    void onDrag(InventoryDragEvent event);
    void onClick(InventoryClickEvent event);
    void onOpen(InventoryOpenEvent event);
    void onClose(InventoryCloseEvent event);
}
