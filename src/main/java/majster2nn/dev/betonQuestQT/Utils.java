package majster2nn.dev.betonQuestQT;

import org.bukkit.configuration.ConfigurationSection;

public class Utils {
    public static String getSafeString(ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getString(langKey) : null;
    }
}
