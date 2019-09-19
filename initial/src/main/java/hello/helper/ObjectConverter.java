package hello.helper;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectConverter {


    public static String listMapToJson(List<Map<String, Object>> list) throws Exception{
        JSONArray json_arr=new JSONArray();
        for (Map<String, Object> map : list) {
            JSONObject json_obj=new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                try {
                    json_obj.put(key,value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            json_arr.put(json_obj);
        }
        return json_arr.toString();
    }

    public static String listMapToJsonFirstObj(List<Map<String, Object>> list) throws Exception{
        JSONObject json_obj=new JSONObject();
        for (Map<String, Object> map : list) {

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                try {
                    json_obj.put(key,value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return json_obj.toString();
        }
        return null;
    }

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}
