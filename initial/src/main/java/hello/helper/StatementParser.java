package hello.helper;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementParser {

    public static Map<String, List<String>> parseWebParamsByMethod(String method, String hibernateParamsMap) {
        Map<String, List<String>> res = new HashMap<>();

        if (StringUtils.isEmpty(hibernateParamsMap)) {
            return res;
        }
        String[] parameters = hibernateParamsMap.split(";");

        List<String> webParamNames = new ArrayList<String>();

        for (String parameter : parameters) {
            String[] keyValuePair = parameter.split(":");
            webParamNames.add(keyValuePair[0]);
        }
        res.put(method, webParamNames);

        return res;
    }

    public static Map<String, String> parseFunctionParamByWebParam(String hibernateParamsMap) {
        Map<String, String> res = new HashMap<>();
        if (StringUtils.isEmpty(hibernateParamsMap)) {
            return res;
        }

        String[] parameters = hibernateParamsMap.split(";");

        for (String parameter : parameters) {
            String[] keyValuePair = parameter.split(":");
            if (keyValuePair.length == 2) {
                res.put(keyValuePair[0], keyValuePair[1]);
            } else {
                throw new IllegalArgumentException(hibernateParamsMap);
            }
        }
        return res;
    }


}
