package majster2nn.dev.betonQuestQT.tracker.menus.buttons;

import io.papermc.paper.datacomponent.DataComponentTypes;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Utils;
import majster2nn.dev.betonQuestQT.menu_handlers.InventoryGUI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class ButtonVisualsStorage {
    private static final Map<String, ButtonEntry> buttonVisualsMap = new HashMap<>();

    public static void setButtonsMaterials() {
        FileConfiguration config = BetonQuestQT.getInstance().getConfig();
        ConfigurationSection buttonsSection = config.getConfigurationSection("buttonVisuals");

        if(buttonsSection == null){
            BetonQuestQT.getInstance().getComponentLogger().error(Component.text("NO BUTTON SECTION, BUTTONS WILL BE LOADED AS BARRIERS WITH ERROR TITLE, CONTACT SERVER ADMIN AND/OR PLUGIN DEV FOR DETAILS"), NamedTextColor.RED);
            return;
        }

        for(String buttonKey : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(buttonKey);
            Map<String, String> langMap = new HashMap<>();
            ConfigurationSection textSection = buttonSection.getConfigurationSection("text");
            if (textSection == null) {
                langMap.put(BetonQuestQT.getInstance().getDefaultLanguage(), "ERROR");
            } else {
                for (String langKey : textSection.getKeys(false)) {
                    langMap.put(langKey, buttonSection.getString("text." + langKey));
                }
            }

            String display = buttonSection.getString("displayMaterial");

            Material material = Material.matchMaterial(display.toUpperCase());

            String customModelData = buttonSection.getString("customModel");

            String events = "";
            if (buttonSection.contains("events")) {
                events += String.join(";", buttonSection.getStringList("events"));
            }

            buttonVisualsMap.put(buttonKey, new ButtonEntry(material, langMap, events, customModelData));
        }
    }

    public static ItemStack getButtonItem(String buttonName, String lang){
        ButtonEntry preFormatButton = buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>()));
        ItemStack button = new ItemStack(preFormatButton.getMaterial());
        String correctedLang = lang.equals("default") ? BetonQuestQT.getInstance().getDefaultLanguage() : lang;
        button.setData(DataComponentTypes.CUSTOM_NAME, Utils.formatString(preFormatButton.getDisplayForLang(correctedLang)));
//        try {
//            ItemIdentifier identifier = BetonQuestQT.getInstance().getBetonQuestApi().instructions().
//            BetonQuestQT.getInstance().getBetonQuestApi().items().manager().getItem(null, DefaultItemIdentifier.);
//        } catch (QuestException e) {
//            throw new RuntimeException(e);
//        }
        String customModelData = preFormatButton.getModelData();

        if (customModelData != null && !customModelData.isBlank()) {
            button.setData(DataComponentTypes.ITEM_MODEL, Key.key(customModelData));
        }

        button.editPersistentDataContainer(pdc -> {
            pdc.set(InventoryGUI.buttonKey, PersistentDataType.INTEGER, 1);
        });
        return button;
    }

    public static Material getButtonMaterial(String buttonName){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getMaterial();
    }

    public static String getButtonDisplayForLang(String buttonName, String lang){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getDisplayForLang(lang);
    }

    public static String getButtonModelData(String buttonName){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getModelData();
    }

    public static String getButtonEvents(String buttonName){
        return buttonVisualsMap.get(buttonName) != null ? buttonVisualsMap.get(buttonName).getSpecialParameters() : "";
    }

    public static boolean checkIfButtonExists(String string){
        return buttonVisualsMap.get(string) != null;
    }
}

