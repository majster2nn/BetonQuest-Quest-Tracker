package majster2nn.dev.betonQuestQT.Tracker.Menus;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.Tracker.Menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.Tracker.PathFinding.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SideQuestsMenu extends MultiPageInventoryGUI {
    public SideQuestsMenu(String invName) {
        super(invName);
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*6, Component.text(invName));
    }
    @Override
    public void decorate(Player player){
        setQuestButtons(player);
        for(int i = 0; i <= 8; i++) {
            this.addButton(i, -1, filler());
        }

        for(int i = 45; i <= 53; i++) {
            this.addButton(i,-1, filler());
        }

        if(pageMap.get(currentPage - 1) != null) {
            this.addButton(46, -1, scrollButtons(46));
        }
        if(pageMap.get(currentPage + 1) != null) {
            this.addButton(52, -1, scrollButtons(52));
        }
        this.addButton(48, -1, backButton());
        this.addButton(50, -1, filterButtons());

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
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();

                    switch(slot){
                        case 46:{
                            display = ButtonVisualsStorage.getButtonItem("previousPageButton", lang);
                            break;
                        }
                        case 52:{
                            display = ButtonVisualsStorage.getButtonItem("nextPageButton", lang);
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
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(p);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("backButton", lang);
                })
                .consumer(e -> {
                    Player player = (Player) e.getWhoClicked();
                    BetonQuestQT.getInstance().guiManager.openGui(new MainQuestMenu(BetonQuestQT.getInstance().getMenuTranslation("main-menu", player)), player);
                });
    }
    public void setQuestButtons(Player player){
        final int[] currentSlot = {9};
        final int[] currentPage = {1};
        QuestPlaceholder.packageByName
                //TODO ADD SORTING (.sorted)
                .forEach((id, questPackage) -> {
                    if (!questPackage.getTemplates().contains("trackedQuest")) {
                        return;
                    }

                    QuestPlaceholder questPlaceholder = QuestPlaceholder.getQuestPlaceholderFromPackage(questPackage, player);

                    if (!"side".equalsIgnoreCase(QuestPlaceholder.packagesByCategory.getOrDefault(questPackage, "none")) ||
                            Statuses.HIDDEN.equals(QuestPlaceholder.packageStatusesMap.getOrDefault(player, new HashMap<>()).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN)) ||
                            Statuses.FINISHED.equals(QuestPlaceholder.packageStatusesMap.getOrDefault(player, new HashMap<>()).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN)) ||
                            !new HashSet<>(QuestPlaceholder.packagesTags.getOrDefault(questPackage, List.of()))
                                    .containsAll(FilterMenu.playerFilters.getOrDefault(player, List.of()))) {
                        return;
                    }


                    this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                            .creator(x -> questPlaceholder.getQuestDisplay())
                            .consumer(e -> {
                                PlayerQuestTracker.setPlayerActiveQuest(player, questPlaceholder);
                                PlayerQuestTracker.activateQuestTracking(player);
                                e.setCancelled(true);
                            })
                    );
                    currentSlot[0]++;
                    if(currentSlot[0] == 45){
                        currentSlot[0] = 9;
                        currentPage[0]++;
                    }
                });
    }

    private InventoryButton filterButtons(){
        return new InventoryButton()
                .creator( p -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(p);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("filterButton", lang);
                })
                .consumer(e -> {
                    BetonQuestQT.getInstance().guiManager.openGui(new FilterMenu("Filters", "side"), (Player) e.getWhoClicked());
                    e.setCancelled(true);
                });
    }

}
