package com.college.library.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {}

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration cfg = new Configuration().configure();

            String envUrl = System.getenv("dbUrl") != null ? System.getenv("dbUrl")
                    : (System.getenv("jdbcUrl") != null ? System.getenv("jdbcUrl")
                    : (System.getenv("JDBC_URL") != null ? System.getenv("JDBC_URL") : System.getenv("DB_URL")));
            String envHost = System.getenv("dbHost") != null ? System.getenv("dbHost") : System.getenv("DB_HOST");
            String envPort = System.getenv("dbPort") != null ? System.getenv("dbPort") : System.getenv("DB_PORT");
            String envName = System.getenv("dbName") != null ? System.getenv("dbName") : System.getenv("DB_NAME");

            if (envUrl != null && !envUrl.isBlank()) {
                cfg.setProperty("hibernate.connection.url", envUrl);
            } else if (envHost != null && !envHost.isBlank()) {
                String port = (envPort != null && !envPort.isBlank()) ? envPort : "3306";
                String name = (envName != null && !envName.isBlank()) ? envName : "digital_library";
                cfg.setProperty("hibernate.connection.url", "jdbc:mysql://" + envHost + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true");
            }

            String envUser = System.getenv("dbUsername") != null ? System.getenv("dbUsername")
                    : (System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getenv("DB_USERNAME"));
            if (envUser != null && !envUser.isBlank()) {
                cfg.setProperty("hibernate.connection.username", envUser);
            }

            String envPass = System.getenv("dbPassword") != null ? System.getenv("dbPassword")
                    : (System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : System.getenv("DB_PASSWORD"));
            if (envPass != null && !envPass.isBlank()) {
                cfg.setProperty("hibernate.connection.password", envPass);
            }

            return cfg.buildSessionFactory();
        } catch (Exception e) {
            log.error("Failed to initialize Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }
}
