package majster2nn.dev.betonQuestQT;

import majster2nn.dev.betonQuestQT.events.Events;
import majster2nn.dev.betonQuestQT.menu_handlers.GUIListener;
import majster2nn.dev.betonQuestQT.menu_handlers.GUIManager;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.tracker.menus.layouts.ButtonLayoutContainer;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.BetonQuestApiService;
import org.betonquest.betonquest.api.LanguageProvider;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class BetonQuestQT extends JavaPlugin {
    public double version = 0.3;
    public GUIManager guiManager;
    public static boolean debug = false;

    private BetonQuestApi betonQuestApi;
    private String defaultLanguage;

    @Override
    public void onLoad(){

    }

    @Override
    public void onEnable() {
        setup();

        //HOOKS
        if (Bukkit.getPluginManager().getPlugin("BetonQuest") == null){
            getLogger().warning("BetonQuest plugin not found. This plugin requires BetonQuest");
            getServer().getPluginManager().disablePlugin(this);
        }else{
            BetonQuestApiService service = getServer().getServicesManager().load(BetonQuestApiService.class);

            if(service != null) {
                betonQuestApi = service.api(this);
                defaultLanguage = BetonQuest.getInstance().getComponentLoader().get(LanguageProvider.class).getDefaultLanguage();
            }
        }

        this.guiManager = new GUIManager();

        reload();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {

    }

    public void reload(){
        reloadConfig();
        resetQuestPackages();
        ButtonVisualsStorage.setButtonsMaterials();
        ButtonLayoutContainer.loadMainMenuLayout();
        ButtonLayoutContainer.loadQuestCategoriesMenusLayout();
    }

    public void resetQuestPackages(){
        QuestPlaceholder.packageByName.clear();
        QuestPlaceholder.packagesByCategory.clear();
        QuestPlaceholder.tags.clear();

        QuestPlaceholder.tags.addAll(getConfig().getStringList("filters"));

        BetonQuestQT.getInstance().getBetonQuestApi().packages().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getConfig().contains("questParameters")){return;}
            QuestPlaceholder.packageByName.put(id, questPackage);

            String questCategory = Utils.parseString(Utils.getSafeString(questPackage.getConfig(), "questParameters", "category"), questPackage, null);

            QuestPlaceholder.packagesByCategory.put(questPackage, questCategory);

            String questTags = Utils.getSafeString(questPackage.getConfig(), "questParameters", "tags");
            List<String> tags = List.of(questTags.split(","));
            QuestPlaceholder.packagesTags.put(questPackage, tags);
        });

    }

    public static BetonQuestQT getInstance(){
        return getPlugin(BetonQuestQT.class);
    }

    private void setup() {
        try {
            double configVersion = getConfig().getDouble("version");

            if (configVersion != version) {
                getConfig().set("version", version);
                saveDefaultConfig();
            }
        } catch (Exception e) {
            try{
                getConfig().set("version", version);
                saveDefaultConfig();
            }catch (Exception err){
                err.printStackTrace();
            }
        }
    }

    public String getMenuTranslation(String part, Player player) {
        String lang;
        Profile profile = BetonQuestQT.getInstance().getBetonQuestApi().profiles().getProfile(player);

        try {
            PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
            lang = playerData.getLanguage().isEmpty() || playerData.getLanguage().get().contains("default") ? defaultLanguage : playerData.getLanguage().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String result = getConfig().getConfigurationSection("menuTranslations").getString(part + "." + lang);

        if(result == null || result.isBlank()) {
            if (debug) {
                getComponentLogger().error(Component.text("Error while trying to get translation for " + part + " for language " + lang + " trying to default to " + defaultLanguage + "..."));
            }
            result  = getConfig().getConfigurationSection("menuTranslations").getString(part + "." + defaultLanguage);
            if(result == null || result.isBlank()) {
                if (debug) {
                    getComponentLogger().error(Component.text("Couldn't default to " + defaultLanguage + " for " + part + "!!! Contact administrator!!!"));
                }
                result = "???";
            }
        }

        result = Utils.parseString(result, null, profile);
        return !result.isEmpty() ? result : "???";
    }


    public BetonQuestApi getBetonQuestApi() {
        return betonQuestApi;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
