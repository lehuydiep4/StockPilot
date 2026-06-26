package org.phalkun.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtil {
    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);
    private static ConnectionProvider provider;

    static {
        Properties props = new Properties();
        String configFile = "db-local.properties";
        InputStream is = DbUtil.class.getClassLoader().getResourceAsStream(configFile);
        
        if (is == null) {
            configFile = "db.properties.example";
            logger.info("db-local.properties not found. Falling back to db.properties.example");
            is = DbUtil.class.getClassLoader().getResourceAsStream(configFile);
        } else {
            logger.info("Loading local database configurations from db-local.properties");
        }

        try {
            if (is == null) {
                logger.warn("Neither db-local.properties nor db.properties.example was found in classpath. Defaulting to default H2 database.");
                provider = new H2ConnectionProvider("jdbc:h2:./stockpilot;DB_CLOSE_DELAY=-1", "sa", "");
            } else {
                try (InputStream input = is) {
                    props.load(input);
                }
                String dbType = props.getProperty("db.type", "h2").toLowerCase().trim();
                
                if ("postgres".equals(dbType) || "postgresql".equals(dbType)) {
                    logger.info("Initializing PostgresConnectionProvider...");
                    provider = new PostgresConnectionProvider(
                        props.getProperty("db.postgres.url"),
                        props.getProperty("db.postgres.user"),
                        props.getProperty("db.postgres.password")
                    );
                } else {
                    logger.info("Initializing H2ConnectionProvider...");
                    provider = new H2ConnectionProvider(
                        props.getProperty("db.h2.url"),
                        props.getProperty("db.h2.user"),
                        props.getProperty("db.h2.password")
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database connection provider", e);
            // Fallback to local H2 to avoid complete boot failure
            provider = new H2ConnectionProvider("jdbc:h2:./stockpilot;DB_CLOSE_DELAY=-1", "sa", "");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (provider == null) {
            throw new SQLException("Connection provider has not been initialized.");
        }
        return provider.getConnection();
    }
}
