package majster2nn.dev.betonQuestQT.Tracker.Menus.layouts;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;

public class ButtonLayoutContainer {
    private static final HashMap<Integer, String> mainMenuLayout = new HashMap<>();
    private static final HashMap<Integer, String> questCategoriesMenus = new HashMap<>();

    public static void loadMainMenuLayout(){
        mainMenuLayout.clear();
        ConfigurationSection menuLayout = BetonQuestQT.getInstance().getConfig().getConfigurationSection("layouts.mainMenu");
        if(menuLayout == null){
            BetonQuestQT.getInstance().getComponentLogger().error(Component.text("Layout section is missing mainMenu section!!! This is a critical error, contact administrator."));
            return;
        }

        try {
            List<String> letterList = List.of(String.join("", menuLayout.getStringList("layout")).split(""));

            HashMap<String, String> buttonMapping = new HashMap<>();
            for (String key : menuLayout.getConfigurationSection("buttons").getKeys(false)) {
                buttonMapping.put(key, menuLayout.getString("buttons." + key));
            }

            for(int i = 0; i < letterList.size(); i++){
                mainMenuLayout.put(i, buttonMapping.getOrDefault(letterList.get(i), ""));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static void loadQuestCategoriesMenuslayout(){
        System.out.println("loaded quest menu layout");
        questCategoriesMenus.clear();
        ConfigurationSection menuLayout = BetonQuestQT.getInstance().getConfig().getConfigurationSection("layouts.questMenus");
        if(menuLayout == null){
            BetonQuestQT.getInstance().getComponentLogger().error(Component.text("Layout section is missing questMenus section!!! This is a critical error, contact administrator."));
            return;
        }

        try {
            List<String> letterList = List.of(String.join("", menuLayout.getStringList("layout")).split(""));

            HashMap<String, String> buttonMapping = new HashMap<>();
            for (String key : menuLayout.getConfigurationSection("buttons").getKeys(false)) {
                buttonMapping.put(key, menuLayout.getString("buttons." + key));
            }

            for(int i = 0; i < letterList.size(); i++){
                questCategoriesMenus.put(i, buttonMapping.getOrDefault(letterList.get(i), "null"));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static HashMap<Integer, String> getMainMenuLayout() {
        return mainMenuLayout;
    }

    public static HashMap<Integer, String> getQuestCategoriesMenus() {
        return questCategoriesMenus;
    }
}
