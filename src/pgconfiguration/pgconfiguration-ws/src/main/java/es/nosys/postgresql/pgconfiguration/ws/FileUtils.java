/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;

/**
 * Created: 12/16/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void moveFileAtomically(Lock persistLock, Path origPath, Path dstPath) throws IOException {
        persistLock.lock();
        try {
            try {
                Files.move(
                        origPath, dstPath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
                );
            } catch(IOException e) {
                logger.error("Error persisting the " + ServiceConfiguration.POSTGRESQL_JSON + " file", e);
                throw e;
            } finally {
                if(Files.exists(origPath)) {
                    try {
                        Files.delete(origPath);
                    } catch(IOException e) {
                        logger.error("Cannot delete temp file " + origPath.getFileName(), e);
                        throw e;
                    }
                }
            }
        } finally {
            persistLock.unlock();
        }
    }

    public static String getTempPersistFileName(String baseFileName) {
        return baseFileName + "." + ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    }
}
