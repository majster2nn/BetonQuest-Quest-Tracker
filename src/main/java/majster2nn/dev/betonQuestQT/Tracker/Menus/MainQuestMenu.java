package majster2nn.dev.betonQuestQT.Tracker.Menus;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryGUI;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@SuppressWarnings("UnstableApiUsage")
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

        addButton(ButtonVisualsStorage.getButtonSlot("mainQuestButton"), mainQuestsButton());
        addButton(ButtonVisualsStorage.getButtonSlot("sideQuestButton"), sideQuestsButton());
        addButton(ButtonVisualsStorage.getButtonSlot("otherQuestButton"), otherQuestsButton());
        addButton(ButtonVisualsStorage.getButtonSlot("finishedQuestButton"), finishedQuestsButton());

        FilterMenu.resetFilters(player);

        super.decorate(player);
    }

    private InventoryButton mainQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("mainQuestButton", lang);
                })
                .consumer(e -> {
                    if(e.getSlot() == ButtonVisualsStorage.getButtonSlot("mainQuestButton")){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new MainQuestsMenu(plugin.getMenuTranslation("header-main", player)), player);
                    }
                   e.setCancelled(true);
                });
    }

    private InventoryButton sideQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("sideQuestButton", lang);
                })
                .consumer(e -> {
                    if(e.getSlot() == ButtonVisualsStorage.getButtonSlot("sideQuestButton")){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new SideQuestsMenu(plugin.getMenuTranslation("header-side", player)), player);
                    }
                    e.setCancelled(true);
                });
    }

    private InventoryButton otherQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("otherQuestButton", lang);
                })
                .consumer(e -> {
                    if(e.getSlot() == ButtonVisualsStorage.getButtonSlot("otherQuestButton")){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new OtherQuestsMenu(plugin.getMenuTranslation("header-other", player)), player);
                    }
                    e.setCancelled(true);
                });
    }

    private InventoryButton finishedQuestsButton(){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem("finishedQuestButton", lang);
                })
                .consumer(e -> {
                    if(e.getSlot() == ButtonVisualsStorage.getButtonSlot("finishedQuestButton")){
                        Player player = (Player) e.getWhoClicked();
                        plugin.guiManager.openGui(new FinishedQuestsMenu(plugin.getMenuTranslation("header-finished", player)), player);
                    }
                    e.setCancelled(true);
                });
    }
}
