/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.model;

import java.util.List;

/**
 * Created: 12/13/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
public class Param {
    public enum Context {
        user,
        superuser,
        postmaster,
        backend,
        sighup;
    }

    public enum Vartype {
        REAL,
        STRING,
        INTEGER,
        ENUM,
        BOOLEAN;
    }

    private String param;
    private String value;
    private String unit;
    private Context context;
    private String category;
    private Vartype vartype;
    private List<String> enumvalues;
    private String minval;
    private String maxval;
    private String defaultvalue;
    private String description;
    private String extra;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Vartype getVartype() {
        return vartype;
    }

    public void setVartype(Vartype vartype) {
        this.vartype = vartype;
    }

    public List<String> getEnumvalues() {
        return enumvalues;
    }

    public void setEnumvalues(List<String> enumvalues) {
        this.enumvalues = enumvalues;
    }

    public String getMinval() {
        return minval;
    }

    public void setMinval(String minval) {
        this.minval = minval;
    }

    public String getMaxval() {
        return maxval;
    }

    public void setMaxval(String maxval) {
        this.maxval = maxval;
    }

    public String getDefaultvalue() {
        return defaultvalue;
    }

    public void setDefaultvalue(String defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
