package majster2nn.dev.betonQuestQT.tracker;

public record QuestPart(String desc, String conditions, String location){
    public String getDesc() {
        return desc;
    }

    public String getConditions() {
        return conditions;
    }

    public String getLocation() {
        return location;
    }

}
