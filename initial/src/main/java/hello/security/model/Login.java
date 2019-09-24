package hello.security.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

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
    private String encryptedPassword;
    private String salt;
    private String token;
    private HashSet<String> roles;

    public Login() {}

    public Login(String login, String encryptedPassword, String salt, Date expire) {
        super();
        this.login = login;
        this.encryptedPassword = encryptedPassword;
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
    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = this.encryptedPassword;
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
    public HashSet<String> getRoles() {
        return roles;
    }
    public void setRoles(HashSet<String> roles) {
        this.roles = roles;
    }
    public void setRole(String role){
        roles.add(role);
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
                ", psw='" + encryptedPassword + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
