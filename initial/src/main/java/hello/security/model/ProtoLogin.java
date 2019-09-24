package hello.security.model;

import java.io.Serializable;

public class ProtoLogin implements Serializable {
    private Integer id;
    private String name;
    private Integer expireInDays;
    private String password;

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

    public Integer getExpireInDays() {
        return expireInDays;
    }

    public void setExpireInDays(Integer expireInDays) {
        this.expireInDays = expireInDays;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ProtoLogin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", expireInDays=" + expireInDays +
                ", password='" + password + '\'' +
                '}';
    }
}
