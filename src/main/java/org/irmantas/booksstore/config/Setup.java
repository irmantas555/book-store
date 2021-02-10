package org.irmantas.booksstore.config;

import io.r2dbc.spi.ConnectionFactory;
import org.irmantas.booksstore.model.Book;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.LongStream;

@Component
public class Setup {

    Flux<Book> bookFlux;

    String filePath;

    Path path;

    public Setup() {
        this.filePath = "src/main/resources/books.txt";;
        this.path = Paths.get(filePath);
    }

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }

    @PostConstruct
    void getBooks() {
        Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(strings -> Flux.fromIterable(strings))
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new Book(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice()))
                .subscribe();
    }

    long generateBarcode() {
        Random r1 = new Random();
        long[] longs = r1.longs(1, 100000000000L, 1000000000000L).toArray();
        return longs[0];

    }

    BigDecimal genetaratePrice() {
        Random r2 = new Random();
        return BigDecimal.valueOf(Double.valueOf(r2.nextInt(3000)) / 100 + 10.00);
    }

    int getQty() {
        Random r1 = new Random();
        return r1.nextInt(30) + 5;
    }
}
