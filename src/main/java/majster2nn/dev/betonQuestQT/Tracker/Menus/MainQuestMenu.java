package majster2nn.dev.betonQuestQT.Tracker.Menus;

import io.papermc.paper.datacomponent.DataComponentTypes;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainQuestMenu extends InventoryGUI {
    private final BetonQuestQT plugin = BetonQuestQT.getInstance();

    public MainQuestMenu(String invName) {
        super(invName);
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*6, Component.text(invName));
    }

    @Override
    public void decorate(Player player){
        addButton(19, mainQuestsButton());
        addButton(21, sideQuestsButton());
        addButton(23, otherQuestsButton());
        addButton(25, finishedQuestsButton());

        FilterMenu.resetFilters(player);

        super.decorate(player);
    }

    private InventoryButton mainQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display = new ItemStack(Material.GOLD_INGOT);
                    display.setData(DataComponentTypes.ITEM_NAME, Component.text("Main Quests"));
                    return display;
                })
                .consumer(e -> {
                    if(e.getSlot() == 19){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new MainQuestsMenu(plugin.getTranslation("header-main", player)), player);
                    }
                   e.setCancelled(true);
                });
    }

    private InventoryButton sideQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display = new ItemStack(Material.IRON_INGOT);
                    display.setData(DataComponentTypes.ITEM_NAME, Component.text("Side Quests"));
                    return display;
                })
                .consumer(e -> {
                    if(e.getSlot() == 21){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new SideQuestsMenu(plugin.getTranslation("header-side", player)), player);
                    }
                    e.setCancelled(true);
                });
    }

    private InventoryButton otherQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display = new ItemStack(Material.PAPER);
                    display.setData(DataComponentTypes.ITEM_NAME, Component.text("Other Quests"));
                    return display;
                })
                .consumer(e -> {
                    if(e.getSlot() == 23){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new OtherQuestsMenu(plugin.getTranslation("header-other", player)), player);
                    }
                    e.setCancelled(true);
                });
    }

    private InventoryButton finishedQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display = new ItemStack(Material.WRITABLE_BOOK);
                    display.setData(DataComponentTypes.ITEM_NAME, Component.text("Finished Quests"));
                    return display;
                })
                .consumer(e -> {
                    if(e.getSlot() == 25){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new FinishedQuestsMenu(plugin.getTranslation("header-finished", player)), player);
                    }
                    e.setCancelled(true);
                });
    }
}
