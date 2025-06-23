package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
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
    public List<QuestPart> questParts;
    public QuestPart currentlyActiveQuestPart;
    public Statuses status = Statuses.LOCKED;
    public Player player;
    public final QuestPackage questPackage;

    public static Map<Player, Map<String, Statuses>> packageStatusesMap = new HashMap<>();
    public static Map<String, QuestPackage> packageByName = new HashMap<>();
    public static Map<QuestPackage, String> packagesByCategory = new HashMap<>();
    public static Map<QuestPackage, List<String>> packagesTags = new HashMap<>();
    public static List<String> tags = new ArrayList<>();

    public ItemStack questDisplay;

    public QuestPlaceholder(
            @NotNull ItemStack display,
            @NotNull String name,
            @NotNull List<QuestPart> questParts,
            @NotNull Player player,
            @NotNull QuestPackage questPackage) {
        this.displayMaterial = display;
        this.name = name;
        this.questParts = questParts;
        this.player = player;
        this.questPackage = questPackage;
        try {
            setQuestDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }//TODO questParts connection with PlayerQuestTracker

    public static QuestPlaceholder getQuestPlaceholderFromPackage(QuestPackage questPackage, Player player){
        ComponentLogger logger = BetonQuestQT.getInstance().getComponentLogger();
        BetonQuest bqInstance = BetonQuest.getInstance();

        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        String lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
        ConfigurationSection config = questPackage.getConfig();

        String material = Utils.getSafeString(config, "questParameters", "display");
        Material mat = Material.matchMaterial(material != null ? material.toUpperCase() : "");
        if(mat == null){
            logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no material found for \"" + material + "\", defaulting to DIRT..."));
            mat = Material.DIRT;
        }
        ItemStack display = new ItemStack(mat);

        String questName = Utils.getSafeString(config, "questParameters.name", lang);

        if(questName == null){
            logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no name specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to en-US..."));
            questName = Utils.getSafeString(config, "questParameters.name", "en-US");

            if(questName == null) {
                logger.error(Component.text("Couldn't default to en-US for package: " + questPackage + ", using default debug values..."));
                questName = "ERROR - contact administration";
            }
        }

//        Map<String, String> conditionedDesc = new HashMap<>();
//
//        for(String key : config.getConfigurationSection("questParameters.desc").getKeys(false)){
//            String descPart = config.getString("questParameters.desc." + key + ".text." + lang);
//            String conditions = config.getString("questParameters.desc." + key + ".conditions");
//            //TODO THIS PART LOADS EVERY SINGLE PACKAGE OF QUEST EVEN IF IT DOESNT HAVE ANYTHING TO DO WITH THE CURRENT CATEGORY AND ALSO RELOADS ON EVERY CLICK, MIGHT CAUSE SERIOUS LAGS PROCEED WITH CAUTION
////            System.out.println("Top level keys in desc: " + config.getConfigurationSection("questParameters.desc").getKeys(false) + " in package of id: " + id);
////            System.out.println("desc: " + descPart);
////            System.out.println(config.getConfigurationSection("questParameters.desc").getKeys(true));
////            System.out.println("conditions: " + conditions + " path checked: questParameters.desc." + key + ".conditions");
//            conditionedDesc.put(descPart, conditions != null ? conditions : "");
//        }

        List<QuestPart> questParts = new ArrayList<>();

        if(config.getConfigurationSection("questParameters.questParts") == null){
            logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", section questParts doesn't exist or is empty" ));
        }else{
            for(String key : config.getConfigurationSection("questParameters.questParts").getKeys(false)){

                String desc = config.getString("questParameters.questParts." + key + ".desc." + lang);

                if(desc == null){
                    logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no desc specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to en-US..."));
                    desc = Utils.getSafeString(config, "questParameters.desc", "en-US");

                    if(desc == null) {
                        logger.error(Component.text("Couldn't default to en-US for package: " + questPackage + ", using default debug values..."));
                        desc = "ERROR - contact administration";
                    }
                }

                String conditions = config.getString("questParameters.questParts." + key + ".conditions");
                String location = config.getString("questParameters.questParts." + key + ".location");

                questParts.add(new QuestPart(desc, conditions, location));
            }
        }

        return new QuestPlaceholder(
                display,
                questName,
                questParts,
                player,
                questPackage
        );
    }

    public void setQuestDisplay() throws QuestException {
        ItemMeta questDisplayMeta = displayMaterial.getItemMeta();

        String formattedName = formatLineWithVariables(name);

        questDisplayMeta.displayName(Utils.formatYmlString(formattedName));

        List<Component> loreComponents = new ArrayList<>();

        update(player);

        String lore = currentlyActiveQuestPart.getDesc();

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

    public void update(Player player){
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        for(QuestPart questPart : questParts){
            List<ConditionID> conditions = new ArrayList<>();
            for(String condition : Optional.ofNullable(questPart.getConditions()).orElse("").split(",")){
                if(!condition.isBlank()){
                    try {
                        conditions.add(new ConditionID(questPackage, condition));
                    } catch (QuestException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
//            for(ConditionID condition : conditions){
//                System.out.println("Condition " + condition.toString());
//                System.out.println("Status " + BetonQuest.getInstance().getQuestTypeAPI().condition(profile, condition));
//            } --DEBUG
            if(BetonQuest.getInstance().getQuestTypeAPI().conditions(profile, conditions)){
                currentlyActiveQuestPart = questPart;
                break;
            }
        }
    }
}

