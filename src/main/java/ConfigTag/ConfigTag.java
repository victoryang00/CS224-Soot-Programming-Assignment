package ConfigTag;

import soot.tagkit.Tag;

import java.nio.charset.StandardCharsets;

public class ConfigTag implements Tag {
    public static final String IDENTIFIER = "ConfigTag";
    protected String info;

    public ConfigTag(String info) {
        this.info = info;
    }

    public String getName() {
        return IDENTIFIER;
    }

    public byte[] getValue() {
        return info.getBytes(StandardCharsets.UTF_8);
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String value) {
        info = value;
    }

    public String toString() {
        return info;
    }

}