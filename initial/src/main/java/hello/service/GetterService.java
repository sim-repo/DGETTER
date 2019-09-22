package hello.service;

import hello.config.AppConfig;
import hello.dao.Dao;
import hello.model.connectors.JdbConnector;
import hello.model.getter.DbGetter;
import hello.model.getter.IGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("getterService")
@Scope("singleton")
public class GetterService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Dao dao;

    // PUBLIC METHODS:

    public String exec(String endpointId, String method, Map<String, String> params) throws Exception {
        DbGetter getter = appConfig.getDbGetter(endpointId, method);
        String sql = getSQLStatement(getter, params);
        return exec(getter, sql, getter.getEndpointId());
    }


    public String exec(String endpointId, String method,  String params) throws Exception {
        DbGetter getter = appConfig.getDbGetter(endpointId, method);
        Map<String, String> parsedParams = getQueryParameters(params);
        String sql = getSQLStatement(getter, parsedParams);
        if (sql==null) return null;
        return exec(getter, sql, getter.getEndpointId());
    }


    // PRIVATE METHODS:

    private String getSQLStatement(IGetter getter, Map<String, String> params){
        JdbConnector connector = appConfig.getJdbConnector(getter.getEndpointId());
        if (connector == null) return null;
        switch (connector.getCode()) {
            case sqlserver:
                return getSQLServerStatement(getter, params);
            case mysql:
                return getMySQLStatement(getter, params);
            case redis:
                return null;
            case postgress:
                return null;
            default:
                return null;
        }
    }

    private String getSQLServerStatement(IGetter getter, Map<String, String> params) {
        List<String> funcParamKeys = new ArrayList<String>();
        List<String> funcParamValues = new ArrayList<String>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String webParam = entry.getKey();
            String functionParam = getter.getFuncParamByWebParam(webParam);
            if (functionParam != null && functionParam != "") {
                funcParamKeys.add(functionParam);
                funcParamValues.add(entry.getValue());
            }
        }
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append(getter.getExecutedFunctionName() + " ");

        if (funcParamKeys.size() > 0) {
            for(int i=0; i < funcParamKeys.size(); i++){
                sqlStatement.append(funcParamKeys.get(i)+"="+funcParamValues.get(i)+",");
            }
            sqlStatement.deleteCharAt(sqlStatement.length()-1);
        }
        return sqlStatement.toString();
    }


    private String getMySQLStatement(IGetter getter, Map<String, String> params) {
        List<String> funcParamValues = new ArrayList<String>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String webParam = entry.getKey();
            String functionParam = getter.getFuncParamByWebParam(webParam);
            if (functionParam != null && functionParam != "") {
                funcParamValues.add(entry.getValue());
            }
        }

        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append(getter.getExecutedFunctionName()+"(");
        for(int i=0; i < funcParamValues.size(); i++){
            sqlStatement.append("'"+funcParamValues.get(i)+"',");
        }
        sqlStatement.deleteCharAt(sqlStatement.length()-1);
        sqlStatement.append(")");
        return sqlStatement.toString();
    }


    private String exec(IGetter getter, String sql, String endpointId) throws Exception {
        switch (getter.getResultType()) {
            case flatXML:
                return getFlatXml(sql, endpointId);
            case flatJSON:
                return getFlatJson(sql, endpointId);
            case complexJSON:
                return getComplexJson(sql, endpointId);
            case firstFlatJSON:
                return getFlatJsonFirstObj(sql, endpointId);
            default: break;
        }
        return "Something Wrong: check [routing db exec].[result handler]";
    }


    private String getFlatJson(String sql, String endpoint) throws Exception {
        String res = dao.getFlatJsonArray(getJdbcTemplate(endpoint), sql);
        return res;
    }


    private String getFlatXml(String sql, String endpoint) throws Exception {
        String res = dao.getFlatXml(getJdbcTemplate(endpoint), sql);
        return res;
    }

    private String getComplexJson(String sql, String endpoint) throws Exception {
        String res = dao.getComplexJsonArray(getJdbcTemplate(endpoint), sql);
        return res;
    }

    private String getFlatJsonFirstObj(String sql, String endpoint) throws Exception {
        String res = dao.getFlatJsonFirstObj(getJdbcTemplate(endpoint), sql);
        return res;
    }


    private static  Map<String, String> getQueryParameters(String queryString) {
        Map<String, String> queryParameters = new HashMap<>();
        String encoded = "";
        try {
            encoded = URLDecoder.decode( queryString, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(encoded)) {
            return queryParameters;
        }

        String[] parameters = encoded.split("&");

        for (String parameter : parameters) {
            String[] keyValuePair = parameter.split("=");
            queryParameters.put(keyValuePair[0], keyValuePair[1]);
        }
        return queryParameters;
    }

    private JdbcTemplate getJdbcTemplate(String endpointId) {
        return appConfig.getJdbcTemplate(endpointId);
    }
}
