package com.cxwmpt.demo.common.util;

import java.util.Map;

/**
 * @author Administrator
 */
public class funcUtil {
    /**
     * map value 转 "%value%"
     *
     * @param map
     * @return
     */
    public static Map MapToLike(Map map) {
        for (Object v : map.values()) {
            if (v == null || v == "") {
                continue;
            }
            //v="%"+v+"%";
            System.out.println("value= " + v);
        }
        return map;
    }

}
