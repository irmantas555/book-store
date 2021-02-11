package org.irmantas.booksstore.util;


import org.irmantas.booksstore.model.Book;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@SpringBootApplication
@EnableTransactionManagement
public
class InfrastructureConfiguration {

    String filePath = "src/main/resources/books.txt";
    Path path = Paths.get(filePath);
    Flux<Book> bookFlux;

    @Autowired
    DatabaseClient client;

    public InfrastructureConfiguration() {
    }

    public void setUp(){
        Hooks.onOperatorDebug();

        List<String> statements = Arrays.asList(
                "DROP TABLE IF EXISTS books;",
                "CREATE TABLE `books` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "  `name` varchar(40) DEFAULT '',\n" +
                        "  `author` varchar(40) NOT NULL DEFAULT '',\n" +
                        "  `barcode` bigint(20) NOT NULL DEFAULT '0',\n" +
                        "  `qty` int(11) DEFAULT '0',\n" +
                        "  `price` decimal(15,2) NOT NULL DEFAULT '0.00');");

        statements
                .forEach(it -> client.sql(it) //
                        .fetch() //
                        .rowsUpdated() //
                        .as(StepVerifier::create) //
                        .expectNextCount(1) //
                        .verifyComplete());
    }




    @BeforeAll
    void getBooks() {
        bookFlux = Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(strings -> Flux.fromIterable(strings))
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new Book(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice()));
    }

    String generateBarcode() {
        Random r1 = new Random();
        long[] longs = r1.longs(1, 100000000000L, 1000000000000L).toArray();
        return String.valueOf(longs[0]);

    }

    double genetaratePrice() {
        Random r2 = new Random();
        return Double.valueOf(r2.nextInt(3000)) / 100 + 10.00;
    }

    int getQty() {
        Random r1 = new Random();
        return r1.nextInt(30) + 5;
    }

}