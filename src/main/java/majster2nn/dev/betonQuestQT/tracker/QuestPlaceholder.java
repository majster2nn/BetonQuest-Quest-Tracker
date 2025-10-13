package majster2nn.dev.betonQuestQT.tracker;

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
import org.betonquest.betonquest.api.quest.condition.ConditionID;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
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
        }

    }

    public static QuestPlaceholder getQuestPlaceholderFromPackage(QuestPackage questPackage, Player player){
        ComponentLogger logger = BetonQuestQT.getInstance().getComponentLogger();
        BetonQuest bqInstance = BetonQuest.getInstance();

        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
        String lang = playerData.getLanguage().isPresent() ? playerData.getLanguage().get() : BetonQuest.getInstance().getDefaultLanguage();
        ConfigurationSection config = questPackage.getConfig();

        String[] item = Utils.formatLineWithVariables(Utils.getSafeString(config, "questParameters", "display"), questPackage, null).split(",");

        Material mat = Material.matchMaterial(item[0] != null ? item[0].toUpperCase() : "");
        if(mat == null){
            if(BetonQuestQT.debug) {
                logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no material found for \"" + item[0] + "\", defaulting to DIRT..."));
            }
            mat = Material.DIRT;
        }
        ItemStack display = new ItemStack(mat);

        if(item.length > 1 && !item[1].isEmpty()){
            ItemMeta displayMeta = display.getItemMeta();
            List<String> mcVersionsSupported = List.of("1.21.5", "1.21.6", "1.21.7", "1.21.8"); //TODO .getVersion().split(".") and then for each splitted number check if its greater then corresponding number of 1.21.3 and if any of the numbers is bigger then use the new branch else use the legacy system
            if(mcVersionsSupported.contains(Bukkit.getMinecraftVersion())){
                CustomModelDataComponent component = displayMeta.getCustomModelDataComponent();
                component.setStrings(List.of(item[1]));
                displayMeta.setCustomModelDataComponent(component);
            }else{
                displayMeta.setCustomModelData(Integer.parseInt(item[1]));
            }

            display.setItemMeta(displayMeta);
        }

        String questName = Utils.getSafeString(config, "questParameters.name", lang);

        if(questName.isBlank() || questName.isEmpty()){
            if(BetonQuestQT.debug) {
                logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no name specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to " + BetonQuest.getInstance().getDefaultLanguage() + "..."));
            }
            questName = Utils.getSafeString(config, "questParameters.name", BetonQuest.getInstance().getDefaultLanguage());

            if(questName.isBlank() || questName.isEmpty()) {
                if(BetonQuestQT.debug) {
                    logger.error(Component.text("Couldn't default to" + BetonQuest.getInstance().getDefaultLanguage() + " for package: " + questPackage + ", using default debug values..."));
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
                        logger.error(Component.text("Error while parsing Quest Placeholder for " + questPackage + ", no desc specified for language " + bqInstance.getPlayerDataStorage().get(profile).getLanguage() + ", trying to default to " + BetonQuest.getInstance().getDefaultLanguage() + "..."));
                    }
                    desc = config.getString("questParameters.questParts." + key + ".desc." + BetonQuest.getInstance().getDefaultLanguage());

                    if(desc == null) {
                        if(BetonQuestQT.debug) {
                            logger.error(Component.text("Couldn't default to " + BetonQuest.getInstance().getDefaultLanguage() + " for package: " + questPackage + ", using default debug values..."));
                        }
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

    public void setQuestDisplay() {
        ItemMeta questDisplayMeta = displayMaterial.getItemMeta();

        String formattedName = Utils.formatLineWithVariables(name, questPackage, BetonQuest.getInstance().getProfileProvider().getProfile(player));

        questDisplayMeta.displayName(Utils.formatYmlString(formattedName));

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
            loreComponents.add(Utils.formatYmlString(Utils.formatLineWithVariables(line, questPackage, BetonQuest.getInstance().getProfileProvider().getProfile(player))));
        }

        status = packageStatusesMap.getOrDefault(player, new HashMap<>()).getOrDefault(questPackage.getQuestPath(), Statuses.HIDDEN);

        ConfigurationSection settings = BetonQuestQT.getInstance().getConfig().getConfigurationSection("settings");
        if(settings != null && settings.contains("questStatusVisuals") && settings.getBoolean("questStatusVisuals")) {
            switch (status) {
                case ACTIVE: {
                    loreComponents.add(Component
                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_active", player))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.GREEN));
                    break;
                }
                case FINISHED: {
//                loreComponents.clear(); TODO add an option to specify if the lore should be hidden after finished or nah
                    loreComponents.add(Component
                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_finished", player))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.GRAY));
                    break;
                }
                case LOCKED: {
//                loreComponents.clear(); TODO add an option to specify if the lore should be hidden if locked or nah
                    loreComponents.add(Component
                            .text(BetonQuestQT.getInstance().getMenuTranslation("quest_locked", player))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.RED));
                    break;
                }
                default: {
                    break;
                }
            }
        }

        questDisplayMeta.lore(loreComponents);

        questDisplay = displayMaterial;
        questDisplay.setItemMeta(questDisplayMeta);
    }

    public ItemStack getQuestDisplay(){
        return questDisplay;
    }

    public void update(Player player){
        Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
        for(QuestPart questPart : questParts){
            List<ConditionID> conditions = new ArrayList<>();
            for(String condition : Optional.ofNullable(questPart.getConditions()).orElse("").split(",")){
                if(!condition.isBlank()){
                    try {
                        conditions.add(new ConditionID(BetonQuest.getInstance().getQuestPackageManager(), questPackage, condition));
                    } catch (QuestException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
//            for(ConditionID condition : conditions){
//                System.out.println("Condition " + condition.toString());
//                System.out.println("Status " + BetonQuest.getInstance().getQuestTypeAPI().condition(profile, condition));
//            } --DEBUG
            if(BetonQuest.getInstance().getQuestTypeApi().conditions(profile, conditions)){
                currentlyActiveQuestPart = questPart;
                break;
            }
        }
    }
}

