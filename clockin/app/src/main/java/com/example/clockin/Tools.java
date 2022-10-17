package com.example.clockin;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Tools {

    public static Map<String,String> jsonTomap(String json) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()){
            String key = keys.next();
            String val = jsonObject.getString(key);
//            System.out.println(key+val);
            map.put(key, val);
        }
        return map;
    }

    public static boolean isOverTime(String old){
        Long timestamp = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date(timestamp));

        String[] oldDate = old.split("-");
        String[] nowDate = date.split("-");

        int isover = 0;
        for (int i=0; i< oldDate.length; i++){
            if (i==2){
                if (Integer.parseInt(oldDate[i]) > Integer.parseInt(nowDate[i])){
                    isover++;
                }
            } else {
                if (Integer.parseInt(oldDate[i]) >= Integer.parseInt(nowDate[i])){
                    isover++;
                }
            }
        }
        if (isover == 3){
            return true;
        } else {
            return false;
        }

    }


}
