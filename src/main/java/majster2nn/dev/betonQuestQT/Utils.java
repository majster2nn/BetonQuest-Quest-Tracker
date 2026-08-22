package majster2nn.dev.betonQuestQT;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.identifier.ConditionIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.Profile;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Utils {
    static BetonQuestQT betonQuestQT = BetonQuestQT.getInstance();

    @Deprecated
    @NotNull
    public static String getSafeString(@Nullable ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) && (section.getString(langKey) != null) ? section.getString(langKey) : "";
    }

    @Deprecated
    /**
     * Uses minimessage to format provided string
     */
    public static Component formatString(String str) {
        return MiniMessage.miniMessage().deserialize("<reset>"+str, TagResolver.resolver())
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.GRAY);
    }


    /**
     * Resolves any BQ compatible variables inside the provided string using BQ's parser
     */
    public static String parseString(String line, QuestPackage questPackage, Profile profile) {
        try {
            return betonQuestQT.getBetonQuestApi().instructions()
                    .createForArgument(questPackage, line)
                    .string()
                    .get()
                    .getValue(profile);
        } catch (QuestException e) {
            e.printStackTrace();
            return line;
        }
    }

    public static boolean checkBqConditions(QuestPackage questPackage, String path, Profile profile){
        try {
            Argument<List<ConditionIdentifier>> conditions = betonQuestQT.getBetonQuestApi().instructions()
                    .createForArgument(
                            questPackage,
                            Optional.ofNullable(questPackage.getConfig().getString(path)).orElse(""))
                    .identifier(ConditionIdentifier.class)
                    .list()
                    .get();

            return betonQuestQT.getBetonQuestApi().conditions().manager().testAll(profile, conditions.getValue(profile));
        } catch (QuestException e) {
            throw new RuntimeException(e);
        }
    }
}
