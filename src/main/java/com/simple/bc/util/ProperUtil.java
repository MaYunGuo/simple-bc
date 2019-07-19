package com.simple.bc.util;


import com.alibaba.fastjson.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.simple.bc.commdef.GenericDef.PROPER_TOOL_CONF_SUFFIX;
import static com.simple.bc.commdef.GenericDef.PROPER_TOOL_INFO_KEY;


public class ProperUtil {

    private static Properties props;

    static {
        props = new Properties();
        try {

            props.load(new FileInputStream("/opt/simple-bc/conf/simple-bc.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static JSONObject getToolInfo() {
        String eqptStr = props.getProperty(PROPER_TOOL_INFO_KEY);
        JSONObject jsonObject = JSONObject.parseObject(eqptStr);
        return jsonObject;
    }


    public static String getToolConf(String tool_id, String conf_key) {
        String key = tool_id + PROPER_TOOL_CONF_SUFFIX;
        String ptnStr = props.getProperty(key);
        JSONObject jsonObject = JSONObject.parseObject(ptnStr);
        if (jsonObject.containsKey(conf_key)) {
            return jsonObject.getString(conf_key).toString();
        }
        return null;
    }


}
