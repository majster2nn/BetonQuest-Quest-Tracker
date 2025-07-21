package majster2nn.dev.betonQuestQT.Tracker.Menus.buttons;

public enum ButtonActions {
    PREVIOUS_PAGE("PreviousPage"),
    NEXT_PAGE("NextPage"),

    NOTHING("");

    final String actionType;

    ButtonActions(String str){
        actionType = str;
    }

    public String getAction(){
        return actionType;
    }
}
