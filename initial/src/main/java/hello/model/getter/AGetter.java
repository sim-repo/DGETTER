package hello.model.getter;

import hello.enums.FormatEnum;
import hello.helper.StatementParser;
import java.util.List;
import java.util.Map;

public abstract class AGetter implements IGetter{
    protected String clazz;
    private String method;
    private String endpointId;
    private Map<String, List<String>> webParamsByMethod;
    private String executedFunctionName;
    private Map<String, String> funcParamByWebParam;
    private String hibernateParamsMap;
    private String resultType;
    private String description;
    private Boolean isAllAccess = false;

    @Override
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Boolean getAllAccess() {
        return isAllAccess;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public Map<String, List<String>> getWebParamsByMethod() {
        return webParamsByMethod;
    }


    public void setWebParamsByMethod(Map<String, List<String>> webParamsByMethod) {
        this.webParamsByMethod = webParamsByMethod;
    }

    @Override
    public String getExecutedFunctionName() {
        return executedFunctionName;
    }


    public void setExecutedFunctionName(String executedFunctionName) {
        this.executedFunctionName = executedFunctionName;
    }

    @Override
    public FormatEnum getResultType() {
        return FormatEnum.fromValue(resultType);
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Map<String, String> getFuncParamByWebParam() {
        return funcParamByWebParam;
    }

    @Override
    public String getFuncParamByWebParam(String webParam) {
        return funcParamByWebParam.get(webParam);
    }

    public void setFuncParamByWebParam(Map<String, String> funcParamByWebParam) {
        this.funcParamByWebParam = funcParamByWebParam;
    }


    public String getHibernateParamsMap() {
        return hibernateParamsMap;
    }

    public void setHibernateParamsMap(String hibernateParamsMap) {
        this.hibernateParamsMap = hibernateParamsMap;
        this.webParamsByMethod = StatementParser.parseWebParamsByMethod(method, hibernateParamsMap);
        this.funcParamByWebParam = StatementParser.parseFunctionParamByWebParam(hibernateParamsMap);
    }

    @Override
    public String toString() {
        return "AGetter{" +
                "method='" + method + '\'' +
                ", endpointId='" + endpointId + '\'' +
                ", webParamsByMethod=" + webParamsByMethod +
                ", executedFunctionName='" + executedFunctionName + '\'' +
                ", funcParamByWebParam=" + funcParamByWebParam +
                ", hibernateParamsMap='" + hibernateParamsMap + '\'' +
                ", resultType='" + resultType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
