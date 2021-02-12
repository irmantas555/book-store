package org.irmantas.booksstore.util;

import lombok.SneakyThrows;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


//@Disabled
@ExtendWith(SpringExtension.class)
//@ActiveProfiles("test")
@SpringBootTest(classes = InfrastructureConfiguration.class)
public class DbTests {

    @Autowired
    LoadFlux loadFlux;

    @Autowired
    BooksRepo booksRepo;

    @Autowired
    DatabaseClient client;

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



    @Test
    @Order(2)
    public void testPostRepositoryExisted() {
        assertNotNull(booksRepo);
    }


    @SneakyThrows
    @Test
    @Order(3)
    void inserAndDeleteBook() {
        this.loadFlux.getBooks()
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
