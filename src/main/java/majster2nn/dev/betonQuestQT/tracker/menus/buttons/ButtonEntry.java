package majster2nn.dev.betonQuestQT.tracker.menus.buttons;

import org.bukkit.Material;

import java.util.Map;

public class ButtonEntry {
    Material buttonMaterial;
    Map<String, String> buttonDisplayLangMap;
    String modelData;
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

    public ButtonEntry(Material material, Map<String, String> displayLangMap, String specialParameters, String modelData){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
        this.specialParameters = specialParameters;
        this.modelData = modelData;
    }

    public Material getMaterial(){
        return buttonMaterial != null ? buttonMaterial : Material.AIR;
    }

    public String getDisplayForLang(String lang){
        return buttonDisplayLangMap.getOrDefault(lang, "");
    }

    public String getModelData(){
        return modelData != null ? modelData : null;
    }
    public String getSpecialParameters() { return specialParameters != null ? specialParameters : null; }
}
