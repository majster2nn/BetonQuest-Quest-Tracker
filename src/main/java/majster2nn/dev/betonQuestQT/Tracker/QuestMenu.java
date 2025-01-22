package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.InventoryHandlers.HeadsHandlers;
import majster2nn.dev.betonQuestQT.InventoryHandlers.InventoryButton;
import majster2nn.dev.betonQuestQT.InventoryHandlers.MultiPageInventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.database.PlayerData;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.betonquest.betonquest.variables.GlobalVariableResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
                            meta.displayName(Component
                                    .text(BetonQuestQT.getInstance().getTranslation("prevP", player))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                            display.setItemMeta(meta);
                            break;
                        }
                        case 52:{
                            display = new ItemStack(HeadsHandlers.getHead(getRightScrollButton()));
                            ItemMeta meta = display.getItemMeta();
                            meta.displayName(Component
                                    .text(BetonQuestQT.getInstance().getTranslation("nextP", player))
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
        Profile profile = PlayerConverter.getID(player);
        PlayerData playerData = BetonQuest.getInstance().getPlayerData(profile);
        List<String> playerTags = playerData.getTags();

        Map<String, QuestPackage> mappedQuests = Config.getPackages();
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
            if(playerTags.contains(questPackage + ".questUnavailable")){return;}

            QuestPlaceholder questPlaceholder = new QuestPlaceholder(
                    new ItemStack(Material.matchMaterial(GlobalVariableResolver.resolve(questPackage, "$questDisplay$"))),
                    GlobalVariableResolver.resolve(questPackage, "$questName$"),
                    GlobalVariableResolver.resolve(questPackage, "$questDesc$").split("\n"),
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
