/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import net.jcip.annotations.Immutable;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Immutable
public final class ServiceConfiguration {
    public static final int DEFAULT_PORT = 8543;
    public static final String POSTGRESQL_CONF = "postgresql.conf";
    public static final String POSTGRESQL_JSON = "postgresql.json";

    public static final String WS_PATH = "/ws" + "/" + POSTGRESQL_CONF;
    public static final String WS_PARAM_PATH = "/{param}";

    private ServiceConfiguration() {}
}
