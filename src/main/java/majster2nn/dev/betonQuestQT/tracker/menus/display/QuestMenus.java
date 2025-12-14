package majster2nn.dev.betonQuestQT.tracker.menus.display;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Utils;
import majster2nn.dev.betonQuestQT.menu_handlers.InventoryButton;
import majster2nn.dev.betonQuestQT.menu_handlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.menus.FilterMenu;
import majster2nn.dev.betonQuestQT.tracker.menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.tracker.menus.layouts.ButtonLayoutContainer;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class QuestMenus extends MultiPageInventoryGUI {
    String questType;
    List<Integer> placeholderSlots = new ArrayList<>();
    List<QuestPlaceholder> filteredQuests = new ArrayList<>();
    int amountOfPages = 1;

    public QuestMenus(String invName, String questType) {
        super(invName, BetonQuestQT.getInstance().configData.getInt("settings.amountOfRowsInQuestMenus", 6));
        this.questType = questType;
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*amountOfRows, Component.text(invName));
    }

    @Override
    public void decorate(Player player){
        ButtonLayoutContainer.getQuestCategoriesMenus().entrySet().stream()
                .filter((key) -> key.getKey() < amountOfRows * 9)
                .forEach((entry) -> addButton(entry.getKey(), -1, buttonSkeleton(entry.getValue())));

        setAllQuestButtons(player);
        super.decorate(player);
    }

    public void setAllQuestButtons(Player player) {
        placeholderSlots = ButtonLayoutContainer.getQuestCategoriesMenus().entrySet().stream()
                .filter(entry -> !ButtonVisualsStorage.checkIfButtonExists(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        filteredQuests = QuestPlaceholder.packageByName.values().stream()
                .map(questPackage -> QuestPlaceholder.getQuestPlaceholderFromPackage(questPackage, player))
                .filter(q -> {
                    String category = QuestPlaceholder.packagesByCategory.getOrDefault(q.questPackage, "none");

                    if ("finished".equalsIgnoreCase(questType)) {
                        return Utils.checkBqConditions(q.questPackage, "questParameters.statuses.finished", BetonQuest.getInstance().getProfileProvider().getProfile(player.getUniqueId()));
                    }

                    return questType.equalsIgnoreCase(category) &&
                            (Utils.checkBqConditions(
                                    q.questPackage,
                                    "questParameters.statuses.active",
                                    BetonQuest.getInstance().getProfileProvider().getProfile(player.getUniqueId()))
                            );
                })
                .filter(q -> {
                    List<String> tags = QuestPlaceholder.packagesTags.getOrDefault(q.questPackage, List.of());
                    return new HashSet<>(tags).containsAll(FilterMenu.playerFilters.getOrDefault(player, List.of()));
                })
                .toList();



        int perPage = placeholderSlots.size();
        amountOfPages = (int) Math.ceil(1.0*filteredQuests.size()/perPage);
        int start = (currentPage - 1) * perPage;
        int end = Math.min(start + perPage, filteredQuests.size());
        List<QuestPlaceholder> pageQuests = filteredQuests.subList(start, end);

        // Apply each quest to a slot
        for (int i = 0; i < pageQuests.size(); i++) {
            try {
                setQuestButton(player, placeholderSlots.get(i), currentPage, pageQuests.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void setQuestButton(Player player, int slot, int currentPage, QuestPlaceholder questPlaceholder) {
        if(BetonQuestQT.debug) {
            System.out.println("ADDED QUEST BUTTON FOR " + questPlaceholder.name + " to slot " + slot + " on page " + currentPage);
        }
        this.addButton(slot, currentPage, new InventoryButton()
                .creator(x -> questPlaceholder.getQuestDisplay())
                .consumer(e -> {

//                        PlayerQuestTracker.setPlayerActiveQuest(player, questPlaceholder);
//                        PlayerQuestTracker.activateQuestTracking(player);

                    e.setCancelled(true);
                }));
    }


    private InventoryButton buttonSkeleton(String buttonVisualName) {
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
                    String lang = playerData.getLanguage().isPresent() ? playerData.getLanguage().get() : BetonQuest.getInstance().getDefaultLanguage();
                    return ButtonVisualsStorage.getButtonItem(buttonVisualName, lang).clone();
                })
                .consumer(event -> {
                    if (ButtonVisualsStorage.getButtonEvents(buttonVisualName) != null) {
                        HashMap<String, String> eventMap = new HashMap<>();
                        for (String eventString : ButtonVisualsStorage.getButtonEvents(buttonVisualName).split(";")) {
                            List<String> eventRaw = new ArrayList<>(Arrays.asList(eventString.split(":", 2)));
                            eventRaw.add(" ");
                            eventMap.put(eventRaw.getFirst(), eventRaw.get(1));
                        }

                        if (eventMap.containsKey("return")) {
                            BetonQuestQT.getInstance().guiManager.openGui(
                                    new MainMenu(BetonQuestQT.getInstance().getMenuTranslation("main-menu", (Player) event.getWhoClicked())),
                                    (Player) event.getWhoClicked()
                            );
                        }

                        if (eventMap.containsKey("filter")) {
                            BetonQuestQT.getInstance().guiManager.openGui(
                                    new FilterMenu(BetonQuestQT.getInstance().getMenuTranslation("header-filters", (Player) event.getWhoClicked()), questType),
                                    (Player) event.getWhoClicked()
                            );
                        }

                        if (eventMap.containsKey("prevPage")) {
                            if (currentPage - 1 > 0) {
                                currentPage--;
                                decorate((Player) event.getWhoClicked());
                            }
                        }

                        if (eventMap.containsKey("nextPage")) {
                            if (currentPage + 1 <= amountOfPages) {
                                currentPage++;
                                decorate((Player) event.getWhoClicked());
                            }
                        }
                    }
                    event.setCancelled(true);
                });
    }
}
