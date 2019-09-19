package hello.model;

import java.io.Serializable;

public class SomeObject implements Serializable {
    private String name = "Johhny";

    public SomeObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SomeObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
