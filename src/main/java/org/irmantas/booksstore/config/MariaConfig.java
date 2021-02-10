package org.irmantas.booksstore.config;

import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableR2dbcRepositories
@Profile("!test")
public class MariaConfig {
    //    @Bean
//    public MariadbConnectionFactory mariadbConnectionFactory() {
//        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        Properties props = new Properties();
//        try (InputStream f = loader.getResourceAsStream("db.properties")) {
//            props.load(f);
//            props.forEach((o, o2) -> System.out.println(o.toString() +  o2.toString()));
//            return new MariadbConnectionFactory(MariadbConnectionConfiguration.builder()
//                    .host(props.getProperty("host"))
//                    .port(Integer.parseInt(props.getProperty("port")))
//                    .username(props.getProperty("username"))
//                    .password(props.getProperty("password"))
//                    .database(props.getProperty("database"))
//                    .build());
//        }
//        catch (IOException e) {
//            System.out.println(e.getMessage());
//            return null;
//        }
//    }
    @Bean
    @Profile("!test")
    @Primary
    public MariadbConnectionFactory connectionFactory() {
        return new MariadbConnectionFactory(MariadbConnectionConfiguration.builder()
                .host("192.168.1.67")
                .port(3306)
                .username("springs")
                .password("springs")
                .database("databbook_storease")
                .build());
    }

    @Bean
    DatabaseClient client() {
        return DatabaseClient.create(connectionFactory());
    }


}
