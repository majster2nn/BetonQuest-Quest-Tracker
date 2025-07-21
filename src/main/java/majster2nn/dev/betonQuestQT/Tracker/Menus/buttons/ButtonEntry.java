package majster2nn.dev.betonQuestQT.Tracker.Menus.buttons;

import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;

import java.util.Map;

public class ButtonEntry {
    Material buttonMaterial;
    Map<String, String> buttonDisplayLangMap;
    CustomModelData modelData;
    int slot;

    public ButtonEntry(Material material, Map<String, String> displayLangMap){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
    }

    public ButtonEntry(Material material, Map<String, String> displayLangMap, int slot){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
        this.slot = slot;
    }

    public ButtonEntry(Material material, Map<String, String> displayLangMap, CustomModelData modelData){
        this.buttonMaterial = material;
        this.buttonDisplayLangMap = displayLangMap;
        this.modelData = modelData;
    }

    public Material getMaterial(){
        return buttonMaterial != null ? buttonMaterial : Material.BARRIER;
    }

    public String getDisplayForLang(String lang){
        return buttonDisplayLangMap.getOrDefault(lang, "NO SUCH LANGUAGE FOUND!!!");
    }

    public CustomModelData getModelData(){
        return modelData != null ? modelData : null;
    }

    public int getSlot(){
        return 0 <= slot && slot <= 53 ? slot : 0;
    }
}
