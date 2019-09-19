package hello.security.model;


import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = Login.class)
public class Login implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    protected Integer id;

    String login;
    Date expire;
    String psw;
    String salt;

    public Login() {}

    public Login(String login, String psw, String salt, Date expire) {
        super();
        this.login = login;
        this.psw = psw;
        this.salt = salt;
        this.expire = expire;
    }

    public String getClazz() {
        return Login.class.getName();
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
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
    public Date getExpire() {
        return expire;
    }
    public void setExpire(Date expire) {
        this.expire = expire;
    }
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }


    @Override
    public String toString() {
        return "Login{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", expire=" + expire +
                ", psw='" + psw + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
