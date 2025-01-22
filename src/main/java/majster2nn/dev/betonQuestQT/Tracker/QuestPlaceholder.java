package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.betonquest.betonquest.variables.GlobalVariableResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestPlaceholder {
    public ItemStack displayMaterial;
    public String name;
    public String lore;
    public Statuses status = Statuses.LOCKED;
    public Player player;
    private QuestPackage questPackage;

    public ItemStack questDisplay;

    public enum QuestTypes{
        MAIN_QUEST,
        SIDE_QUEST,
        EVENT_QUEST
    }

    public enum Statuses{
        FINISHED,
        LOCKED,
        ACTIVE
    }

    public QuestPlaceholder(@NotNull ItemStack display,@NotNull String name,@NotNull String lore,@NotNull Player player,@NotNull QuestPackage questPackage){
        this.displayMaterial = display;
        this.name = name;
        this.lore = lore;
        this.player = player;
        this.questPackage = questPackage;
        setQuestDisplay();
    }

    public void setQuestDisplay(){
        ItemMeta questDisplayMeta = displayMaterial.getItemMeta();

        String formattedName = formatLineWithVariables(name);

        questDisplayMeta.displayName(Component
                .text(formattedName)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        List<Component> loreComponents = new ArrayList<>();

        for(String line : lore.split("\n")){
            loreComponents.add(Component
                    .text(formatLineWithVariables(line))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(TextColor.color(Integer.parseInt("757575" , 16)))
            );
        }

        switch(status){
            case ACTIVE:{
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getTranslation("quest_active", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN));
                break;
            }
            case FINISHED:{
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getTranslation("quest_finished", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GRAY));
                break;
            }
            case LOCKED:{
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getTranslation("quest_locked", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED));
                break;
            }
            default:{
                break;
            }
        }

        questDisplayMeta.lore(loreComponents);

        questDisplay=displayMaterial;
        questDisplay.setItemMeta(questDisplayMeta);
    }

    public ItemStack getQuestDisplay(){
        return questDisplay;
    }

    public String formatLineWithVariables(String line) {
        StringBuilder formattedString = new StringBuilder();
        StringBuilder preFormatVariable = new StringBuilder();
        StringBuilder preFormatGlobalVariable = new StringBuilder();
        boolean caughtVariable = false;
        boolean caughtGlobalVariable = false;

        for (String str : line.split("")) {
            switch (str) {
                case "$": {
                    if (!caughtGlobalVariable) {
                        caughtGlobalVariable = true;
                        preFormatGlobalVariable.append("$");
                    } else {
                        caughtGlobalVariable = false;
                        preFormatGlobalVariable.append("$");
                        if(caughtVariable){
                            preFormatVariable.append(GlobalVariableResolver.resolve(questPackage, preFormatGlobalVariable.toString()));
                        }else{
                            formattedString.append(GlobalVariableResolver.resolve(questPackage, preFormatGlobalVariable.toString()));
                        }
                        preFormatGlobalVariable = new StringBuilder();
                    }
                    break;
                }
                case "%": {
                    if (!caughtVariable) {
                        caughtVariable = true;
                        preFormatVariable.append("%");
                    } else{
                        caughtVariable = false;
                        preFormatVariable.append("%");
                        try {
                            formattedString.append(
                                    BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, preFormatVariable.toString(), PlayerConverter.getID(player)));
                        } catch (InstructionParseException e) {
                            BetonQuestQT.getInstance().getLogger().severe("Failed to process variable: " + preFormatVariable);
                        }
                        preFormatVariable = new StringBuilder();
                    }
                    break;
                }
                default: {
                    if(caughtGlobalVariable){
                        preFormatGlobalVariable.append(str);
                    }else if (caughtVariable) {
                        preFormatVariable.append(str);
                    }else {
                        formattedString.append(str);
                    }
                }
            }

        }
        return formattedString.toString();
    }
}
