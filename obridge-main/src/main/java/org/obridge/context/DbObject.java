package org.obridge.context;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DbObject {

    private String owner;
    private String name;

    public DbObject(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public DbObject() {
    }

    public String toConcatenated() {
        return this.owner + "." + this.name;
    }

    public String toSQL() {
        return "SELECT '" + this.owner + "', '" + this.name + "' FROM dual ";
    }
}
