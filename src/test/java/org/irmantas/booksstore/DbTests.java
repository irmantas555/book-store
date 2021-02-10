package org.irmantas.booksstore;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.aspectj.lang.annotation.Before;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;


//@Disabled
@ExtendWith(SpringExtension.class)
//@ContextConfiguration
//@DataR2dbcTest
@ActiveProfiles("test")
@SpringBootTest(classes = InfrastructureConfiguration.class)
public class DbTests {

    String filePath = "src/main/resources/books.txt";

    Path path = Paths.get(filePath);

    Flux<Book> bookFlux;

    @Autowired
    DatabaseClient client;

    @Autowired
    BooksRepo booksRepo;

    @BeforeAll
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

    @Test
    @Order(2)
    public void testPostRepositoryExisted() {
        assertNotNull(booksRepo);
    }


    @SneakyThrows
    @Test
    @Order(3)
    void inserAndDeleteBook() {
        this.bookFlux
                .flatMap(book -> booksRepo.save(book))
                .as(StepVerifier::create)
                .expectNextMatches(b -> b instanceof Book)
                .expectNextCount(15)
                .verifyComplete();
    }

    @Test
    public void whenDeleteAll_then0IsExpected() {
        booksRepo.deleteAll()
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }



}
