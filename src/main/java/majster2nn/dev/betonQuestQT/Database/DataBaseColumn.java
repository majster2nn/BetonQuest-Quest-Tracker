package majster2nn.dev.betonQuestQT.Database;

public enum DataBaseColumn {
    UUID("STRING"),
    QUEST("STRING"),
    STATUS("STRING");

    private final String columnType;

    DataBaseColumn(String type) {
        this.columnType = type;
    }

    public String getColumnType() {
        return columnType;
    }
}
