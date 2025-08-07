package majster2nn.dev.betonQuestQT.Tracker.Menus.buttons;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                langMap.put(BetonQuest.getInstance().getDefaultLanguage(), "ERROR");
            } else {
                for (String langKey : textSection.getKeys(false)) {
                    langMap.put(langKey, buttonSection.getString("text." + langKey));
                }
            }

            List<String> display = List.of(buttonSection.getString("displayMaterial").split(","));

            Material material = Material.matchMaterial(display.getFirst().toUpperCase());
            //CustomModelData modelData -- TODO ADD CUSTOM MODEL DATA SUPPORT

            String events = "";
            if (buttonSection.contains("events")) {
                events += String.join(";", buttonSection.getStringList("events"));
            }

            buttonVisualsMap.put(buttonKey, new ButtonEntry(material, langMap, events));
        }
    }

    public static ItemStack getButtonItem(String buttonName, String lang){
        ButtonEntry preFormatButton = buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>()));
        ItemStack button = new ItemStack(preFormatButton.getMaterial());
        button.setData(DataComponentTypes.CUSTOM_NAME, Component
                .text(preFormatButton.getDisplayForLang(lang), NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        return button;
    }

    public static Material getButtonMaterial(String buttonName){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getMaterial();
    }

    public static String getButtonDisplayForLang(String buttonName, String lang){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getDisplayForLang(lang);
    }

    public static CustomModelData getButtonModelData(String buttonName){
        return buttonVisualsMap.getOrDefault(buttonName, new ButtonEntry(Material.AIR, new HashMap<>())).getModelData();
    }

    public static String getButtonEvents(String buttonName){
        return buttonVisualsMap.get(buttonName) != null ? buttonVisualsMap.get(buttonName).getSpecialParameters() : "";
    }
}

