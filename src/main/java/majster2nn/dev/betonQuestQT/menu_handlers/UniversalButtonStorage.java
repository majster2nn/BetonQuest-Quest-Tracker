package majster2nn.dev.betonQuestQT.menu_handlers;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UniversalButtonStorage {
    public static InventoryButton nextPageButton(MultiPageInventoryGUI gui){
        return new InventoryButton()
                .creator(p -> {
                    ItemStack display = new ItemStack(Material.ARROW);
                    display.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Next Page"));
                    return display;
                })
                .consumer(e -> {
                    if(gui.buttonMap.get(gui.currentPage + 1) != null){
                        gui.currentPage+=1;
                    }
                    e.setCancelled(true);
                });
    }
}
