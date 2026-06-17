package mars.tripplanappbackend.mypage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetLanguage {
    EN("en"),
    JA("ja"),
    ZH_CN("zh-CN"),
    ZH_TW("zh-TW"),
    VI("vi"),
    TH("th"),
    ID("id"),
    FR("fr"),
    ES("es"),
    RU("ru"),
    DE("de"),
    IT("it");

    private final String code;

    @JsonCreator
    public static TargetLanguage from(String value) {
        for (TargetLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(value)) {
                return lang;
            }
        }
        return EN;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
