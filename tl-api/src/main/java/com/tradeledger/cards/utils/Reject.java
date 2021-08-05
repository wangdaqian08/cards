package com.tradeledger.cards.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by daqwang on 5/8/21.
 */
public final class Reject {
    public Reject() {
    }

    public static void ifNullOrEmpty(Object obj) {
        if (obj instanceof String) {
            ifBlank((String) obj);
        }else {
            ifNull(obj);
        }
    }

    public static void ifNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Null not allowed!");
        }
    }

    public static void ifBlank(String str) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException("Empty string is not allowed!");
        }
    }
}
