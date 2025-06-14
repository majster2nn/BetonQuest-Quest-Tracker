package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsHandlers;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getLeftScrollButton;
import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getRightScrollButton;

@Deprecated(forRemoval = true)
public class QuestMenu extends MultiPageInventoryGUI {
    private final BetonQuestQT plugin;

    //TODO ADD MENU TYPES FOR ACTIVE LOCKED AND FINISHED QUESTS SO THEY ARE NOT IN ONE MENU AND FILTERED BUT RATHER IN SEPARATE MENUS SO SPLIT THIS MENU INTO 3 OR EVEN 4 MENUS
    public QuestMenu(String invName, BetonQuestQT plugin) {
        super(invName);
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory(String invName) {
        return Bukkit.createInventory(null, 6 * 9, invName);
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

    public void setQuestButtons(Player player){
        final int[] currentSlot = {9};
        final int[] currentPage = {1};
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);

        Map<String, QuestPackage> sortedQuests = QuestPlaceholder.packageByName.entrySet()
                .stream()
                .parallel()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        sortedQuests.forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            if(QuestPlaceholder.packageStatusesMap.get(player).getOrDefault(questPackage, Statuses.HIDDEN) == Statuses.HIDDEN){return;}

            ItemStack display;
            String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
            ConfigurationSection config = questPackage.getConfig();

            String material = getSafeString(config, "questParameters.display", lang);
            Material mat = Material.matchMaterial(material != null ? material.toUpperCase() : "");
            display = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.DIRT);

            String questName = getSafeString(config, "questParameters.nane", lang);
            if (questName == null) {
                questName = "ERROR CHECK SYNTAX OR REPORT";
            }

            String lore = getSafeString(config, "questParameters.desc", lang);
            if (lore == null) {
                lore = "ERROR CHECK SYNTAX OR REPORT";
            }

            QuestPlaceholder questPlaceholder = new QuestPlaceholder(
                    display,
                    questName,
                    lore,
                    player,
                    questPackage
            );

            //TODO DYNAMICZNY OPIS W ZALEŻNOŚCI OD WARUNKU CZYLI KASKADOWE SPRAWDZANIE JAK W BETONQUEŚCIE

            this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                    .creator(x -> questPlaceholder.getQuestDisplay())
                    .consumer(e -> {
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

    String getSafeString(ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getString(langKey) : null;
    }
}
