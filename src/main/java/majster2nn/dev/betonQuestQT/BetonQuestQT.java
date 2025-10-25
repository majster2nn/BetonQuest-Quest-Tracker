package majster2nn.dev.betonQuestQT;

import fr.perrier.cupcodeapi.CupCodeAPI;
import majster2nn.dev.betonQuestQT.data.DataBaseHandler;
import majster2nn.dev.betonQuestQT.data.MySqlManager;
import majster2nn.dev.betonQuestQT.data.PlayerDataManager;
import majster2nn.dev.betonQuestQT.data.SqliteManager;
import majster2nn.dev.betonQuestQT.data.asyncSaver.SavePlayerDataThread;
import majster2nn.dev.betonQuestQT.events.Events;
import majster2nn.dev.betonQuestQT.hooks.betonquest.events.ActiveQuestFactory;
import majster2nn.dev.betonQuestQT.hooks.betonquest.events.FinishQuestFactory;
import majster2nn.dev.betonQuestQT.hooks.betonquest.events.HideQuestFactory;
import majster2nn.dev.betonQuestQT.hooks.betonquest.events.LockQuestFactory;
import majster2nn.dev.betonQuestQT.hooks.papi.QuestStatus;
import majster2nn.dev.betonQuestQT.menu_handlers.GUIListener;
import majster2nn.dev.betonQuestQT.menu_handlers.GUIManager;
import majster2nn.dev.betonQuestQT.tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.tracker.menus.buttons.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.tracker.menus.layouts.ButtonLayoutContainer;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public final class BetonQuestQT extends JavaPlugin {
    public File config;
    public FileConfiguration configData;
    public double version = 0.3;
    public GUIManager guiManager;
    private BetonQuestLoggerFactory loggerFactory;
    public static boolean debug = false;
    public DataBaseHandler dataBaseHandler;
    public SavePlayerDataThread savePlayerDataThread;

    @Override
    public void onLoad(){

    }

    @Override
    public void onEnable() {
        setup();

        configData = YamlConfiguration.loadConfiguration(config);

        if(configData.contains("databaseType")){
            String dataBaseType = Objects.requireNonNullElse(configData.getString("databaseType"), "");

            switch(dataBaseType.toLowerCase()){
                case "mysql" -> {
                    dataBaseHandler = new MySqlManager();
                }
                case "mariadb" -> {
                    getComponentLogger().info(Component.text("MariaDB is not yet supported!!! Switching to default SQLite database..."));
                    dataBaseHandler = new SqliteManager();
                }
                default -> {
                    dataBaseHandler = new SqliteManager();
                }
            }
        }else{
            dataBaseHandler = new SqliteManager();
        }

        dataBaseHandler.init();
        savePlayerDataThread = new SavePlayerDataThread();
        savePlayerDataThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{savePlayerDataThread.end(dataBaseHandler);}));

        //HOOKS
        if (Bukkit.getPluginManager().getPlugin("BetonQuest") == null){
            getLogger().warning("BetonQuest plugin not found. This plugin requires BetonQuest");
            getServer().getPluginManager().disablePlugin(this);
        }

        CupCodeAPI.enable(this);


        this.guiManager = new GUIManager();

        BetonQuest betonQuest = BetonQuest.getInstance();
        this.loggerFactory = betonQuest.getLoggerFactory();

        registerEvents(betonQuest);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new QuestStatus().register();
        }

        reload();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            PlayerDataManager.savePlayerData(player);
        }

        CupCodeAPI.disable();

        savePlayerDataThread.end(dataBaseHandler);
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

            String questCategory = Utils.formatLineWithVariables(Utils.getSafeString(questPackage.getConfig(), "questParameters", "category"), questPackage, null);

            QuestPlaceholder.packagesByCategory.put(questPackage, questCategory);

            String questTags = Utils.getSafeString(questPackage.getConfig(), "questParameters", "tags");
            List<String> tags = List.of(questTags.split(","));
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

        File dbConfig = new File(this.getDataFolder(), "dbConfig.properties");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        if (!dbConfig.exists()) {
            saveResource("dbConfig.properties", false);
            getLogger().info("Created default dbConfig.properties");
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
            PlayerData playerData = BetonQuest.getInstance().getPlayerDataStorage().get(profile);
            lang = playerData.getLanguage().isPresent() ? playerData.getLanguage().get() : BetonQuest.getInstance().getDefaultLanguage();
        } catch (Exception ignored) {}

        String result = Utils.getSafeString(configData.getConfigurationSection("menuTranslations"), part, lang);

        if(result.isBlank() || result.isEmpty()) {
            if (debug) {
                getComponentLogger().error(Component.text("Error while trying to get translation for " + part + " for language " + lang + " trying to default to " + BetonQuest.getInstance().getDefaultLanguage() + "..."));
            }
            result = Utils.getSafeString(configData.getConfigurationSection("menuTranslations"), part, BetonQuest.getInstance().getDefaultLanguage());
            if(result.isBlank() || result.isEmpty()) {
                if (debug) {
                    getComponentLogger().error(Component.text("Couldn't default to " + BetonQuest.getInstance().getDefaultLanguage() + " for " + part + "!!! Contact administrator!!!"));
                }
                result = "???";
            }
        }

        result = Utils.formatLineWithVariables(result, null);
        return !result.isEmpty() ? result : "???";
    }
}
