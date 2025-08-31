package majster2nn.dev.betonQuestQT.tracker.menus.display;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.menu_handlers.InventoryButton;
import majster2nn.dev.betonQuestQT.menu_handlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.Statuses;
import majster2nn.dev.betonQuestQT.tracker.gps.PlayerQuestTracker;
import majster2nn.dev.betonQuestQT.tracker.menus.FilterMenu;
import majster2nn.dev.betonQuestQT.tracker.menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.tracker.menus.layouts.ButtonLayoutContainer;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class QuestMenus extends MultiPageInventoryGUI {
    final String questType;

    public QuestMenus(String invName, String questType) {
        super(invName);
        this.questType = questType;
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 9*6, Component.text(invName));
    }

    @Override
    public void decorate(Player player){
        ButtonLayoutContainer.getQuestCategoriesMenus().forEach((key, value) -> {
            if(ButtonVisualsStorage.getButtonEvents(value).contains("prevPage") && pageMap.get(currentPage - 1) == null){
                addButton(key, currentPage, buttonSkeleton(value, true));
            }
            if(ButtonVisualsStorage.getButtonEvents(value).contains("nextPage") && pageMap.get(currentPage + 1) == null){
                addButton(key, currentPage, buttonSkeleton(value, true));
            }
            addButton(key, buttonSkeleton(value, false));
        });

        setAllQuestButtons(player);

        super.decorate(player);
    }

    public void setAllQuestButtons(Player player) {
        Map<Integer, String> layout = ButtonLayoutContainer.getMainMenuLayout();
        List<Integer> placeholderSlots = layout.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().trim().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        List<QuestPlaceholder> filteredQuests = QuestPlaceholder.packageByName.values().stream()
                .map(questPackage -> QuestPlaceholder.getQuestPlaceholderFromPackage(questPackage, player))
                .filter(q -> {
                    String category = QuestPlaceholder.packagesByCategory.getOrDefault(q.questPackage, "none");
                    Statuses status = QuestPlaceholder.packageStatusesMap
                            .getOrDefault(player, Map.of())
                            .getOrDefault(q.questPackage.getQuestPath(), Statuses.HIDDEN);

                    if ("finished".equalsIgnoreCase(questType)) {
                        return status == Statuses.FINISHED;
                    }

                    return questType.equalsIgnoreCase(category)
                            && status != Statuses.HIDDEN
                            && status != Statuses.FINISHED;
                })
                .filter(q -> {
                    List<String> tags = QuestPlaceholder.packagesTags.getOrDefault(q.questPackage, List.of());
                    return new HashSet<>(tags).containsAll(FilterMenu.playerFilters.getOrDefault(player, List.of()));
                })
                .toList();


        int perPage = placeholderSlots.size();
        int start = (currentPage - 1) * perPage;
        int end = Math.min(start + perPage, filteredQuests.size());
        List<QuestPlaceholder> pageQuests = filteredQuests.subList(start, end);

        // Apply each quest to a slot
        for (int i = 0; i < pageQuests.size(); i++) {
            setQuestButton(player, placeholderSlots.get(i), currentPage, pageQuests.get(i));
        }
    }


    private void setQuestButton(Player player, int slot, int currentPage, QuestPlaceholder questPlaceholder) {
        this.addButton(slot, currentPage, new InventoryButton()
                .creator(x -> questPlaceholder.getQuestDisplay())
                .consumer(e -> {
                    Statuses status = QuestPlaceholder.packageStatusesMap
                            .getOrDefault(player, Map.of())
                            .getOrDefault(questPlaceholder.questPackage.getQuestPath(), Statuses.HIDDEN);

                    if(status == Statuses.ACTIVE) {
                        PlayerQuestTracker.setPlayerActiveQuest(player, questPlaceholder);
                        PlayerQuestTracker.activateQuestTracking(player);
                    }
                    e.setCancelled(true);
                }));
    }


    private InventoryButton buttonSkeleton(String buttonVisualName, boolean ignoreEvent){
        return new InventoryButton()
                .creator(player -> {
                    Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    return ButtonVisualsStorage.getButtonItem(buttonVisualName, lang).clone();
                })
                .consumer(event -> {
                    if(!ignoreEvent && ButtonVisualsStorage.getButtonEvents(buttonVisualName) != null){
                        HashMap<String, String> eventMap = new HashMap<>();
                        for(String eventString : ButtonVisualsStorage.getButtonEvents(buttonVisualName).split(";")){
                            List<String> eventRaw = new ArrayList<>(Arrays.asList(eventString.split(":", 2)));
                            eventRaw.add(" ");
                            eventMap.put(eventRaw.getFirst(), eventRaw.get(1));
                        }

                        if(eventMap.containsKey("return")){
                            BetonQuestQT.getInstance().guiManager.openGui(
                                    new MainMenu(BetonQuestQT.getInstance().getMenuTranslation("main-menu", (Player) event.getWhoClicked())),
                                    (Player) event.getWhoClicked()
                            );
                        }

                        if(eventMap.containsKey("filter")){
                            BetonQuestQT.getInstance().guiManager.openGui(
                                    new FilterMenu(BetonQuestQT.getInstance().getMenuTranslation("header-filters", (Player) event.getWhoClicked()), questType),
                                    (Player) event.getWhoClicked()
                            );
                        }

                        if(eventMap.containsKey("prevPage")){
                            if(pageMap.get(currentPage - 1) != null) {
                                currentPage--;
                            }
                        }

                        if(eventMap.containsKey("nextPage")){
                            if(pageMap.get(currentPage + 1) != null){
                                currentPage++;
                            }
                        }
                    }
                    event.setCancelled(true);
                });
    }
}
