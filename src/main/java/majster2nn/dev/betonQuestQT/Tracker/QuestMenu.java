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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        mappedQuests.forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("basicQuest")){return;}
            System.out.println(playerTags);
            if(playerTags.contains(questPackage + ".questUnavailable")){return;}

            this.addButton(currentSlot[0], currentPage[0], new InventoryButton()
                    .creator(player1 -> {
                        ItemStack display = new ItemStack(Material.DIRT);
                        try {
                            display = new ItemStack(Material.matchMaterial(GlobalVariableResolver.resolve(questPackage, "$questDisplay$")));
                            ItemMeta itemMeta = display.getItemMeta();
                            Component questName = Component.text("");
                            for(String word : GlobalVariableResolver.resolve(questPackage, "$questName$").split(" ")){
                                questName = questName.append(Component
                                        .text(word.split("")[0].equals("%") ? BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, word, profile) : word + " ")
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                        );
                            }
                            itemMeta.displayName(questName);

                            List<Component> loreParts = new ArrayList<>();
                            for(String line : GlobalVariableResolver.resolve(questPackage, "$questDesc$").split("\n")){
                                Component loreLine = Component.text("");
                                for(String word : line.split(" ")){
                                    if(word.equals("\n")) continue;

                                    loreLine = loreLine.append(Component
                                            .text(word.split("")[0].equals("%") ? BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, word, profile) : word + " ")
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                            .color(TextColor.color(Integer.parseInt("757575" , 16)
                                            )));
                                }
                                loreParts.add(loreLine);
                            }
                            itemMeta.lore(loreParts);
                            display.setItemMeta(itemMeta);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return display;
                    })
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
