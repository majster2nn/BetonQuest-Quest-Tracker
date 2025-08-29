package majster2nn.dev.betonQuestQT;

import fr.perrier.cupcodeapi.CupCodeAPI;
import majster2nn.dev.betonQuestQT.Events.Events;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIListener;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIManager;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.ActiveQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.FinishQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.HideQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.LockQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.Menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.Tracker.Menus.layouts.ButtonLayoutContainer;
import majster2nn.dev.betonQuestQT.Tracker.Placeholders.QuestStatus;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.data.DataBaseManager;
import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BetonQuestQT extends JavaPlugin {
    public File config;
    public FileConfiguration configData;
    public double version = 0.3;
    public GUIManager guiManager;
    private BetonQuestLoggerFactory loggerFactory;

    @Override
    public void onLoad(){
        DataBaseManager.connectToDb();
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("BetonQuest") == null){
            getLogger().warning("BetonQuest plugin not found. This plugin requires BetonQuest");
            getServer().getPluginManager().disablePlugin(this);
        }

        CupCodeAPI.enable(this);

        this.guiManager = new GUIManager();

        BetonQuest betonQuest = BetonQuest.getInstance();
        this.loggerFactory = betonQuest.getLoggerFactory();

        registerEvents(betonQuest);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new QuestStatus().register(); //
        }

        reload();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        setup();
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            PlayerDataManager.savePlayerData(player);
        }

        CupCodeAPI.disable();

        DataBaseManager.disconnectFromDB();
    }

    public void reload(){
        updateConfig();
        resetQuestPackages();
        ButtonVisualsStorage.setButtonsMaterials();
        ButtonLayoutContainer.loadMainMenuLayout();
        ButtonLayoutContainer.loadQuestCategoriesMenuslayout();
    }

    public void resetQuestPackages(){
        QuestPlaceholder.packageByName.clear();
        QuestPlaceholder.packagesByCategory.clear();
        QuestPlaceholder.tags.clear();

        QuestPlaceholder.tags.addAll(getConfig().getStringList("filters"));

        BetonQuest.getInstance().getQuestPackageManager().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageByName.put(id, questPackage);

            String questCategory = Utils.getSafeString(questPackage.getConfig(), "questParameters", "category");
            if (questCategory == null) {
                questCategory = "other";
            }

            QuestPlaceholder.packagesByCategory.put(questPackage, questCategory);

            String questTags = Utils.getSafeString(questPackage.getConfig(), "questParameters", "tags");
            List<String> tags = questTags != null ? List.of(questTags.split(",")): new ArrayList<>();
            QuestPlaceholder.packagesTags.put(questPackage, tags);
        });

    }

    public void registerEvents(BetonQuest betonQuest){
        betonQuest.getQuestRegistries().event().register("lockQuest", new LockQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("activeQuest", new ActiveQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("finishQuest", new FinishQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("hideQuest", new HideQuestFactory(loggerFactory));
    }

    public static BetonQuestQT getInstance(){
        return getPlugin(BetonQuestQT.class);
    }

    private void setup() {
        config = new File(this.getDataFolder(), "config.yml");

        if (!config.exists()) {
            config.getParentFile().mkdirs();
            this.saveDefaultConfig();
        }

        configData = YamlConfiguration.loadConfiguration(config);

        try {
            double configVersion = getConfig().getDouble("version");

            if (configVersion != version) {
                getConfig().set("version", version);
                this.saveDefaultConfig();
                configData = YamlConfiguration.loadConfiguration(config);
            }
        } catch (Exception e) {
            try{
                getConfig().set("version", version);
                this.saveDefaultConfig();
                configData = YamlConfiguration.loadConfiguration(config);
            }catch (Exception err){
                err.printStackTrace();
            }
        }
    }
    public void updateConfig(){
        config = new File(this.getDataFolder(), "config.yml");
        configData = YamlConfiguration.loadConfiguration(config);
    }

    public @NotNull FileConfiguration getConfig() {
        return this.configData;
    }

    public String getMenuTranslation(String part, Player player) {
        String lang = "en-US";

        try {
            Profile profile = BetonQuest.getInstance().getProfileProvider().getProfile(player);
            lang = BetonQuest.getInstance().getPlayerDataStorage().get(profile).getLanguage().get();
        } catch (Exception ignored) {}

        String path = "menuTranslations." + part + "." + lang;

        String result = configData.getString(path);

        return (result != null && !result.isEmpty()) ? result : "???";
    }
}
