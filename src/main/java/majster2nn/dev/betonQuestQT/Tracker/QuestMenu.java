package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsHandlers;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getLeftScrollButton;
import static majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsList.getRightScrollButton;

public class QuestMenu extends MultiPageInventoryGUI {
    public QuestMenu(String invName) {
        super(invName);
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
        PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
        List<String> playerTags = playerData.getTags();

        Map<String, QuestPackage> mappedQuests = BetonQuest.getInstance().getPackages();
        Map<String, QuestPackage> sortedQuests = mappedQuests.entrySet()
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
            if(!playerTags.contains(questPackage + ".questTrackable")){return;}


            questPackage.getConfig().getConfigurationSection("questParameters.questDesc").getKeys(false);
            questPackage.getConfig().getConfigurationSection("questParameters.questName").getKeys(false);
            QuestPlaceholder questPlaceholder = new QuestPlaceholder(
                    new ItemStack(Material.matchMaterial(questPackage.getConfig().getString("questParameters.questDisplay"))),
                    questPackage.getConfig().getConfigurationSection("questParameters.questName").getString(BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get()),
                    questPackage.getConfig().getConfigurationSection("questParameters.questDesc").getString(BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get()),
                    player,
                    questPackage
            );


            this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                    .creator(player1 -> questPlaceholder.getQuestDisplay())
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
}
