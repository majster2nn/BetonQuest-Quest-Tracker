package majster2nn.dev.betonQuestQT.tracker;

import io.papermc.paper.datacomponent.DataComponentTypes;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Deprecated
public class QuestPlaceholder {
    public ItemStack displayMaterial;
    public String name;
    public List<QuestPart> questParts;
    public QuestPart currentlyActiveQuestPart;
    public Player player;
    public final QuestPackage questPackage;
    private static String defaultLanguage = BetonQuestQT.getInstance().getDefaultLanguage();

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
        }
    }

    public static QuestPlaceholder getQuestPlaceholderFromPackage(QuestPackage questPackage, Player player){
        ComponentLogger logger = BetonQuestQT.getInstance().getComponentLogger();
        BetonQuest bqInstance = BetonQuest.getInstance();

        Profile profile = BetonQuestQT.getInstance().getBetonQuestApi().profiles().getProfile(player);
        PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
        String lang = playerData.getLanguage().isPresent() ? playerData.getLanguage().get() : defaultLanguage;
        ConfigurationSection config = questPackage.getConfig();

        String itemMaterial = Utils.parseString(Utils.getSafeString(config, "questParameters", "display"), questPackage, null);

        Material mat = Material.matchMaterial(itemMaterial != null ? itemMaterial.toUpperCase() : "");
        if(mat == null){
            if(BetonQuestQT.debug) {
                logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no material found for \"" + itemMaterial + "\", defaulting to DIRT..."));
            }
            mat = Material.DIRT;
        }
        ItemStack display = new ItemStack(mat);

        String itemModel = Utils.parseString(Utils.getSafeString(config, "questParameters", "customModel"), questPackage, null);

        if(itemModel != null && !itemModel.isEmpty() && !itemModel.isBlank()){
            display.setData(DataComponentTypes.ITEM_MODEL, Key.key(itemModel));
        }

        String questName = Utils.getSafeString(config, "questParameters.name", lang);

        if(questName.isBlank()){
            if(BetonQuestQT.debug) {
                logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no name specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to " + defaultLanguage + "..."));
            }
            questName = Utils.getSafeString(config, "questParameters.name", defaultLanguage);

            if(questName.isBlank() || questName.isEmpty()) {
                if(BetonQuestQT.debug) {
                    logger.error(Component.text("Couldn't default to" + defaultLanguage + " for package: " + questPackage + ", using default debug values..."));
                }
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
            if(BetonQuestQT.debug) {
                logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", section questParts doesn't exist or is empty"));
            }
        }else{
            for(String key : config.getConfigurationSection("questParameters.questParts").getKeys(false)){

                String desc = config.getString("questParameters.questParts." + key + ".desc." + lang);

                if(desc == null){
                    if(BetonQuestQT.debug) {
                        logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no desc specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to " + defaultLanguage + "..."));
                    }
                    desc = config.getString("questParameters.questParts." + key + ".desc." + defaultLanguage);

                    if(desc == null) {
                        if(BetonQuestQT.debug) {
                            logger.error(Component.text("Couldn't default to " + BetonQuestQT.getInstance().getDefaultLanguage() + " for package: " + questPackage + ", using default debug values..."));
                        }
                        desc = "ERROR - contact administration";
                    }
                }

                String location = config.getString("questParameters.questParts." + key + ".location");

                questParts.add(new QuestPart(desc, "questParameters.questParts." + key + ".conditions", location));
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

    public void setQuestDisplay() {
        ItemMeta questDisplayMeta = displayMaterial.getItemMeta();

        String formattedName = Utils.parseString(name, questPackage, BetonQuestQT.getInstance().getBetonQuestApi().profiles().getProfile(player));

        questDisplayMeta.displayName(Utils.formatString(formattedName));

        List<Component> loreComponents = new ArrayList<>();

        update(player);

        String lore = null;

        if(currentlyActiveQuestPart != null){
            lore = currentlyActiveQuestPart.getDesc();
        }

        if(lore == null || lore.isBlank() || lore.isEmpty()){
            lore = "";
        }

        for(String line : lore.split("\n")){
            loreComponents.add(Utils.formatString(Utils.parseString(line, questPackage, BetonQuestQT.getInstance().getBetonQuestApi().profiles().getProfile(player))));
        }

        ConfigurationSection settings = BetonQuestQT.getInstance().getConfig().getConfigurationSection("settings");
//        if(settings != null && settings.contains("questStatusVisuals") && settings.getBoolean("questStatusVisuals")) {
//            switch (status) {
//                case ACTIVE: {
//                    loreComponents.add(Component
//                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_active", player))
//                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
//                            .color(NamedTextColor.GREEN));
//                    break;
//                }
//                case FINISHED: {
////                loreComponents.clear();
//                    loreComponents.add(Component
//                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_finished", player))
//                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
//                            .color(NamedTextColor.GRAY));
//                    break;
//                }
//                case LOCKED: {
////                loreComponents.clear();
//                    loreComponents.add(Component
//                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_locked", player))
//                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
//                            .color(NamedTextColor.RED));
//                    break;
//                }
//                default: {
//                    break;
//                }
//            }
//        }

        questDisplayMeta.lore(loreComponents);

        questDisplay = displayMaterial;
        questDisplay.setItemMeta(questDisplayMeta);
    }

    public ItemStack getQuestDisplay(){
        return questDisplay;
    }

    public void update(Player player){
        Profile profile = BetonQuestQT.getInstance().getBetonQuestApi().profiles().getProfile(player);
        for(QuestPart questPart : questParts){
            if(Utils.checkBqConditions(questPackage, questPart.getConditions(), profile)){
                currentlyActiveQuestPart = questPart;
                break;
            }
        }
    }
}

