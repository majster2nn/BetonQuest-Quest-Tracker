package majster2nn.dev.betonQuestQT.tracker.menus.buttons;

import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;

import java.util.Map;

public class ButtonEntry {
    Material buttonMaterial;
    Map<String, String> buttonDisplayLangMap;
    CustomModelData modelData;
    String specialParameters;

    public ButtonEntry(Material material, Map<String, String> displayLangMap){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
    }

    public ButtonEntry(Material material, Map<String, String> displayLangMap, String specialParameters){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
        this.specialParameters = specialParameters;
    }

    public ButtonEntry(Material material, Map<String, String> displayLangMap, CustomModelData modelData){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
        this.modelData = modelData;
    }

    public Material getMaterial(){
        return buttonMaterial != null ? buttonMaterial : Material.AIR;
    }

    public String getDisplayForLang(String lang){
        return buttonDisplayLangMap.getOrDefault(lang, "");
    }

    public CustomModelData getModelData(){
        return modelData != null ? modelData : null;
    }
    public String getSpecialParameters() { return specialParameters != null ? specialParameters : null; }
}
