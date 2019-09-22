package hello.security.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = Login.class)
public class Login implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Integer id;

    private String login;
    private Date expire;
    private String encriptedPassword;
    private String salt;
    private String token;
    ArrayList<Role> roles;

    public Login() {}

    public Login(String login, String encriptedPassword, String salt, Date expire) {
        super();
        this.login = login;
        this.encriptedPassword = encriptedPassword;
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
    public String getEncriptedPassword() {
        return encriptedPassword;
    }
    public void setEncriptedPassword(String encriptedPassword) {
        this.encriptedPassword = encriptedPassword;
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
    public ArrayList<Role> getRoles() {
        return roles;
    }
    public void setRoles(ArrayList<Role> roles) {
        this.roles = roles;
    }
    public void setRole(Role role){
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Login{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", expire=" + expire +
                ", psw='" + encriptedPassword + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
