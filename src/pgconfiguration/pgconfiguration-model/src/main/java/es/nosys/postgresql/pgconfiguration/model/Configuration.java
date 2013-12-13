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
public class Configuration {
    private List<Param> postgresqlconf;

    public List<Param> getPostgresqlconf() {
        return postgresqlconf;
    }

    public void setPostgresqlconf(List<Param> postgresqlconf) {
        this.postgresqlconf = postgresqlconf;
    }
}
