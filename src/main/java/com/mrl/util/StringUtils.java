package com.mrl.util;

import java.util.List;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-23-15:36
 * @Description: com.mrl.util-chatgptForJava
 * @Version: 1.0
 */
public class StringUtils {
    public static boolean isEmpty(Object o){
        if (o == null) return true;
        if (o instanceof String) return "".equals(o);
        if (o instanceof List) return ((List<?>) o).size() == 0;
        if (o instanceof Map) return ((Map<?, ?>) o).size() == 0;
        return false;
    }

    public static String cqCodeEscape(String s){
        return s.replaceAll("&","&amp;")
                .replaceAll("\\[","&#91;")
                .replaceAll("]","&#93;")
                .replaceAll(",","&#44;");
    }
}
