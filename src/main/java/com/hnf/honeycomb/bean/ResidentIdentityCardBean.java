package com.hnf.honeycomb.bean;

import com.fasterxml.jackson.annotation.JsonValue;
import com.hnf.honeycomb.util.TripleDesUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author admin
 */
public class ResidentIdentityCardBean implements Serializable {
    private static final long serialVersionUID = -205034799093733117L;

    private String value;

    public String getValue() {
        return value;
    }

    @JsonValue
    public String getRaw() {
        return Optional.of(this.value).map(RESIDENT_IDENTITY_CARD_DECODER).orElse(null);
    }

    public ResidentIdentityCardBean setValue(String value) {
        this.value = value;
        return this;
    }

    public ResidentIdentityCardBean replaceRaw(String idNumber) {
        this.value = Optional.ofNullable(idNumber).map(String::trim)
                .map(RESIDENT_IDENTITY_CARD_ENCODER).orElse(null);
        return this;
    }

    public static ResidentIdentityCardBean ofRaw(String idNumber) {
        final ResidentIdentityCardBean result = new ResidentIdentityCardBean();
        Optional.ofNullable(idNumber).filter(StringUtils::isNotBlank)
                .map(String::trim).ifPresent(result::replaceRaw);
        return result;
    }

    private static final Function<String, String> RESIDENT_IDENTITY_CARD_ENCODER =
            s -> TripleDesUtils.encode3Des("hnf", s);

    private static final Function<String, String> RESIDENT_IDENTITY_CARD_DECODER =
            s -> TripleDesUtils.decode3Des("hnf", s);
}
