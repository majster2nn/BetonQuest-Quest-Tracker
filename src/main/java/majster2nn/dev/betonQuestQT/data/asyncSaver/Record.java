package majster2nn.dev.betonQuestQT.data.asyncSaver;

public class Record {
    final String key;
    final String value;
    final String column;

    public Record(String key, String value, String column) {
        this.key = key;
        this.value = value;
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
