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

    private String name;
    private Date expire;
    private String encryptedPassword;
    private String salt;
    private String token;
    private String oldToken;
    private HashSet<String> roles = new HashSet<>();

    public Login() {}

    public Login(String name, String encryptedPassword, String salt, Date expire) {
        super();
        this.name = name;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.expire = expire;
    }

    public Login(Integer id, String name, String encryptedPassword, String salt, Date expire) {
        super();
        this.id = id;
        this.name = name;
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public String getOldToken() {
        return oldToken;
    }

    public void setOldToken(String oldToken) {
        this.oldToken = oldToken;
    }

    @Override
    public String toString() {
        return "Login{" +
                "id=" + id +
                ", login='" + name + '\'' +
                ", expire=" + expire +
                ", psw='" + encryptedPassword + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
