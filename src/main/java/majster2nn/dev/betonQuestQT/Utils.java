package majster2nn.dev.betonQuestQT;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.instruction.argument.Argument;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.ConditionID;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Utils {
    @NotNull
    public static String getSafeString(@Nullable ConfigurationSection base, String path, String langKey) {
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) && (section.getString(langKey) != null) ? section.getString(langKey) : "";
    }

    @NotNull
    public static List<String> getSafeStringList(ConfigurationSection base, String path, String langKey){
        ConfigurationSection section = base.getConfigurationSection(path);
        return (section != null) ? section.getStringList(langKey) : new ArrayList<>();
    }

    public static Component formatYmlString(String str) {
        String mode = BetonQuestQT.getInstance().getConfig().getString("settings.formattingMode");
        if(Objects.equals(mode, "legacy")) {
            List<String> formatableHashes = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
            Component formattedComponent = Component.text("");
            formattedComponent = formattedComponent.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

            List<String> words = List.of(str.split(" "));
            TextColor currentColor = null;
            Set<TextDecoration> decorations = new HashSet<>();

            for (String word : words) {
                StringBuilder currentText = new StringBuilder();

                boolean inFormatting = false;
                StringBuilder colorCode = new StringBuilder();

                for (int i = 0; i < word.length(); i++) {
                    char ch = word.charAt(i);

                    if (ch == '&') {
                        if (currentText.length() > 0) {
                            Component tempComponent = Component.text(currentText.toString());

                            if (currentColor != null) {
                                tempComponent = tempComponent.color(currentColor);
                            }
                            for (TextDecoration decoration : decorations) {
                                tempComponent = tempComponent.decorate(decoration);
                            }
                            formattedComponent = formattedComponent.append(tempComponent);
                            currentText = new StringBuilder();
                        }
                        if (i + 1 < word.length() && word.charAt(i + 1) == '<') {
                            decorations = new HashSet<>();
                            currentColor = null;

                            inFormatting = true;
                            i++; // Skip '<'
                        }
                        continue;
                    }

                    if (inFormatting) {
                        ch = Character.toLowerCase(ch);
                        if (ch == '>') {
                            inFormatting = false;
                            if (colorCode.length() == 6) {
                                currentColor = TextColor.color(Integer.parseInt(colorCode.toString(), 16));
                            }
                            colorCode.setLength(0);
                            continue;
                        }

                        switch (ch) {
                            case '*' -> decorations.add(TextDecoration.BOLD);
                            case '/' -> decorations.add(TextDecoration.ITALIC);
                            case '-' -> decorations.add(TextDecoration.STRIKETHROUGH);
                            case '_' -> decorations.add(TextDecoration.UNDERLINED);
                            case '#' -> colorCode = new StringBuilder();
                            default -> {
                                if (formatableHashes.contains(String.valueOf(ch))) {
                                    colorCode.append(ch);
                                }
                            }
                        }
                        continue;
                    }

                    currentText.append(ch);
                }

                if (!currentText.isEmpty()) {
                    Component tempComponent = Component.text(currentText.toString());
                    if (words.indexOf(word) != words.size() - 1) {
                        tempComponent = tempComponent.append(Component.text(" "));
                    }
                    tempComponent = tempComponent.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    if (currentColor != null) {
                        tempComponent = tempComponent.color(currentColor);
                    } else {
                        tempComponent = tempComponent.color(TextColor.color(Integer.parseInt("757575", 16)));
                    }
                    for (TextDecoration decoration : decorations) {
                        tempComponent = tempComponent.decorate(decoration);
                    }
                    formattedComponent = formattedComponent.append(tempComponent);

                }
            }

            return formattedComponent;
        }else if(Objects.equals(mode, "minimessage")){
            return MiniMessage.miniMessage().deserialize("<reset>"+str, TagResolver.resolver())
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .colorIfAbsent(NamedTextColor.GRAY);
        }

        return Component.text("???");
    }


    public static String formatLineWithVariables(String line, QuestPackage questPackage, Profile profile) {
        try {
            return new Variable<>(BetonQuest.getInstance().getVariableProcessor(), questPackage, line, Argument.STRING).getValue(profile);
        } catch (QuestException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkBqConditions(QuestPackage questPackage, String path, Profile profile){
        List<ConditionID> conditions = new ArrayList<>();
        for(String condition : Optional.ofNullable(questPackage.getConfig().getString(path)).orElse("").split(",")){
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
//                System.out.println("Status " + BetonQuest.getInstance().getQuestTypeApi().condition(profile, condition));
//            } //--DEBUG
        return BetonQuest.getInstance().getQuestTypeApi().conditions(profile, conditions);
    }
}
