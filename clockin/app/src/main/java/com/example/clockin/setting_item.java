package com.example.clockin;

import androidx.annotation.Nullable;

public class setting_item {

    private String id;
    private String vname;
    private String value;
    private String hint = "";


    public setting_item(String id, String vname, String value) {
        this.id = id;
        this.vname = vname;
        this.value = value;
    }


    public setting_item(String id, String vname, String value, @Nullable String hint) {
        this.id = id;
        this.vname = vname;
        this.value = value;
        this.hint = hint;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
