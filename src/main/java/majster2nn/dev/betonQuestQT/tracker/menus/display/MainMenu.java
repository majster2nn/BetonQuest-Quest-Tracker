package majster2nn.dev.betonQuestQT.tracker.menus.display;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.menu_handlers.InventoryButton;
import majster2nn.dev.betonQuestQT.menu_handlers.InventoryGUI;
import majster2nn.dev.betonQuestQT.tracker.menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.tracker.menus.layouts.ButtonLayoutContainer;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainMenu extends InventoryGUI {
    public MainMenu(String invName) {
        super(invName);
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*6, Component.text(invName));
    }

    @Override
    public void decorate(Player player){
        ButtonLayoutContainer.getMainMenuLayout().forEach((key, value) -> {
            addButton(key, buttonSkeleton(value));
        });

        super.decorate(player);
    }

    private InventoryButton buttonSkeleton(String buttonVisualName){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem(buttonVisualName, lang).clone();
                })
                .consumer(event -> {
                    if(ButtonVisualsStorage.getButtonEvents(buttonVisualName) != null){
                        HashMap<String, String> eventMap = new HashMap<>();
                        for(String eventString : ButtonVisualsStorage.getButtonEvents(buttonVisualName).split(";")){
                            List<String> eventRaw = new ArrayList<>(Arrays.asList(eventString.split(":", 2)));
                            eventRaw.add(" ");
                            eventMap.put(eventRaw.getFirst(), eventRaw.get(1));
                        }

                        if(eventMap.containsKey("openMenu")){
                            BetonQuestQT.getInstance().guiManager.openGui(
                                    new QuestMenus(
                                            BetonQuestQT.getInstance().getMenuTranslation(
                                                    "header-" + eventMap.get("openMenu"),
                                                    (Player) event.getWhoClicked()
                                            ),
                                            eventMap.get("openMenu")
                                    ),
                                    (Player) event.getWhoClicked()
                            );
                        }
                    }
                    event.setCancelled(true);
                });
    }
}
