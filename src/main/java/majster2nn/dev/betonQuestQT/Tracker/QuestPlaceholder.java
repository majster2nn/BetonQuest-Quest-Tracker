package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.id.ConditionID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QuestPlaceholder {
    public ItemStack displayMaterial;
    public String name;
    public Map<String, String> conditionedLore;
    public Statuses status = Statuses.LOCKED;
    public Player player;
    private final QuestPackage questPackage;

    public static Map<Player, Map<String, Statuses>> packageStatusesMap = new HashMap<>();
    public static Map<String, QuestPackage> packageByName = new HashMap<>();
    public static Map<QuestPackage, String> packagesByCategory = new HashMap<>();
    public static Map<QuestPackage, List<String>> packagesTags = new HashMap<>();
    public static List<String> tags = new ArrayList<>();

    public ItemStack questDisplay;

    public QuestPlaceholder(
            @NotNull ItemStack display,
            @NotNull String name,
            @NotNull Map<String, String> conditionedLore,
            @NotNull Player player,
            @NotNull QuestPackage questPackage) {
        this.displayMaterial = display;
        this.name = name;
        this.conditionedLore = conditionedLore;
        this.player = player;
        this.questPackage = questPackage;
        try {
            setQuestDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void setQuestDisplay() throws QuestException {
        ItemMeta questDisplayMeta = displayMaterial.getItemMeta();

        String formattedName = formatLineWithVariables(name);

        questDisplayMeta.displayName(Utils.formatYmlString(formattedName));

        List<Component> loreComponents = new ArrayList<>();
        String lore = "";

        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        for(Map.Entry<String, String> descPart : conditionedLore.entrySet()){
            List<ConditionID> conditions = new ArrayList<>();
            for(String condition : Optional.ofNullable(descPart.getValue()).orElse("").split(",")){
                if(!condition.isBlank()){
                    try {
                        System.out.println(condition);
                        conditions.add(new ConditionID(questPackage, condition));
                    } catch (QuestException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
//            for(ConditionID condition : conditions){
//                System.out.println("Condition " + condition.toString());
//                System.out.println("Status " + BetonQuest.getInstance().getQuestTypeAPI().condition(profile, condition));
//            }
            if(BetonQuest.getInstance().getQuestTypeAPI().conditions(profile, conditions)){
                lore = descPart.getKey();
                break;
            }
        }

        if(lore == null || lore.isBlank() || lore.isEmpty()){
            lore = "";
        }

        for(String line : lore.split("\n")){
            loreComponents.add(Utils.formatYmlString(formatLineWithVariables(line)));
        }

        status = packageStatusesMap.getOrDefault(player, new HashMap<>()).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN);


        switch(status){
            case ACTIVE:{
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getMenuTranslation("quest_active", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN));
                break;
            }
            case FINISHED:{
                loreComponents.clear();
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getMenuTranslation("quest_finished", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GRAY));
                break;
            }
            case LOCKED:{
                loreComponents.clear();
                loreComponents.add(Component
                        .text(BetonQuestQT.getInstance().getMenuTranslation("quest_locked", player))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED));
                break;
            }
            default:{
                break;
            }
        }

        questDisplayMeta.lore(loreComponents);

        questDisplay = displayMaterial;
        questDisplay.setItemMeta(questDisplayMeta);
    }

    public ItemStack getQuestDisplay(){
        return questDisplay;
    }

    public String formatLineWithVariables(String line) throws QuestException {
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
                        preFormatGlobalVariable.append("%");
                    } else {
                        caughtGlobalVariable = false;
                        preFormatGlobalVariable.append("%");
                        if(caughtVariable){
                            preFormatVariable.append(BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, preFormatGlobalVariable.toString(), null));
                        }else{
                            formattedString.append(BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, preFormatGlobalVariable.toString(), null));
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
                                    BetonQuest.getInstance().getVariableProcessor().getValue(questPackage, preFormatVariable.toString(), BetonQuest.getInstance().getProfileProvider().getProfile(player)));
                        } catch (QuestException e) {
                            throw new RuntimeException(e);
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

    public static QuestPlaceholder getQuestPlaceholderFromPackage(String id, QuestPackage questPackage, Player player){
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);

        ItemStack display;
        String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
        ConfigurationSection config = questPackage.getConfig();

        String material = Utils.getSafeString(config, "questParameters", "display");
        Material mat = Material.matchMaterial(material != null ? material.toUpperCase() : "");
        display = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.DIRT);

        String questName = Utils.getSafeString(config, "questParameters.name", lang);
        if (questName == null) {
            questName = "ERROR CHECK SYNTAX OR REPORT";
        }

        Map<String, String> conditionedDesc = new HashMap<>();
        for(String key : config.getConfigurationSection("questParameters.desc").getKeys(false)){
            String descPart = config.getString("questParameters.desc." + key + ".text." + lang);
            String conditions = config.getString("questParameters.desc." + key + ".conditions");
            //TODO THIS PART LOADS EVERY SINGLE PACKAGE OF QUEST EVEN IF IT DOESNT HAVE ANYTHING TO DO WITH THE CURRENT CATEGORY AND ALSO RELOADS ON EVERY CLICK, MIGHT CAUSE SERIOUS LAGS PROCEED WITH CAUTION
//            System.out.println("Top level keys in desc: " + config.getConfigurationSection("questParameters.desc").getKeys(false) + " in package of id: " + id);
//            System.out.println("desc: " + descPart);
//            System.out.println(config.getConfigurationSection("questParameters.desc").getKeys(true));
//            System.out.println("conditions: " + conditions + " path checked: questParameters.desc." + key + ".conditions");
            conditionedDesc.put(descPart, conditions != null ? conditions : "");
        }


        return new QuestPlaceholder(
                display,
                questName,
                conditionedDesc,
                player,
                questPackage
        );
    }
}
