package majster2nn.dev.betonQuestQT.Tracker;

public class QuestPart{
    String desc;
    String conditions;
    String location;

    public QuestPart(String desc, String conditions, String location){
        this.desc = desc;
        this.conditions = conditions;
        this.location = location;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
