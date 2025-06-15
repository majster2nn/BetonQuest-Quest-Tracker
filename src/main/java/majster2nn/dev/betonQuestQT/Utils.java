package majster2nn.dev.betonQuestQT;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Utils {
    public static String getSafeString(ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getString(langKey) : null;
    }

    public static List<String> getSafeStringList(ConfigurationSection base, String path, String langKey){
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getStringList(langKey) : null;
    }
}
