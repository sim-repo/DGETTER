package hello.model.connectors;

import hello.enums.ConnectorEnum;

import java.io.Serializable;

public class JdbConnector implements Serializable {
    private Integer id;
    private String code = "";
    private String driverClassName = "";
    private String dbURL= "";
    private String dbServerName = "";
    private String dbName = "";
    private String login = "";
    private String psw = "";
    private String endpointId = "";


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCode(ConnectorEnum code) {
        this.code = code.toValue();
    }

    public ConnectorEnum getCode() {
        return ConnectorEnum.fromValue(code);
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDbURL() {
        return dbURL;
    }

    public void setDbURL(String dbURL) {
        this.dbURL = dbURL;
    }

    public String getDbServerName() {
        return dbServerName;
    }

    public void setDbServerName(String dbServerName) {
        this.dbServerName = dbServerName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public String toString() {
        return "JdbConnector{" +
                "code='" + code + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", dbURL='" + dbURL + '\'' +
                ", dbServerName='" + dbServerName + '\'' +
                ", dbName='" + dbName + '\'' +
                ", login='" + login + '\'' +
                ", psw='" + psw + '\'' +
                ", endpointId='" + endpointId + '\'' +
                '}';
    }
}
