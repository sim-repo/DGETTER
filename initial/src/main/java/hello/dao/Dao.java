package hello.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.helper.ObjectConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("dao")
public class Dao {

    public String getFlatJsonArray(JdbcTemplate currentJDBCTemplate, String sql) throws  Exception {
        List<Map<String,Object>> list =  currentJDBCTemplate.queryForList(sql);
        return ObjectConverter.listMapToJson(list);
    }


    public String getFlatJsonFirstObj(JdbcTemplate currentJDBCTemplate, String sql) throws Exception{
        List<Map<String,Object>> list =  currentJDBCTemplate.queryForList(sql);
        return listMapToJsonFirstObj(list);
    }

    public String getComplexJsonArray(JdbcTemplate currentJDBCTemplate, String sql) throws Exception {
        List<Map<String,Object>> list =  currentJDBCTemplate.queryForList(sql);
        StringBuilder result = new StringBuilder();
        for(Map<String, Object> map: list){
            for(Map.Entry<String, Object> pair: map.entrySet()){
                result.append(pair.getValue());
            }
        }
        JSONObject jObject = XML.toJSONObject(result.toString());
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(jObject.toString(), Object.class);
        String output = mapper.writeValueAsString(json);
        return output;
    }

    public String getFlatXml(JdbcTemplate currentJDBCTemplate, String sql) throws Exception {
        List<Map<String,Object>> list =  currentJDBCTemplate.queryForList(sql);
        StringBuilder result = new StringBuilder();

        for(Map<String, Object> map: list){
            for(Map.Entry<String, Object> pair: map.entrySet()){
                result.append(pair.getValue());
            }
        }
        JSONObject jObject = XML.toJSONObject(result.toString());
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(jObject.toString(), Object.class);
        String output = mapper.writeValueAsString(json);
        return output;
    }

    private String listMapToJsonFirstObj(List<Map<String, Object>> list){

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
}
