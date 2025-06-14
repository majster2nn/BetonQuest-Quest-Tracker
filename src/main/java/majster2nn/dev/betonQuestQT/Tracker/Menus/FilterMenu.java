package majster2nn.dev.betonQuestQT.Tracker.Menus;

import io.papermc.paper.datacomponent.DataComponentTypes;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsHandlers;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getLeftScrollButton;
import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getRightScrollButton;

public class FilterMenu extends MultiPageInventoryGUI {
    private final String whichMenu;
    public static HashMap<Player, List<String>> playerFilters = new HashMap<>();

    public FilterMenu(String invName, String whichMenu) {
        super(invName);
        this.whichMenu = whichMenu;
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*6, Component.text(invName));
    }

    @Override
    public void decorate(Player player){
        setFilterButtons(player);
        for(int i = 0; i <= 8; i++) {
            this.addButton(i, -1, filler());
        }

        for(int i = 45; i <= 53; i++) {
            this.addButton(i,-1, filler());
        }

        this.addButton(46, -1, scrollButtons(46));
        this.addButton(52, -1, scrollButtons(52));
        this.addButton(48, -1, backButton());
        this.addButton(50, -1, resetFilters());

        super.decorate(player);
    }

    public InventoryButton filler(){
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                .consumer(e-> e.setCancelled(true));
    }

    public InventoryButton scrollButtons(int slot){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display;
                    switch(slot){
                        case 46:{
                            display = new ItemStack(HeadsHandlers.getHead(getLeftScrollButton()));
                            ItemMeta meta = display.getItemMeta();
                            String label = BetonQuestQT.getInstance().getTranslation("prevP", player);
                            if (label == null || label.isEmpty()) label = "←";
                            meta.displayName(Component
                                    .text(label)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                            display.setItemMeta(meta);
                            break;
                        }
                        case 52:{
                            display = new ItemStack(HeadsHandlers.getHead(getRightScrollButton()));
                            ItemMeta meta = display.getItemMeta();
                            String label = BetonQuestQT.getInstance().getTranslation("nextP", player);
                            if (label == null || label.isEmpty()) label = "→";
                            meta.displayName(Component
                                    .text(label)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                            display.setItemMeta(meta);
                            break;
                        }
                        default:{
                            display = new ItemStack(Material.DIRT);
                        }
                    }
                    return display;
                })
                .consumer(e -> {
                    if(slot == 46){
                        if(pageMap.get(currentPage - 1) != null) {
                            currentPage--;
                        }
                    }
                    if(slot == 52){
                        if(pageMap.get(currentPage + 1) != null){
                            currentPage++;
                        }
                    }
                    e.setCancelled(true);
                });
    }
    private InventoryButton backButton(){
        return new InventoryButton()
                .creator(p -> {
                    ItemStack display = new ItemStack(Material.ARROW);
                    display.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Back")
                            .decoration(TextDecoration.ITALIC, false));
                    return display;
                })
                .consumer(e -> {
                    Player player = (Player) e.getWhoClicked();
                    switch(whichMenu){
                        case "finished" -> BetonQuestQT.getInstance().guiManager.openGui(new FinishedQuestsMenu(BetonQuestQT.getInstance().getTranslation("header-finished", player)), player);
                        case "side" -> BetonQuestQT.getInstance().guiManager.openGui(new SideQuestsMenu(BetonQuestQT.getInstance().getTranslation("header-side", player)), player);
                        case "main" -> BetonQuestQT.getInstance().guiManager.openGui(new MainQuestsMenu(BetonQuestQT.getInstance().getTranslation("header-main", player)), player);
                        case "other" -> BetonQuestQT.getInstance().guiManager.openGui(new OtherQuestsMenu(BetonQuestQT.getInstance().getTranslation("header-other", player)), player);
                    }

                });
    }

    private void setFilterButtons(Player player){
        final int[] currentSlot = {9};
        final int[] currentPage = {1};

        for(String tag : QuestPlaceholder.tags){


            this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                    .creator(x -> {
                        ItemStack display;
                        if(playerFilters.getOrDefault(player, new ArrayList<>()).contains(tag)){
                            display = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                            display.setData(DataComponentTypes.CUSTOM_NAME,Component.text(tag).append(Component.text(" Active", NamedTextColor.GREEN))
                                    .decoration(TextDecoration.ITALIC, false));
                        }else{
                            display = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                            display.setData(DataComponentTypes.CUSTOM_NAME,Component.text(tag).append(Component.text(" Disabled", NamedTextColor.RED))
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                        return display;
                    })
                    .consumer(e -> {
                        if(!playerFilters.getOrDefault(player, new ArrayList<>()).contains(tag)) {
                            playerFilters.computeIfAbsent(player, p -> new ArrayList<>()).add(tag);
                        }else{
                            if(playerFilters.getOrDefault(player, new ArrayList<>()).contains(tag)) playerFilters.get(player).remove(tag);
                        }
                        decorate(player);
                        e.setCancelled(true);
                    })
            );
            currentSlot[0]++;
            if(currentSlot[0] == 45){
                currentSlot[0] = 9;
                currentPage[0]++;
            }
        }
    }
    private InventoryButton resetFilters(){
        return new InventoryButton()
                .creator(player -> {
                    ItemStack display = new ItemStack(Material.BARRIER);
                    display.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Reset", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    return display;
                })
                .consumer(e -> {
                    resetFilters((Player) e.getWhoClicked());
                    decorate((Player) e.getWhoClicked());
                    e.setCancelled(true);
                });
    }

    public static void resetFilters(Player player){
        playerFilters.remove(player);
    }
}
