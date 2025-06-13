package majster2nn.dev.betonQuestQT.Tracker.Menus;

import io.papermc.paper.datacomponent.DataComponentTypes;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsHandlers;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getLeftScrollButton;
import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getRightScrollButton;

public class OtherQuestsMenu extends MultiPageInventoryGUI {
    public OtherQuestsMenu(String invName) {
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

        this.addButton(46, -1, scrollButtons(46));
        this.addButton(52, -1, scrollButtons(52));
        this.addButton(53, -1, backButton());

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
                    display.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Back"));
                    return display;
                })
                .consumer(e -> {
                    Player player = (Player) e.getWhoClicked();
                    BetonQuestQT.getInstance().guiManager.openGui(new MainQuestMenu(BetonQuestQT.getInstance().getTranslation("main-menu", player)), player);
                });
    }
    //TODO DYNAMIC DESCRIPTION BASED OFF CONDITION CHECKING LIKE IN BETONQUEST
    public void setQuestButtons(Player player){
        final int[] currentSlot = {9};
        final int[] currentPage = {1};
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        QuestPlaceholder.packageByName
                //TODO ADD SORTING AND FILTERING (.sorted, .filter)
                .forEach((id, questPackage) -> {
                    if (!questPackage.getTemplates().contains("trackedQuest")) {
                        return;
                    }

                    ItemStack display;
                    String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
                    ConfigurationSection config = questPackage.getConfig();

                    String material = getSafeString(config, "questParameters.display", lang);
                    Material mat = Material.matchMaterial(material != null ? material.toUpperCase() : "");
                    display = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.DIRT);

                    String questName = getSafeString(config, "questParameters.name", lang);
                    if (questName == null) {
                        questName = "ERROR CHECK SYNTAX OR REPORT";
                    }

                    String lore = getSafeString(config, "questParameters.desc", lang);
                    if (lore == null) {
                        lore = "ERROR CHECK SYNTAX OR REPORT";
                    }

                    String questCategory = getSafeString(config, "questParameters", "category");
                    if (questCategory == null) {
                        questCategory = "other";
                    }

                    QuestPlaceholder questPlaceholder = new QuestPlaceholder(
                            display,
                            questName,
                            lore,
                            questCategory,
                            player,
                            questPackage
                    );

                    if(!QuestPlaceholder.packagesByCategory.getOrDefault(questPackage, "none").equalsIgnoreCase("other") ||
                        QuestPlaceholder.packageStatusesMap.getOrDefault(player, new HashMap<>()).getOrDefault(questPackage, Statuses.HIDDEN).equals(Statuses.HIDDEN)){
                        return;
                    }

                    this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                            .creator(x -> questPlaceholder.getQuestDisplay())
                            .consumer(e -> e.setCancelled(true))
                    );
                    currentSlot[0]++;
                    if(currentSlot[0] == 45){
                        currentSlot[0] = 9;
                        currentPage[0]++;
                    }
                });
    }

    String getSafeString(ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getString(langKey) : null;
    }
}
