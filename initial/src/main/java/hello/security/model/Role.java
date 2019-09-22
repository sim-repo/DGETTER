package hello.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hello.model.getter.IGetter;

import java.io.Serializable;
import java.util.ArrayList;


public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    protected Integer id;

    String name;
    ArrayList<IGetter> getters;

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

    public ArrayList<IGetter> getGetters() {
        return getters;
    }

    public void setGetters(ArrayList<IGetter> getters) {
        this.getters = getters;
    }
}
